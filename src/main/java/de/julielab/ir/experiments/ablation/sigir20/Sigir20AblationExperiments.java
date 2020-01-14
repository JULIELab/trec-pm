package de.julielab.ir.experiments.ablation.sigir20;

import de.julielab.ir.es.ElasticSearchSetup;
import de.julielab.ir.experiments.ablation.AblationCrossValResult;
import de.julielab.ir.experiments.ablation.AblationExperiments;
import de.julielab.ir.paramopt.HttpParamOptServer;
import de.julielab.ir.paramopt.SmacLiveRundataEntry;
import de.julielab.ir.paramopt.SmacLiveRundataReader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import static de.julielab.ir.paramopt.HttpParamOptServer.*;

public class Sigir20AblationExperiments {
    public static final String METRICS_TO_RETURN = "infndcg,rprec,p10";
    private AblationExperiments ablationExperiments = new AblationExperiments();

    public static void main(String args[]) throws IOException {
        Sigir20AblationExperiments sigir20AblationExperiments = new Sigir20AblationExperiments();
        sigir20AblationExperiments.doCtExperiments();
    }

    public void doBaExperiments() throws IOException {

        int smacRunNumber = 1;
        String corpus = "ba";
        int splitNum = 0;

        runCrossvalAblationExperiment(new Sigir20TopDownAblationBAParameters(), smacRunNumber, corpus, splitNum);
    }

    public void doCtExperiments() throws IOException {

        int smacRunNumber = 1;
        String corpus = "ct";
        int splitNum = 0;

        runCrossvalAblationExperiment(new Sigir20TopDownAblationCTParameters(), smacRunNumber, corpus, splitNum);
    }

    private void runCrossvalAblationExperiment(Map<String, Map<String, String>> parameters, int smacRunNumber, String corpus, int splitNum) throws IOException {
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
        System.out.println(ablationCrossValResult);


        List<Map<String, Map<String, String>>> bottomUpAblationParameters = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Map<String, Map<String, String>> bottomUpAblationThisSplit = corpus.equals("ba") ? new Sigir20BottomUpAblationBAParameters(topDownReferenceParameters.get(i)) : new Sigir20BottomUpAblationCTParameters(topDownReferenceParameters.get(i));
            bottomUpAblationParameters.add(bottomUpAblationThisSplit);
        }
        Map<String, String> buttomUpReferenceParameters = corpus.equals("ba") ? new Sigir20BaBottomUpRefParameters() : new Sigir20CtBottomUpRefParameters();
        Map<String, AblationCrossValResult> ablationCrossValResult1 = ablationExperiments.getAblationCrossValResult(bottomUpAblationParameters, Collections.singletonList(buttomUpReferenceParameters), instances, indexSuffixes, METRICS_TO_RETURN, endpoint);
        System.out.println(ablationCrossValResult1);

        StringBuilder sb = new StringBuilder();
        sb.append("\\begin{table}[hp]\n" +
                "\\caption{This table shows the impact of individual system features for the biomedical abstracts task from two perspectives, namely a top-down and a bottom-up approach. In the top-down approach, the best performing system configuration is used as the reference configuration. In the bottom-up approach, no feature is active accept the usage of the disjunction max query structure for query expansion. When no query expansion is active, this has no effect. In each row, a feature is disabled (-) or enabled (+). Indented items are added or removed relative to their parent item.}\n" +
                "\\begin{center}\n" +
                "\\begin{tabular}{l|c|c|c|c|c}\n" +
                "\\toprule\n" +
                "configuration & infNDCG & R-Prec & P@10 & mean & diff to ref \\\\\n" +
                "\\midrule\n" +
                "\\multicolumn{6}{c}{\\textbf{top-down}} \\\\\n" +
                "\\midrule\n");
        AblationCrossValResult firstResultForGettingReferenceScore = ablationCrossValResult.values().iterator().next();
        appendReferenceTableLine(sb, firstResultForGettingReferenceScore);
        for (AblationCrossValResult r : ablationCrossValResult.values())
            getAblationResultTableLine(sb, r);

        System.out.println(sb.toString());
    }

    private void getAblationResultTableLine(StringBuilder sb, AblationCrossValResult crossValResult) {
        DecimalFormat df = new DecimalFormat("#,###,###,##0.00");
        double[] ablationScores = new double[]{crossValResult.getMeanAblationScore(INFNDCG), crossValResult.getMeanAblationScore(RPREC), crossValResult.getMeanAblationScore(P10)};
        double[] referenceScores = new double[]{crossValResult.getMeanReferenceScore(INFNDCG), crossValResult.getMeanReferenceScore(RPREC), crossValResult.getMeanReferenceScore(P10)};
        double ablationMean = DoubleStream.of(ablationScores).average().getAsDouble();
        double referenceMean = DoubleStream.of(referenceScores).average().getAsDouble();
        double diff = referenceMean - ablationMean;
        double pct = diff / referenceMean * 100;
        sb.append(crossValResult.get(0).getAblationName()).append("&").append(ablationScores[1]).append("&").append(ablationScores[2]).append(ablationMean).append(String.format("$%s\\%$", df.format(pct))).append("\\").append("\n");
    }

    private void appendReferenceTableLine(StringBuilder sb, AblationCrossValResult crossValResult) {
        double[] scores = new double[]{crossValResult.getMeanReferenceScore(INFNDCG), crossValResult.getMeanReferenceScore(RPREC), crossValResult.getMeanReferenceScore(P10)};

        sb.append("ALL (ref)  &").append(scores[0]).append("&").append(scores[1]).append("&").append(scores[2]).append(DoubleStream.of(scores).average().getAsDouble()).append("$0.0\\%$").append("\\").append("\n");
    }
}
