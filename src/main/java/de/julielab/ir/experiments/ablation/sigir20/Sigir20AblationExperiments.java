package de.julielab.ir.experiments.ablation.sigir20;

import de.julielab.ir.Multithreading;
import de.julielab.ir.TrecCacheConfiguration;
import de.julielab.ir.es.ElasticSearchSetup;
import de.julielab.ir.evaluation.CrossEvaluation;
import de.julielab.ir.experiments.ablation.*;
import de.julielab.ir.paramopt.HttpParamOptServer;
import de.julielab.ir.paramopt.SmacLiveRundataEntry;
import de.julielab.ir.paramopt.SmacLiveRundataReader;
import de.julielab.ir.paramopt.SmacParameterConfiguration;
import de.julielab.java.utilities.ConfigurationUtilities;
import de.julielab.java.utilities.cache.CacheService;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileBased;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static de.julielab.ir.paramopt.HttpParamOptServer.INFNDCG;
import static java.nio.charset.StandardCharsets.UTF_8;

public class Sigir20AblationExperiments {
    public static final String METRICS_TO_RETURN = "infndcg";
    public static final int SMAC_RUN_NUMBER = 1;
    private static final Logger log = LoggerFactory.getLogger(Sigir20AblationExperiments.class);
    public static String splitType = "test";
    private AblationExperiments ablationExperiments = new AblationExperiments();
    private String sigirExperimentResults = "sigir20-ablation-results";

    public static void main(String args[]) throws ExecutionException, InterruptedException {
        CacheService.initialize(new TrecCacheConfiguration());
        Sigir20AblationExperiments sigir20AblationExperiments = new Sigir20AblationExperiments();
        if (args.length > 0)
            splitType = args[0];

        Future<?> f1 = Multithreading.getInstance().submit(() -> {
            try {
                sigir20AblationExperiments.doBaExperiments();
            } catch (Exception e) {
                e.printStackTrace();
            }
            log.info("BA finished");
        });
        Future<?> f2 = Multithreading.getInstance().submit(() -> {
            try {
                sigir20AblationExperiments.doCtExperiments();
            } catch (Exception e) {
                e.printStackTrace();
            }
            log.info("CT finished");
        });

        f1.get();
        f2.get();

        Multithreading.getInstance().shutdown();
        CacheService.getInstance().commitAllCaches();
    }

    public void doBaExperiments() throws IOException, ConfigurationException {
        doExperiments(new Sigir20TopDownAblationBAParameters(), "ba");
    }

