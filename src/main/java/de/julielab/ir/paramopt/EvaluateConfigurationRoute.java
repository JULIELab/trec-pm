package de.julielab.ir.paramopt;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.trec.experiment.Experiment;
import at.medunigraz.imi.bst.trec.experiment.TrecPmRetrieval;
import at.medunigraz.imi.bst.trec.experiment.registry.ClinicalTrialsRetrievalRegistry;
import at.medunigraz.imi.bst.trec.experiment.registry.LiteratureArticlesRetrievalRegistry;
import at.medunigraz.imi.bst.trec.model.*;
import de.julielab.ir.goldstandards.AggregatedTrecQrelGoldStandard;
import de.julielab.ir.goldstandards.GoldStandard;
import de.julielab.ir.goldstandards.TrecPMGoldStandardFactory;
import de.julielab.ir.goldstandards.TrecQrelGoldStandard;
import de.julielab.ir.ltr.features.FeatureControlCenter;
import de.julielab.ir.model.QueryDescription;
import de.julielab.java.utilities.cache.CacheService;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.io.FileBased;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static de.julielab.ir.paramopt.HttpParamOptServer.*;
import static java.nio.charset.StandardCharsets.UTF_8;

public class EvaluateConfigurationRoute extends SmacWrapperBase implements Route {
    private static final Logger log = LoggerFactory.getLogger(EvaluateConfigurationRoute.class);
    private final GoldStandard<Topic> goldStandard;
    private final Map<String, TrecQrelGoldStandard<Topic>> goldStandardSplit;
    private final int numSplits = 10;

    public EvaluateConfigurationRoute(AggregatedTrecQrelGoldStandard aggregatedTrecQrelGoldStandard, Challenge challenge, Task task, GoldStandardType type) {
        goldStandard = aggregatedTrecQrelGoldStandard;
        log.info("Creating the gold standard cross-validation partitioning of size {}", numSplits);
        List<List<Topic>> partitioning = goldStandard.createPropertyBalancedQueryPartitioning(numSplits, Arrays.asList(Topic::getDisease, Topic::getGeneField));
        goldStandardSplit = new HashMap<>(numSplits);
        for (int i = 0; i < numSplits; i++) {
            TrecQrelGoldStandard gsSplit = new TrecQrelGoldStandard(challenge, task, i, type, partitioning.get(i), goldStandard.getQrelDocumentsForQueries(partitioning.get(i)));
            gsSplit.setQueryIdFunction(AggregatedTrecQrelGoldStandard.CROSS_DATASET_QUERY_ID_FUNCTION);
            goldStandardSplit.put("split" + i, gsSplit);
        }
    }

    @Override
    public Object handle(Request req, Response res) throws Exception {
        if (req.queryParams().contains("SHUTDOWN")) {
            CacheService.getInstance().commitAllCaches();
            log.info("Committing all caches is done, server can be shutdown.");
            return 0;
        }
        String score;
        try {
            Set<String> queryParams = req.queryParams();
            String instanceName = null;
            String instanceInfo = null;
            int cutoffTime = 0;
            int cutoffLength = 0;
            int seed = 0;
            String indexSuffix = null;
            String[] metricsToReturn = null;
            List<String> parameters = new ArrayList<>(queryParams.size());
            // Fill the beginning of the list because the parameter parsing algorithm is expecting it
            parameters.addAll(Arrays.asList(null, null, null, null, null));
            for (String queryParam : queryParams) {
                switch (queryParam) {
                    case INSTANCE:
                        instanceName = req.queryParams(queryParam);
                        break;
                    case INSTANCE_INFO:
                        instanceInfo = req.queryParams(queryParam);
                        break;
                    case CUTOFF_TIME:
                        cutoffTime = (int) Math.round(Double.valueOf(req.queryParams(queryParam)));
                        break;
                    case CUTOFF_LENGTH:
                        cutoffLength = (int) Math.round(Double.valueOf(req.queryParams(queryParam)));
                        break;
                    case SEED:
                        seed = Integer.valueOf(req.queryParams(queryParam));
                        break;
                    case INDEX_SUFFIX:
                        indexSuffix = req.queryParams(queryParam);
                        break;
                    case METRICS:
                        metricsToReturn = req.queryParams(queryParam).split(",");
                        break;
                    default:
                        parameters.add("-" + queryParam);
                        parameters.add(req.queryParams(queryParam));
                        break;
                }
            }
            String[] params = parameters.toArray(new String[0]);
            HierarchicalConfiguration<ImmutableNode> configuration = parseConfiguration(params);
            configuration.addProperty(INDEX_SUFFIX, indexSuffix);
            if (log.isDebugEnabled()) {
                FileHandler fh = new FileHandler((FileBased) configuration);
                StringWriter sw = new StringWriter();
                fh.save(sw);
                String xml = sw.toString();
                xml = xml.replaceAll("\n(\\s+)?", "");
                log.debug("Evaluating instance {} in thread {} on index copy {} with configuration: {}", instanceName, Thread.currentThread(), indexSuffix,xml);
            }
            score = calculateScore(configuration, metricsToReturn, instanceName, seed);
        } catch (Exception e) {
            throw e;
        }
        return score;
    }


