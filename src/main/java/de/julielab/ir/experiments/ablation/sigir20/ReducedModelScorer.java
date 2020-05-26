package de.julielab.ir.experiments.ablation.sigir20;

import de.julielab.ir.Multithreading;
import de.julielab.ir.TrecCacheConfiguration;
import de.julielab.ir.es.ElasticSearchSetup;
import de.julielab.ir.experiments.ablation.AblationCrossValResult;
import de.julielab.ir.experiments.ablation.AblationExperiments;
import de.julielab.ir.paramopt.HttpParamOptServer;
import de.julielab.ir.paramopt.SmacLiveRundataEntry;
import de.julielab.ir.paramopt.SmacLiveRundataReader;
import de.julielab.java.utilities.cache.CacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class ReducedModelScorer {
    public static final String METRICS_TO_RETURN = "infndcg";
        public static final String smacRunNumber = "1";
    private final static Logger log = LoggerFactory.getLogger(ReducedModelScorer.class);
    public static String splitType = "test";
    private AblationExperiments ablationExperiments = new AblationExperiments();
    public static void main(String args[]) throws IOException {
        CacheService.initialize(new TrecCacheConfiguration());
        ReducedModelScorer scorer = new ReducedModelScorer();
        scorer.run("ba");
        scorer.run("ct");
        Multithreading.getInstance().shutdown();
        CacheService.getInstance().commitAllCaches();
    }

    private void run(String corpus) throws IOException {
        SmacLiveRundataReader smacLiveRundataReader = new SmacLiveRundataReader();
        String instancePrefix = corpus.equals("ba") ? "pm" : "ct";
        String instanceFmtStr = "%s-split%d-%s";
        List<String> instances = new ArrayList<>();
        List<Map<String, String>> topDownReferenceParameters = new ArrayList<>();
        List<SmacLiveRundataEntry> bestRuns = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            instances.add(String.format(instanceFmtStr, instancePrefix, i, splitType));
            File smacRunFile = Path.of("final-smac-output", String.format("allparams_%s_split%s", corpus, i), String.format("live-rundata-%s.json", smacRunNumber)).toFile();
            SmacLiveRundataEntry entryWithBestScore = smacLiveRundataReader.read(smacRunFile).getEntryWithBestScore();
            bestRuns.add(entryWithBestScore);

            Map<String, String> optimizedSettings = entryWithBestScore.getRunInfo().getConfiguration().getSettings();
            // always set hypernyms to false - they are contained in the optimized parameters but deactivated in the optimization code
            optimizedSettings.put("retrievalparameters.diseaseexpansion.hypernyms", "false");
            optimizedSettings.put("retrievalparameters.diseaseexpansion.hypernyms", "false");
            optimizedSettings.put("retrievalparameters.templateparameters.clauseboosts.exists_abstract_boost", "0");
            optimizedSettings.put("retrievalparameters.templateparameters.clauseboosts.filtered_treatments_boost", "0");
            optimizedSettings.put("retrievalparameters.templateparameters.clauseboosts.structured_boost", "0");
            topDownReferenceParameters.add(optimizedSettings);
        }

        String endpoint = corpus.equals("ba") ? HttpParamOptServer.GET_CONFIG_SCORE_PM : HttpParamOptServer.GET_CONFIG_SCORE_CT;
        List<String> indexSuffixes = Arrays.stream(ElasticSearchSetup.independentCopies).map(c -> "_" + c).collect(Collectors.toList());

        Map<String, Map<String, String>> reducedModelParameterOverrides = corpus.equals("ba") ? new Sigir20ReducedBaModel() : new Sigir20ReducedCtModel();
        Map<String, AblationCrossValResult> topDownAblationResults = ablationExperiments.getAblationCrossValResult(Collections.singletonList(reducedModelParameterOverrides), topDownReferenceParameters, instances, indexSuffixes, METRICS_TO_RETURN, true, endpoint);
        AblationCrossValResult reducedModelResults = topDownAblationResults.get("reduced");
        log.info("[{}] reference: {}", corpus, reducedModelResults.getMeanReferenceScore(METRICS_TO_RETURN));
        log.info("[{}] reduced model: {}", corpus, reducedModelResults.getMeanAblationScore(METRICS_TO_RETURN));
    }
}
