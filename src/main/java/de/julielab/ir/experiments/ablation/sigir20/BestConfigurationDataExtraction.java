package de.julielab.ir.experiments.ablation.sigir20;

import de.julielab.ir.paramopt.SmacLiveRundata;
import de.julielab.ir.paramopt.SmacLiveRundataEntry;
import de.julielab.ir.paramopt.SmacLiveRundataReader;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class BestConfigurationDataExtraction {
    public static final String smacRunNumber = "1";

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
            SmacLiveRundata liveData = reader.read(smacRunFile);

            List<Integer> runNumbers = new ArrayList<>();
            List<Double> scores = new ArrayList<>();
            for (SmacLiveRundataEntry e : liveData) {
                if (scores.isEmpty() || scores.get(scores.size() - 1) < e.getrQuality()*-1) {
                    runNumbers.add(e.getRunInfo().getRcid());
                    scores.add(e.getrQuality()*-1);
                }
            }
            File outputfile = new File("sigir20-ablation-results", "smac-"+corpus+"progression-split" + i + ".csv");
            FileUtils.write(outputfile, "run number,infNDCG\n", StandardCharsets.UTF_8, false);
            for (int j = 0; j < scores.size(); j++) {
                Integer runNumber = runNumbers.get(j);
                Double score = scores.get(j);
                FileUtils.write(outputfile, runNumber +","+score+"\n", StandardCharsets.UTF_8, true);
            }

            SmacLiveRundataEntry entryWithBestScore = liveData.getEntryWithBestScore();
            bestEntries.add(entryWithBestScore);
        }
//
//        Set<String> numericalParams = bestEntries.stream().map(e -> e.getRunInfo().getConfiguration().getSettings())
//                .flatMap(m -> m.keySet().stream().filter(k -> {
//                    try {
//                        Double.parseDouble(m.get(k));
//                    } catch (NumberFormatException e) {
//                        return false;
//                    }
//                    return true;
//                })).collect(Collectors.toSet());
//        for (String p : numericalParams) {
//            double[] doubles = bestEntries.stream().map(e -> e.getRunInfo().getConfiguration().getSettings().get(p)).mapToDouble(Double::valueOf).toArray();
//            OptionalDouble average = Arrays.stream(doubles).average();
//            double variance = Arrays.stream(doubles).map(d -> Math.pow(d - average.getAsDouble(), 2) / (doubles.length - 1)).sum();
//            System.out.println("[" + corpus + "] " + p + " - avg: " + average.getAsDouble() + ", stddev: " + Math.sqrt(variance));
//        }


//        Set<String> booleanParams = bestEntries.stream().map(e -> e.getRunInfo().getConfiguration().getSettings())
//                .flatMap(m -> m.keySet().stream().filter(k -> {
//                    try {
//                        return Boolean.parseBoolean(m.get(k));
//                    } catch (NumberFormatException e) {
//                        return false;
//                    }
//                })).collect(Collectors.toSet());
//        for (String p : booleanParams) {
//            long count = bestEntries.stream().map(e -> e.getRunInfo().getConfiguration().getSettings().get(p)).map(Boolean::parseBoolean).filter(b -> b).count();
//            System.out.println("[" + corpus + "] " + p + ": " + count);
//        }
//        Set<String> booleanParams = bestEntries.stream().map(e -> e.getRunInfo().getConfiguration().getSettings())
//                .flatMap(m -> m.keySet().stream().filter(k -> {
//                    try {
//                        return m.get(k).equals("phrase") || m.get(k).equals("best_fields");
//                    } catch (NumberFormatException e) {
//                        return false;
//                    }
//                })).collect(Collectors.toSet());
//        for (String p : booleanParams) {
//            long phrasecount = bestEntries.stream().map(e -> e.getRunInfo().getConfiguration().getSettings().get(p)).filter(t -> t.equals("phrase")).count();
//            System.out.println("[" + corpus + "] " + p + " phrase: " + phrasecount);
//            long bestfieldscount = bestEntries.stream().map(e -> e.getRunInfo().getConfiguration().getSettings().get(p)).filter(t -> t.equals("best_fields")).count();
//            System.out.println("[" + corpus + "] " + p + " best_fields: " + bestfieldscount);
//        }
//        Set<String> nonMelBestFieldsParams = bestEntries.stream().map(e -> e.getRunInfo().getConfiguration().getSettings())
//                .flatMap(m -> m.keySet().stream().filter(k -> {
//                    try {
//                        return m.get(k).contains("no_non_melanoma") || m.get(k).equals("best_fields");
//                    } catch (NumberFormatException e) {
//                        return false;
//                    }
//                })).collect(Collectors.toSet());
//        for (String p : nonMelBestFieldsParams) {
//            long phrasecount = bestEntries.stream().map(e -> e.getRunInfo().getConfiguration().getSettings().get(p)).filter(t -> t.equals("phrase")).count();
//            System.out.println("[" + corpus + "] " + p + " phrase: " + phrasecount);
//            long bestfieldscount = bestEntries.stream().map(e -> e.getRunInfo().getConfiguration().getSettings().get(p)).filter(t -> t.equals("best_fields")).count();
//            System.out.println("[" + corpus + "] " + p + " best_fields: " + bestfieldscount);


//        Set<String> templateParamters = Collections.singleton("retrievalparameters.template");
//        for (String p : templateParamters) {
//            long melanoma = bestEntries.stream().map(e -> e.getRunInfo().getConfiguration().getSettings().get(p)).filter(t -> t.contains("melanoma")).count();
//            System.out.println("[" + corpus + "] " + p + " no non melanoma: " + melanoma);
//            long dismax = bestEntries.stream().map(e -> e.getRunInfo().getConfiguration().getSettings().get(p)).filter(t -> t.contains("dismax")).count();
//            System.out.println("[" + corpus + "] " + p + " no dismax: " + dismax);
//            long defaulttemplate = bestEntries.stream().map(e -> e.getRunInfo().getConfiguration().getSettings().get(p)).filter(t -> t.endsWith("generic.json")).count();
//            System.out.println("[" + corpus + "] " + p + " default: " + defaulttemplate);
//        }
    }
}