    @Override
    protected String calculateScore(HierarchicalConfiguration<ImmutableNode> config, String[] metricsToReturn, String instance, int seed) {
        if (!FeatureControlCenter.isInitialized())
            FeatureControlCenter.initialize(config);
        else
            FeatureControlCenter.reconfigure(config);
        String indexSuffix = config.containsKey(INDEX_SUFFIX) ? config.getString(INDEX_SUFFIX) : "";
        log.debug("Index Suffix is set to '{}'", indexSuffix);
        TrecPmRetrieval trecPmRetrieval = instance.startsWith("pm-") ? LiteratureArticlesRetrievalRegistry.jlpmgeneric(TrecConfig.SIZE, instance, indexSuffix) : ClinicalTrialsRetrievalRegistry.jlctgeneric(TrecConfig.SIZE, instance, indexSuffix);
        trecPmRetrieval.withIndexSuffix(indexSuffix);

        // e.g. ct-split2-train
        String[] splitAndType = instance.split("-");
        String partitionType = splitAndType[2];
        GoldStandard<Topic> evalGs;
        if (partitionType.equals("test")) {
            Integer splitNumber = Integer.valueOf(String.valueOf(splitAndType[1].charAt(5)));
            // The test partition is just the the partition with the given number
            evalGs = goldStandardSplit.get("split" + splitNumber);
        } else if (partitionType.equals("train")) {
            Integer splitNumber = Integer.valueOf(String.valueOf(splitAndType[1].charAt(5)));
            // The train split is all except the test partition
            List<TrecQrelGoldStandard<Topic>> evalData = IntStream.range(0, numSplits).filter(i -> i != splitNumber).mapToObj(i -> goldStandardSplit.get("split" + i)).collect(Collectors.toList());
            evalGs = new AggregatedTrecQrelGoldStandard<>(evalData);
        } // the next "else" matches not really crossval splits but just the name of specific datasets like
        // pm2018 or ct2019. This way, we can easily evaluate on those specific gold standards.
        else if (partitionType.startsWith("pm")) {
            if (partitionType.endsWith("2017"))
                evalGs = TrecPMGoldStandardFactory.pubmedOfficial2017();
            else if (partitionType.endsWith("2018"))
                evalGs = TrecPMGoldStandardFactory.pubmedOfficial2017();
            else if (partitionType.endsWith("2019"))
                evalGs = TrecPMGoldStandardFactory.pubmedOfficial2019();
            else throw new IllegalArgumentException("Unknown biomedical abstracts dataset " + partitionType);

        } else if (partitionType.startsWith("ct")) {
            if (partitionType.endsWith("2018"))
                evalGs = TrecPMGoldStandardFactory.trialsOfficial2018();
            else if (partitionType.endsWith("2019"))
                evalGs = TrecPMGoldStandardFactory.trialsOfficial2019();
            else throw new IllegalArgumentException("Unknown clinical trials dataset " + partitionType);
        } else
            throw new IllegalArgumentException("Unknown split/dataset '" + partitionType + "'");

        log.debug("Evaluating instance {} with {} queries", instance, evalGs.getQueries().count());
        Experiment<QueryDescription> exp = new Experiment<>(evalGs, trecPmRetrieval);

        Metrics metrics = exp.run();
        CacheService.getInstance().commitAllCaches();
        logMetrics(config, instance, metrics, splitAndType[0], partitionType);
        if (metricsToReturn == null)
            metricsToReturn = new String[]{INFNDCG};
        StringBuilder sb = new StringBuilder();
        for (String metric : metricsToReturn) {
            double value;
            switch (metric) {
                case INFNDCG:
                    value = metrics.getInfNDCG();
                    break;
                case RPREC:
                    value = metrics.getRPrec();
                    break;
                case P10:
                    value = metrics.getP10();
                    break;
                case SET_RECALL:
                    value = metrics.getSetRecall();
                    break;
                default:
                    throw new IllegalArgumentException("Currenty unsupported metric: " + metric);
            }
            // SMAC always minimizes the objective, thus multiplying with -1
            sb.append(value*-1).append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    private void logMetrics(HierarchicalConfiguration<ImmutableNode> config, String instance, Metrics metrics, String corpus, String partitionType) {
        File dir = new File("smac-metrics-logging");
        File logfile = new File(dir, "parameteroptimization-log-" + corpus + "-" + partitionType + "-" + goldStandard.getDatasetId() + ".tsv");
        if (!dir.exists())
            dir.mkdirs();
        boolean logFileExists = logfile.exists();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(logfile, UTF_8, true))) {
            if (!logFileExists) {
                // Write header
                Iterator<String> keys = config.getKeys();
                bw.write(String.join("\t", (Iterable<String>) () -> keys));
                bw.write("\t");
                bw.write(String.join("\t", "instance", "infNDCG", "R-prec", "P@10", "Set-Recall"));
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
            bw.write(String.join("\t", instance, String.valueOf(metrics.getInfNDCG()), String.valueOf(metrics.getRPrec()), String.valueOf(metrics.getP10()), String.valueOf(metrics.getSetRecall())));
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
