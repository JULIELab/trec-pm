package de.julielab.ir.experiments.ablation.sigir20;

import de.julielab.ir.paramopt.SmacLiveRundataEntry;
import de.julielab.ir.paramopt.SmacLiveRundataReader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class BestConfigurationDataExtraction  {
    public static final String smacRunNumber = "1";
    private List<String> paramsToGet = Arrays.asList("indexparameters.bm25.k1", "indexparameters.bm25.b", "retrievalparameters.templateparameters.disease.boosts.disease_query_boost", "retrievalparameters.templateparameters.gene.boosts.gene_query_boost");
    public static void main(String args[]) throws Exception {
        BestConfigurationDataExtraction extraction = new BestConfigurationDataExtraction();
        extraction.run("ba");
        extraction.run("ct");
    }

    private void run(String corpus) throws IOException {
        SmacLiveRundataReader reader = new SmacLiveRundataReader();
        List<SmacLiveRundataEntry> bestEntries = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            File smacRunFile = Path.of("smac-output", String.format("allparams_%s_split%s", corpus, i), String.format("live-rundata-%s.json", smacRunNumber)).toFile();
            SmacLiveRundataEntry entryWithBestScore = reader.read(smacRunFile).getEntryWithBestScore();
            bestEntries.add(entryWithBestScore);
        }
        for (String p : paramsToGet) {
            OptionalDouble average = bestEntries.stream().map(e -> e.getRunInfo().getConfiguration().getSettings().get(p)).mapToDouble(Double::valueOf).average();
            System.out.println("["+corpus+"] " + p + ": " + average.getAsDouble());
        }
    }
}