    public void doExperiments(Map<String, Map<String, String>> ablationParameters, String corpus) throws IOException, ConfigurationException {
        Map<String, AblationCrossValResult> topDownAblationResults = runCrossvalAblationExperiment(ablationParameters, SMAC_RUN_NUMBER, corpus);
        List<AblationCrossValResult> scoreImprovingAblations = topDownAblationResults.values().stream().filter(result -> result.getMeanAblationScore(INFNDCG) > result.getMeanReferenceScore(INFNDCG)).collect(Collectors.toList());

        if (log.isInfoEnabled()) {
            log.info("Found the following ablations that actually improved the reference score:");
            for (AblationCrossValResult r : scoreImprovingAblations) {
                log.info("[{}] {}: ablation {}, reference {} ({})", corpus, r.getAblationGroupName(), r.getMeanAblationScore(INFNDCG), r.getMeanReferenceScore(INFNDCG), INFNDCG);
            }
        }


        // Check score-improving ablations
        Map<String, String> improvingAblationsParameters = new HashMap<>();
        scoreImprovingAblations.stream().map(r -> ablationParameters.get(r.getAblationGroupName())).forEach(improvingAblationsParameters::putAll);

        SmacLiveRundataReader smacLiveRundataReader = new SmacLiveRundataReader();
        List<Map<String, String>> evalParams = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            File smacRunFile = Path.of("smac-output", String.format("allparams_%s_split%s", corpus, i), String.format("live-rundata-%s.json", SMAC_RUN_NUMBER)).toFile();
            SmacLiveRundataEntry entryWithBestScore = smacLiveRundataReader.read(smacRunFile).getEntryWithBestScore();
            Map<String, String> params = new HashMap<>();
            params.putAll(entryWithBestScore.getRunInfo().getConfiguration().getSettings());

            params.putAll(improvingAblationsParameters);
            evalParams.add(params);
        }
        String endpoint = corpus.equals("ba") ? HttpParamOptServer.GET_CONFIG_SCORE_PM : HttpParamOptServer.GET_CONFIG_SCORE_CT;
        String instancePrefix = corpus.equals("ba") ? "pm" : "ct";
        AblationCrossValResult crossValResult = CrossEvaluation.runEval(evalParams, instancePrefix + "-split%d-" + splitType, "_copy%s", endpoint, METRICS_TO_RETURN, true);
        log.info("Evaluation with reference parameters overridden with best performing ablations:");
        for (AblationComparisonPair evalPair : crossValResult) {
            log.info("[{}] {}", corpus, evalPair.getReferenceScore(INFNDCG));
        }
        log.info("[{}] Average: {}", corpus, crossValResult.getMeanReferenceScore(INFNDCG));


    }

    public void doCtExperiments() throws IOException, ConfigurationException {
        doExperiments(new Sigir20TopDownAblationCTParameters(), "ct");
    }

    private Map<String, AblationCrossValResult> runCrossvalAblationExperiment(Map<String, Map<String, String>> topDownAblationParams, int smacRunNumber, String corpus) throws IOException, ConfigurationException {
        SmacLiveRundataReader smacLiveRundataReader = new SmacLiveRundataReader();
        String instancePrefix = corpus.equals("ba") ? "pm" : "ct";
        String instanceFmtStr = "%s-split%d-%s";
        List<String> instances = new ArrayList<>();
        List<Map<String, String>> topDownReferenceParameters = new ArrayList<>();
        List<SmacLiveRundataEntry> bestRuns = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            instances.add(String.format(instanceFmtStr, instancePrefix, i, splitType));
            File smacRunFile = Path.of("smac-output", String.format("allparams_%s_split%s", corpus, i), String.format("live-rundata-%s.json", smacRunNumber)).toFile();
            SmacLiveRundataEntry entryWithBestScore = smacLiveRundataReader.read(smacRunFile).getEntryWithBestScore();
            bestRuns.add(entryWithBestScore);
            if (log.isDebugEnabled()) {
                FileHandler fh = new FileHandler((FileBased) entryWithBestScore.getRunInfo().getConfiguration().getSettingsAsXmlConfiguration());
                StringWriter sw = new StringWriter();
                fh.save(sw);
                String xml = sw.toString();
                xml = xml.replaceAll("\n(\\s+)?", "");
                log.debug("[{}, split{}] Best score from SMAC: {}, configuration: {} ", corpus, i, entryWithBestScore.getrQuality(), xml);
            }
            Map<String, String> optimizedSettings = entryWithBestScore.getRunInfo().getConfiguration().getSettings();
            // always set hypernyms to false - they are contained in the optimized parameters but deactivated in the optimization code
            optimizedSettings.put("retrievalparameters.diseaseexpansion.hypernyms", "false");
            optimizedSettings.put("retrievalparameters.diseaseexpansion.hypernyms", "false");
            optimizedSettings.put("retrievalparameters.templateparameters.clauseboosts.exists_abstract_boost", "0");
            optimizedSettings.put("retrievalparameters.templateparameters.clauseboosts.filtered_treatments_boost", "0");
            optimizedSettings.put("retrievalparameters.templateparameters.clauseboosts.structured_boost", "0");
            topDownReferenceParameters.add(optimizedSettings);
        }
        // Result logging
        File crossvalResults = new File(sigirExperimentResults, corpus+"-crossvalScores.tsv");
        FileUtils.write(crossvalResults, Stream.concat(Stream.of("source", "corpustype"), IntStream.range(0, 10).mapToObj(i -> "split"+i)).collect(Collectors.joining("\t"))+"\n", UTF_8);
        Stream<String> crossvalSmacResults = bestRuns.stream().map(SmacLiveRundataEntry::getrQuality).map(String::valueOf);
        String resultString = Stream.concat(Stream.of("smac", corpus), crossvalSmacResults).collect(Collectors.joining("\t"));
        FileUtils.write(crossvalResults, resultString+"\n", UTF_8, true);
        log.info("[{}] Best SMAC configuration crossval scores for train: {}", corpus, resultString);

        String endpoint = corpus.equals("ba") ? HttpParamOptServer.GET_CONFIG_SCORE_PM : HttpParamOptServer.GET_CONFIG_SCORE_CT;
        List<String> indexSuffixes = Arrays.stream(ElasticSearchSetup.independentCopies).map(c -> "_" + c).collect(Collectors.toList());

        Map<String, AblationCrossValResult> topDownAblationResults = ablationExperiments.getAblationCrossValResult(Collections.singletonList(topDownAblationParams), topDownReferenceParameters, instances, indexSuffixes, METRICS_TO_RETURN, true, endpoint);


        // Result logging
        Stream<String> crossvalReferenceAblationResults = topDownAblationResults.values().iterator().next().stream().map(c -> String.valueOf(c.getReferenceScore(INFNDCG)));
        String ablationResultString = Stream.concat(Stream.of("experiments", corpus), crossvalReferenceAblationResults).collect(Collectors.joining("\t"));
        FileUtils.write(crossvalResults, ablationResultString+"\n", UTF_8, true);
        for (String ablationGroup : topDownAblationResults.keySet()) {
            AblationCrossValResult ablationGroupResult = topDownAblationResults.get(ablationGroup);
            String ablationCrossValResultString = Stream.concat(Stream.of(ablationGroup, corpus), ablationGroupResult.stream().map(c -> String.valueOf(c.getAblationScore(INFNDCG)))).collect(Collectors.joining("\t"));
            FileUtils.write(crossvalResults, ablationCrossValResultString+"\n", UTF_8, true);
        }
        log.info("[{}] Reference crossval scores as obtained in ablation experiments for {}: {}",corpus, splitType, ablationResultString);

        // Topic wise result logging for statistical tests
        File topicscoresFile = new File(sigirExperimentResults, corpus+"-scoresPerTopic.tsv");
        List<String> topics = topDownAblationResults.values().iterator().next().stream().flatMap(p -> p.getReferenceMetrics().keySet().stream()).sorted().collect(Collectors.toList());
        FileUtils.write(topicscoresFile, "ablationgroup\t" + String.join("\t", topics)+"\n", UTF_8);
        boolean referenceWritten = false;
        for (String groupName : topDownAblationResults.keySet()) {
            Map<String, Double> groupValues = new HashMap<>();
            Map<String, Double> refValues = new HashMap<>();
            AblationCrossValResult pairs = topDownAblationResults.get(groupName);
            for (AblationComparisonPair p : pairs) {
                for (String topic : p.getAblationMetrics().keySet()) {
                    groupValues.put(topic, p.getAblationMetrics().get(topic).getInfNDCG());
                    refValues.put(topic, p.getReferenceMetrics().get(topic).getInfNDCG());
                }
            }
            if (!referenceWritten) {
                FileUtils.write(topicscoresFile, "reference\t" + topics.stream().map(refValues::get).map(String::valueOf).collect(Collectors.joining("\t"))+"\n", UTF_8, true);
                referenceWritten = true;
            }
            FileUtils.write(topicscoresFile, groupName+"\t" + topics.stream().map(groupValues::get).map(String::valueOf).collect(Collectors.joining("\t"))+"\n", UTF_8, true);
        }
        //----------------

        // Check scoring consistency
        if (splitType.equalsIgnoreCase("train")) {
            for (String ablationGroup : topDownAblationResults.keySet()) {
                for (int i = 0; i < 10; i++) {
                    if (i >= topDownAblationResults.get(ablationGroup).size())
                        continue;
                    double referenceScore = topDownAblationResults.get(ablationGroup).get(i).getReferenceScore(INFNDCG);
                    double bestScore = bestRuns.get(i).getrQuality();
                    if (Math.abs(bestScore - referenceScore) > 0.01)
                        log.warn("[{}] Expected best score {} as reference for ablation group {} on instance {} but got reference score {}", corpus, bestScore, ablationGroup, instances.get(i), referenceScore);
                }
            }
        }


        List<Map<String, Map<String, String>>> bottomUpAblationParameters = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Map<String, Map<String, String>> bottomUpAblationThisSplit = corpus.equals("ba") ? new Sigir20BottomUpAblationBAParameters(topDownReferenceParameters.get(i)) : new Sigir20BottomUpAblationCTParameters(topDownReferenceParameters.get(i));
            bottomUpAblationParameters.add(bottomUpAblationThisSplit);
        }
        Map<String, String> bottomUpReferenceParameters = corpus.equals("ba") ? new Sigir20BaBottomUpRefParameters() : new Sigir20CtBottomUpRefParameters();
        Map<String, AblationCrossValResult> bottomUpAblationResults = ablationExperiments.getAblationCrossValResult(bottomUpAblationParameters, Collections.singletonList(bottomUpReferenceParameters), instances, indexSuffixes, METRICS_TO_RETURN, true, endpoint);



        String caption = corpus.equals("ba") ? "This table shows the impact of individual system features for the biomedical abstracts task from two perspectives, namely a top-down and a bottom-up approach. In the top-down approach, the best performing system configuration is used as the reference configuration. In the bottom-up approach, no feature is active accept the usage of the disjunction max query structure for query expansion. When no query expansion is active, this has no effect. In each row, a feature is disabled (-) or enabled (+). Indented items are added or removed relative to their parent item." : "This table shows the impact of individual system features for the clinical trials task analogously to Table \\ref{tab:bafeatureablation}.";
        String label = corpus.equals("ba") ? "tab:bafeatureablation" : "tab:ctfeatureablation";
        StringBuilder sb = AblationLatexTableBuilder.buildLatexTable(topDownAblationResults, bottomUpAblationResults, caption, label, (AblationLatexTableInfo) topDownAblationParams, (AblationLatexTableInfo) bottomUpAblationParameters.get(0));

        // Topic wise result logging for statistical tests
        File bottomuptopicscoresFile = new File(sigirExperimentResults, corpus+"-scoresPerTopicBottomUp.tsv");
        FileUtils.write(bottomuptopicscoresFile, "ablationgroup\t" + String.join("\t", topics)+"\n", UTF_8);
        for (String groupName : bottomUpAblationResults.keySet()) {
            Map<String, Double> groupValues = new HashMap<>();
            Map<String, Double> refValues = new HashMap<>();
            AblationCrossValResult pairs = bottomUpAblationResults.get(groupName);
            for (AblationComparisonPair p : pairs) {
                for (String topic : p.getAblationMetrics().keySet()) {
                    groupValues.put(topic, p.getAblationMetrics().get(topic).getInfNDCG());
                    refValues.put(topic, p.getReferenceMetrics().get(topic).getInfNDCG());
                }
            }

            FileUtils.write(bottomuptopicscoresFile, groupName+"\t" + topics.stream().map(groupValues::get).map(String::valueOf).collect(Collectors.joining("\t"))+"\n", UTF_8, true);
        }




        File tablefile = new File(sigirExperimentResults, corpus + "-" + splitType + ".tex");
        if (!tablefile.exists())
            tablefile.getParentFile().mkdirs();

        FileUtils.write(tablefile, sb.toString(), UTF_8);

        return topDownAblationResults;
    }


}
