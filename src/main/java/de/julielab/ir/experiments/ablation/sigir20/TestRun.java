package de.julielab.ir.experiments.ablation.sigir20;

import de.julielab.ir.experiments.ablation.AblationExperiments;
import de.julielab.ir.paramopt.HttpParamOptClient;
import de.julielab.ir.paramopt.HttpParamOptServer;
import de.julielab.ir.paramopt.SmacLiveRundataReader;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

public class TestRun {
    public static void main(String args[]) throws Exception {
        AblationExperiments exp = new AblationExperiments();
        Map<String, String> settings = new SmacLiveRundataReader().read(new File("smac-output/allparams_ba_split4/live-rundata-3.json")).getEntryWithBestScore().getRunInfo().getConfiguration().getSettings();
        double[] doubles = HttpParamOptClient.requestScoreFromServer(settings, "pm-xx-pm2019", "", HttpParamOptServer.GET_CONFIG_SCORE_PM, HttpParamOptServer.INFNDCG);
        System.out.println(Arrays.toString(doubles));
    }
}
