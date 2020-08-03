package de.julielab.ir.experiments.ablation.sigir20;

import cc.mallet.types.Alphabet;
import de.julielab.ir.paramopt.SmacLiveRundata;
import de.julielab.ir.paramopt.SmacLiveRundataEntry;
import de.julielab.ir.paramopt.SmacLiveRundataReader;
import de.julielab.java.utilities.FileUtilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BestConfigurationVectorExtraction {
    public static final String smacRunNumber = "1";

    public static void main(String args[]) throws Exception {
        BestConfigurationVectorExtraction extraction = new BestConfigurationVectorExtraction();
        Alphabet featureAlphabet = new Alphabet();
        try (BufferedWriter bw = FileUtilities.getWriterToFile(new File("smac-best-configuration-vectors.csv"))) {
            List<List<Object>> baVectors = extraction.run("ba", featureAlphabet, bw);
            List<List<Object>> ctVectors = extraction.run("ct", featureAlphabet, bw);
            int maxLength = Stream.concat(baVectors.stream(), ctVectors.stream()).mapToInt(Collection::size).max().getAsInt();
            Stream.concat(baVectors.stream(), ctVectors.stream()).forEach(l -> pad(l, maxLength));
            writeHeader(featureAlphabet, bw);
            writeVectors("ba", baVectors, bw);
            writeVectors("ct", ctVectors, bw);
        }
    }

    private static void writeHeader(Alphabet featureAlphabet, BufferedWriter bw) throws IOException {
        bw.write("dataset,");
        for (int i = 0; i < featureAlphabet.size(); ++i) {
            bw.write(featureAlphabet.lookupObject(i).toString());
            if (i < featureAlphabet.size() - 1)
                bw.write(",");
        }
        bw.newLine();
    }

    private static void writeVectors(String corpus, List<List<Object>> vectors, BufferedWriter bw) throws IOException {
        for (int i = 0; i < vectors.size(); i++) {
            List<Object> configurationVector = vectors.get(i);
            bw.write(Stream.concat(Stream.of(corpus), configurationVector.stream().map(String::valueOf)).collect(Collectors.joining(",")));
            bw.newLine();
        }
    }

    public static void pad(List<Object> list, int size) {
        int listSize = list.size();
        if (listSize < size) {
            for (int i = 0; i < size - listSize; ++i) {
                list.add(null);
            }
        }
    }

    public static void ensureSize(ArrayList<?> list, int size) {
        // Prevent excessive copying while we're adding
        list.ensureCapacity(size);
        while (list.size() < size) {
            list.add(null);
        }
    }

    private List<List<Object>> run(String corpus, Alphabet featureAlphabet, BufferedWriter bw) throws IOException {
        SmacLiveRundataReader reader = new SmacLiveRundataReader();
        List<List<Object>> vectors = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            File smacRunFile = Path.of("/Users/faessler/Research/sigir20/smacruns/smac-output-2-21-01", String.format("allparams_%s_split%s", corpus, i), String.format("live-rundata-%s.json", smacRunNumber)).toFile();
            SmacLiveRundata liveData = reader.read(smacRunFile);
            SmacLiveRundataEntry entryWithBestScore = liveData.getEntryWithBestScore();

            List<Object> configurationVector = getConfigurationVector(entryWithBestScore, featureAlphabet);
            vectors.add(configurationVector);

        }
        return vectors;
    }

    private List<Object> getConfigurationVector(SmacLiveRundataEntry entry, Alphabet featureAlphabet) {
        ArrayList<Object> features = new ArrayList<>(200);

        Map<String, String> settings = entry.getRunInfo().getConfiguration().getSettings();
        for (String k : settings.keySet()) {
            int featureIndex = featureAlphabet.lookupIndex(k);
            ensureSize(features, featureIndex + 1);
            features.set(featureIndex, settings.get(k));
        }
        return features;
    }
}
