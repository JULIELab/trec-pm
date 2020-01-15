package de.julielab.ir.experiments.ablation.sigir20;

import de.julielab.ir.Multithreading;
import de.julielab.ir.es.ElasticSearchSetup;
import de.julielab.ir.experiments.ablation.AblationCrossValResult;
import de.julielab.ir.experiments.ablation.AblationExperiments;
import de.julielab.ir.experiments.ablation.AblationLatexTableBuilder;
import de.julielab.ir.experiments.ablation.AblationLatexTableInfo;
import de.julielab.ir.paramopt.HttpParamOptServer;
import de.julielab.ir.paramopt.SmacLiveRundataEntry;
import de.julielab.ir.paramopt.SmacLiveRundataReader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class Sigir20AblationExperiments {
    public static final String METRICS_TO_RETURN = "infndcg";
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
        f2.get();

        Multithreading.getInstance().shutdown();
    }

    public void doBaExperiments() throws IOException {

        int smacRunNumber = 1;
        String corpus = "ba";
        runCrossvalAblationExperiment(new Sigir20TopDownAblationBAParameters(), smacRunNumber, corpus);
    }

    public void doCtExperiments() throws IOException {

        int smacRunNumber = 1;
        String corpus = "ct";
        runCrossvalAblationExperiment(new Sigir20TopDownAblationCTParameters(), smacRunNumber, corpus);
    }

    private void runCrossvalAblationExperiment(Map<String, Map<String, String>> parameters, int smacRunNumber, String corpus) throws IOException {
        SmacLiveRundataReader smacLiveRundataReader = new SmacLiveRundataReader();
        String instancePrefix = corpus.equals("ba") ? "pm" : "ct";
        String instanceFmtStr = "%s-split%d-test";
        List<String> instances = new ArrayList<>();
        List<Map<String, String>> topDownReferenceParameters = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            instances.add(String.format(instanceFmtStr, instancePrefix, i));
            File smacRunFile = Path.of("smac-output", String.format("allparams_%s_split%s", corpus, i), String.format("live-rundata-%s.json", smacRunNumber)).toFile();
            SmacLiveRundataEntry entryWithBestScore = smacLiveRundataReader.read(smacRunFile).getEntryWithBestScore();
            topDownReferenceParameters.add(entryWithBestScore.getRunInfo().getConfiguration().getSettings());
        }
        String endpoint = corpus.equals("ba") ? HttpParamOptServer.GET_CONFIG_SCORE_PM : HttpParamOptServer.GET_CONFIG_SCORE_CT;
        List<String> indexSuffixes = Arrays.stream(ElasticSearchSetup.independentCopies).map(c -> "_" + c).collect(Collectors.toList());

        Map<String, AblationCrossValResult> ablationCrossValResult = ablationExperiments.getAblationCrossValResult(Collections.singletonList(parameters), topDownReferenceParameters, instances, indexSuffixes, METRICS_TO_RETURN, endpoint);


        List<Map<String, Map<String, String>>> bottomUpAblationParameters = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Map<String, Map<String, String>> bottomUpAblationThisSplit = corpus.equals("ba") ? new Sigir20BottomUpAblationBAParameters(topDownReferenceParameters.get(i)) : new Sigir20BottomUpAblationCTParameters(topDownReferenceParameters.get(i));
            bottomUpAblationParameters.add(bottomUpAblationThisSplit);
        }
        Map<String, String> bottomUpReferenceParameters = corpus.equals("ba") ? new Sigir20BaBottomUpRefParameters() : new Sigir20CtBottomUpRefParameters();
        Map<String, AblationCrossValResult> ablationCrossValResult1 = ablationExperiments.getAblationCrossValResult(bottomUpAblationParameters, Collections.singletonList(bottomUpReferenceParameters), instances, indexSuffixes, METRICS_TO_RETURN, endpoint);

        /**
         * Multiply all the scores with -1 because the SMAC server returns negative values for parameter minimization
         */
        ablationCrossValResult.values().stream().flatMap(Collection::stream).forEach(c -> {
            for (String metric : c.getMetrics()) {
                c.setReferenceScore(c.getReferenceScore(metric) * -1, metric);
                c.setAblationScore(c.getAblationScore(metric) * -1, metric);
            }
        });

        String caption = corpus.equals("ba") ? "This table shows the impact of individual system features for the biomedical abstracts task from two perspectives, namely a top-down and a bottom-up approach. In the top-down approach, the best performing system configuration is used as the reference configuration. In the bottom-up approach, no feature is active accept the usage of the disjunction max query structure for query expansion. When no query expansion is active, this has no effect. In each row, a feature is disabled (-) or enabled (+). Indented items are added or removed relative to their parent item." : "This table shows the impact of individual system features for the clinical trials task analogously to Table \\ref{tab:bafeatureablation}.";
        String label = corpus.equals("ba") ? "tab:bafeatureablation" : "tab:ctfeatureablation";
        StringBuilder sb = AblationLatexTableBuilder.buildLatexTable(ablationCrossValResult, ablationCrossValResult1, caption, label, (AblationLatexTableInfo)topDownReferenceParameters.get(0), (AblationLatexTableInfo)bottomUpAblationParameters.get(0));

        System.out.println(sb.toString());
    }


}
