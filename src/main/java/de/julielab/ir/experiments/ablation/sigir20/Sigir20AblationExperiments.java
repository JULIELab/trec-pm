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
import java.util.Map;

public class Sigir20AblationExperiments {
    private AblationExperiments ablationExperiments = new AblationExperiments();

    public static void main(String args[]) throws IOException {
        Sigir20AblationExperiments sigir20AblationExperiments = new Sigir20AblationExperiments();
        sigir20AblationExperiments.doCtExperiments();
    }

    public void doBaExperiments() throws IOException {

        int smacRunNumber = 1;
        String corpus = "pm";
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
        File smacRunFile = Path.of("smac-output", String.format("allparams_%s_split%s", corpus, splitNum), String.format("live-rundata-%s.json", smacRunNumber)).toFile();
        SmacLiveRundataEntry entryWithBestScore = smacLiveRundataReader.read(smacRunFile).getEntryWithBestScore();
        Map<String, String> referenceParameters = entryWithBestScore.getRunInfo().getConfiguration().getSettings();
        String instancePrefix = corpus.equals("ba") ? "pm" : "ct";
        String instance = String.format("%s-split%s-test", instancePrefix, splitNum);
        String endpoint = corpus.equals("ba") ? HttpParamOptServer.GET_CONFIG_SCORE_PM : HttpParamOptServer.GET_CONFIG_SCORE_CT;
        String indexSuffix = "_"+ElasticSearchSetup.independentCopies[splitNum];

        AblationCrossValResult ablationCrossValResult = ablationExperiments.getAblationCrossValResult(parameters, referenceParameters, instance, indexSuffix, endpoint);
        System.out.println(ablationCrossValResult);
    }
}
