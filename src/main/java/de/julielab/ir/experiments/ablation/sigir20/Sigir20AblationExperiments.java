package de.julielab.ir.experiments.ablation.sigir20;

import de.julielab.ir.Multithreading;
import de.julielab.ir.es.ElasticSearchSetup;
import de.julielab.ir.evaluation.CrossEvaluation;
import de.julielab.ir.experiments.ablation.*;
import de.julielab.ir.paramopt.HttpParamOptServer;
import de.julielab.ir.paramopt.SmacLiveRundataEntry;
import de.julielab.ir.paramopt.SmacLiveRundataReader;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static de.julielab.ir.paramopt.HttpParamOptServer.INFNDCG;

public class Sigir20AblationExperiments {
    public static final String METRICS_TO_RETURN = "infndcg";
    public static final int SMAC_RUN_NUMBER = 1;
    public static final String SPLIT_TYPE = "train";
    private static final Logger log = LoggerFactory.getLogger(Sigir20AblationExperiments.class);
    private AblationExperiments ablationExperiments = new AblationExperiments();

    public static void main(String args[]) throws ExecutionException, InterruptedException {
        Sigir20AblationExperiments sigir20AblationExperiments = new Sigir20AblationExperiments();

        Future<?> f1 = Multithreading.getInstance().submit(() -> {
            try {
                sigir20AblationExperiments.doBaExperiments();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        Future<?> f2 = Multithreading.getInstance().submit(() -> {
            try {
                sigir20AblationExperiments.doCtExperiments();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        f1.get();
        log.info("BA finished");
        f2.get();
        log.info("CT finished");

        Multithreading.getInstance().shutdown();
    }

    public void doBaExperiments() throws IOException {
        doExperiments(new Sigir20TopDownAblationBAParameters(), "ba");
    }

    public void doExperiments(Map<String, Map<String, String>> ablationParameters, String corpus) throws IOException {
        Map<String, AblationCrossValResult> topDownAblationResults = runCrossvalAblationExperiment(ablationParameters, SMAC_RUN_NUMBER, corpus);
        List<AblationCrossValResult> scoreImprovingAblations = topDownAblationResults.values().stream().filter(result -> result.getMeanAblationScore(INFNDCG) > result.getMeanReferenceScore(INFNDCG)).collect(Collectors.toList());

        if (log.isInfoEnabled()) {
            log.info("Found the following ablations that actually improved the reference score:");
            for (AblationCrossValResult r : scoreImprovingAblations)
                log.info("{}: ablation {}, reference {} ({})", r.getAblationGroupName(), r.getMeanReferenceScore(INFNDCG), INFNDCG);
        }

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
        }
        String endpoint = corpus.equals("ba") ? HttpParamOptServer.GET_CONFIG_SCORE_PM : HttpParamOptServer.GET_CONFIG_SCORE_CT;
        AblationCrossValResult crossValResult = CrossEvaluation.runEval(evalParams, "%s-split%d-%s", "_copy%s", endpoint, METRICS_TO_RETURN);
        log.info("Evaluation with reference parameters overridden with best performing ablations:");
        for (AblationComparisonPair evalPair : crossValResult) {
            log.info("[{}] {}", corpus, evalPair.getReferenceScore(INFNDCG));
        }
        log.info("[{}] Average: {}", corpus, crossValResult.getMeanReferenceScore(INFNDCG));


    }

    public void doCtExperiments() throws IOException {
        doExperiments(new Sigir20TopDownAblationCTParameters(), "ct");
    }

    private Map<String, AblationCrossValResult> runCrossvalAblationExperiment(Map<String, Map<String, String>> parameters, int smacRunNumber, String corpus) throws IOException {
        SmacLiveRundataReader smacLiveRundataReader = new SmacLiveRundataReader();
        String instancePrefix = corpus.equals("ba") ? "pm" : "ct";
        String instanceFmtStr = "%s-split%d-%s";
        List<String> instances = new ArrayList<>();
        List<Map<String, String>> topDownReferenceParameters = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            instances.add(String.format(instanceFmtStr, instancePrefix, i, SPLIT_TYPE));
            File smacRunFile = Path.of("smac-output", String.format("allparams_%s_split%s", corpus, i), String.format("live-rundata-%s.json", smacRunNumber)).toFile();
            SmacLiveRundataEntry entryWithBestScore = smacLiveRundataReader.read(smacRunFile).getEntryWithBestScore();
            topDownReferenceParameters.add(entryWithBestScore.getRunInfo().getConfiguration().getSettings());
        }
        String endpoint = corpus.equals("ba") ? HttpParamOptServer.GET_CONFIG_SCORE_PM : HttpParamOptServer.GET_CONFIG_SCORE_CT;
        List<String> indexSuffixes = Arrays.stream(ElasticSearchSetup.independentCopies).map(c -> "_" + c).collect(Collectors.toList());

        Map<String, AblationCrossValResult> topDownAblationResults = ablationExperiments.getAblationCrossValResult(Collections.singletonList(parameters), topDownReferenceParameters, instances, indexSuffixes, METRICS_TO_RETURN, endpoint);


        List<Map<String, Map<String, String>>> bottomUpAblationParameters = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Map<String, Map<String, String>> bottomUpAblationThisSplit = corpus.equals("ba") ? new Sigir20BottomUpAblationBAParameters(topDownReferenceParameters.get(i)) : new Sigir20BottomUpAblationCTParameters(topDownReferenceParameters.get(i));
            bottomUpAblationParameters.add(bottomUpAblationThisSplit);
        }
        Map<String, String> bottomUpReferenceParameters = corpus.equals("ba") ? new Sigir20BaBottomUpRefParameters() : new Sigir20CtBottomUpRefParameters();
        Map<String, AblationCrossValResult> bottomUpAblationResults = ablationExperiments.getAblationCrossValResult(bottomUpAblationParameters, Collections.singletonList(bottomUpReferenceParameters), instances, indexSuffixes, METRICS_TO_RETURN, endpoint);

        /**
         * Multiply all the scores with -1 because the SMAC server returns negative values for parameter minimization
         */
        topDownAblationResults.values().stream().flatMap(Collection::stream).forEach(c -> {
            for (String metric : c.getMetrics()) {
                c.setReferenceScore(c.getReferenceScore(metric) * -1, metric);
                c.setAblationScore(c.getAblationScore(metric) * -1, metric);
            }
        });
        bottomUpAblationResults.values().stream().flatMap(Collection::stream).forEach(c -> {
            for (String metric : c.getMetrics()) {
                c.setReferenceScore(c.getReferenceScore(metric) * -1, metric);
                c.setAblationScore(c.getAblationScore(metric) * -1, metric);
            }
        });

        String caption = corpus.equals("ba") ? "This table shows the impact of individual system features for the biomedical abstracts task from two perspectives, namely a top-down and a bottom-up approach. In the top-down approach, the best performing system configuration is used as the reference configuration. In the bottom-up approach, no feature is active accept the usage of the disjunction max query structure for query expansion. When no query expansion is active, this has no effect. In each row, a feature is disabled (-) or enabled (+). Indented items are added or removed relative to their parent item." : "This table shows the impact of individual system features for the clinical trials task analogously to Table \\ref{tab:bafeatureablation}.";
        String label = corpus.equals("ba") ? "tab:bafeatureablation" : "tab:ctfeatureablation";
        StringBuilder sb = AblationLatexTableBuilder.buildLatexTable(topDownAblationResults, bottomUpAblationResults, caption, label, (AblationLatexTableInfo) parameters, (AblationLatexTableInfo) bottomUpAblationParameters.get(0));

        File tablefile = new File("sigir20-ablation-results", corpus + "-" + SPLIT_TYPE + ".tex");
        if (!tablefile.exists())
            tablefile.getParentFile().mkdirs();

        FileUtils.write(tablefile, sb.toString(), StandardCharsets.UTF_8);

        return topDownAblationResults;
    }


}
