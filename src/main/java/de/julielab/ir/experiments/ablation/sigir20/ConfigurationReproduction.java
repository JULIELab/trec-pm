package de.julielab.ir.experiments.ablation.sigir20;

import at.medunigraz.imi.bst.trec.model.Metrics;
import de.julielab.ir.paramopt.HttpParamOptClient;
import de.julielab.ir.paramopt.HttpParamOptServer;
import de.julielab.ir.paramopt.SmacLiveRundataEntry;
import de.julielab.ir.paramopt.SmacLiveRundataReader;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileBased;
import org.apache.commons.configuration2.io.FileHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConfigurationReproduction {
    public static void main(String args[]) throws Exception {
        ConfigurationReproduction configurationReproduction = new ConfigurationReproduction();
        configurationReproduction.run();
    }

    private static final Logger log = LoggerFactory.getLogger(ConfigurationReproduction.class);
    public static final String splitType = "train";
    public static final String smacRunNumber = "1";
    public void run() throws Exception {
        runReproduction("ct");
//        runReproduction("ba");
    }

    private void runReproduction(String corpus) throws IOException, ConfigurationException {
        String endpoint = corpus.equals("ba") ? HttpParamOptServer.GET_CONFIG_SCORE_PM : HttpParamOptServer.GET_CONFIG_SCORE_CT;
        SmacLiveRundataReader smacLiveRundataReader = new SmacLiveRundataReader();
        String instancePrefix = corpus.equals("ba") ? "pm" : "ct";
        String instanceFmtStr = "%s-split%d-%s";
        List<String> instances = new ArrayList<>();
        List<SmacLiveRundataEntry> bestRuns = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            String instance = String.format(instanceFmtStr, instancePrefix, i, splitType);
            instances.add(instance);
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
            Map<String, Metrics> metricsPerTopic = HttpParamOptClient.requestScoreFromServer(entryWithBestScore.getRunInfo().getConfiguration().getSettings(), instance, "_copy" + i, endpoint, HttpParamOptServer.INFNDCG, false);
            log.info("[{}] instance: {}, expected score: {}, actual score: {}", corpus, instance, entryWithBestScore.getrQuality(), metricsPerTopic.get("all").getInfNDCG());
        }
    }
}
