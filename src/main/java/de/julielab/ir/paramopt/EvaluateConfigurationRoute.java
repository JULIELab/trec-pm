package de.julielab.ir.paramopt;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.trec.experiment.Experiment;
import at.medunigraz.imi.bst.trec.experiment.TrecPmRetrieval;
import at.medunigraz.imi.bst.trec.experiment.registry.LiteratureArticlesRetrievalRegistry;
import at.medunigraz.imi.bst.trec.model.*;
import de.julielab.ir.goldstandards.AggregatedTrecQrelGoldStandard;
import de.julielab.ir.goldstandards.GoldStandard;
import de.julielab.ir.goldstandards.TrecQrelGoldStandard;
import de.julielab.ir.ltr.features.FeatureControlCenter;
import de.julielab.ir.model.QueryDescription;
import de.julielab.java.utilities.cache.CacheService;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static de.julielab.ir.paramopt.HttpParamOptServer.*;
import static java.nio.charset.StandardCharsets.UTF_8;

public class EvaluateConfigurationRoute extends SmacWrapperBase implements Route {
    private static final Logger log = LogManager.getLogger();
    private final GoldStandard<Topic> goldStandard;
    private final Map<String, TrecQrelGoldStandard<Topic>> goldStandardSplit;
    private final int numSplits = 10;

    public EvaluateConfigurationRoute(AggregatedTrecQrelGoldStandard aggregatedTrecQrelGoldStandard, Challenge challenge, Task task, GoldStandardType type) {
        goldStandard = aggregatedTrecQrelGoldStandard;
        log.info("Creating the gold standard cross-validation partitioning of size {}", 10);
        List<List<Topic>> partitioning = goldStandard.createPropertyBalancedQueryPartitioning(numSplits, Arrays.asList(Topic::getDisease, Topic::getGeneField));
        goldStandardSplit = new HashMap<>(numSplits);
        for (int i = 0; i < goldStandardSplit.size(); i++) {
            goldStandardSplit.put("split" + i, new TrecQrelGoldStandard(challenge, task, -1, type, partitioning.get(i), goldStandard.getQrelDocumentsForQueries(partitioning.get(i))));
        }
    }

    @Override
    public Object handle(Request req, Response res) throws Exception {
        if (req.queryParams().contains("SHUTDOWN")) {
            CacheService.getInstance().commitAllCaches();
            log.info("Comitting all caches is done, server can be shutdown.");
            return 0;
        }
        double score = 0;
        try {
            Set<String> queryParams = req.queryParams();
            String instanceName = null;
            int cutoffTime = 0;
            int cutoffLength = 0;
            int seed = 0;
            List<String> parameters = new ArrayList<>(queryParams.size() - 4);
            for (String queryParam : queryParams) {
                switch (queryParam) {
                    case INSTANCE:
                        instanceName = req.queryParams(queryParam);
                        break;
                    case CUTOFF_TIME:
                        cutoffTime = Integer.valueOf(req.queryParams(queryParam));
                        break;
                    case CUTOFF_LENGTH:
                        cutoffLength = Integer.valueOf(req.queryParams(queryParam));
                        break;
                    case SEED:
                        seed = Integer.valueOf(req.queryParams(queryParam));
                        break;
                    default:
                        parameters.add("-" + queryParam);
                        parameters.add(req.queryParams(queryParam));
                        break;
                }
            }
            HierarchicalConfiguration<ImmutableNode> configuration = parseConfiguration(parameters.toArray(new String[0]));
            score = calculateScore(configuration, instanceName, seed);
        } catch (Exception e) {
            throw e;
        }
        return score;
    }


    @Override
    protected double calculateScore(HierarchicalConfiguration<ImmutableNode> config, String instance, int seed) {
        if (!FeatureControlCenter.isInitialized())
            FeatureControlCenter.initialize(config);
        else
            FeatureControlCenter.reconfigure(config);
        TrecPmRetrieval trecPmRetrieval = LiteratureArticlesRetrievalRegistry.jlpmcommon2Generic(TrecConfig.SIZE);
        // e.g. split2-train
        String[] splitAndType = instance.split("-");
        Integer splitNumber = Integer.valueOf(splitAndType[0].charAt(5));
        String splitType = splitAndType[1];
        GoldStandard<Topic> evalGs;
        if (splitType.equals("test")) {
            // The test partition is just the the partition with the given number
            evalGs = goldStandardSplit.get(splitNumber);
        } else if (splitType.equals("train")) {
            // The train split is all except the test partition
            evalGs = new AggregatedTrecQrelGoldStandard<>(IntStream.range(0, numSplits).filter(i -> i != splitNumber).mapToObj(goldStandardSplit::get).collect(Collectors.toList()));
        } else {
            throw new IllegalArgumentException("Unknown split type " + splitType);
        }
        Experiment<QueryDescription> exp = new Experiment<>(evalGs, trecPmRetrieval);

        Metrics metrics = exp.run();
        logMetrics(config, metrics);
        // SMAC always minimizes the objective, thus multiplying with -1
        return -1 * metrics.getInfNDCG();
    }

    private void logMetrics(HierarchicalConfiguration<ImmutableNode> config, Metrics metrics) {
        File dir = new File("smac-metrics-logging");
        File logfile = new File(dir, "parameteroptimization-log-" + goldStandard.getDatasetId() + ".tsv");
        if (!dir.exists())
            dir.mkdirs();
        boolean logFileExists = logfile.exists();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(logfile, UTF_8, true))) {
            if (!logFileExists) {
                // Write header
                Iterator<String> keys = config.getKeys();
                bw.write(String.join("\t", (Iterable<String>) () -> keys));
                bw.write("\t");
                bw.write(String.join("\t", "infNDCG", "R-prec", "P@10", "Set-Recall"));
                bw.newLine();
            }
            Iterator<String> keys = config.getKeys();
            List<String> values = new ArrayList<>();
            while (keys.hasNext()) {
                String key = keys.next();
                values.add(config.getString(key));
            }
            bw.write(String.join("\t", values));
            bw.write("\t");
            bw.write(String.join("\t", String.valueOf(metrics.getInfNDCG()), String.valueOf(metrics.getRPrec()), String.valueOf(metrics.getP10()), String.valueOf(metrics.getSetRecall())));
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
