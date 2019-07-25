package at.medunigraz.imi.bst.trec;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.trec.experiment.Experiment;
import at.medunigraz.imi.bst.trec.experiment.registry.LiteratureArticlesRetrievalRegistry;
import at.medunigraz.imi.bst.trec.model.Topic;
import de.julielab.ir.goldstandards.TrecPMGoldStandardFactory;
import de.julielab.ir.goldstandards.TrecQrelGoldStandard;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class KeywordExperimenter {
    private static final int YEAR = 2018;

    private static final TrecQrelGoldStandard<Topic> GOLD_STANDARD = TrecPMGoldStandardFactory.pubmedOfficial2018();

    public static void main(String[] args) {
        final File keywordsSource = new File(KeywordExperimenter.class.getResource("/negative-keywords/").getFile());

        File[] files = null;
        if (keywordsSource.isDirectory()) {
            files = keywordsSource.listFiles();
        } else {
            files = new File[] { keywordsSource };
        }

        // FIXME set your baseline
        double baselineMetric = 0;

        // 1st step: collect metrics for individual keywords
        Set<Experiment> experiments = new LinkedHashSet<>();

        for (File keywordFile : files) {
            List<String> lines;
            try {
                lines = FileUtils.readLines(keywordFile, "UTF-8");
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }

            for (String keyword : lines) {
                experiments.add(new Experiment(GOLD_STANDARD, LiteratureArticlesRetrievalRegistry.keyword(YEAR, TrecConfig.SIZE, keyword)));
            }
        }

        TreeMap<Double, String> resultsUniqueKeywords = runExperiments(experiments);



        // 2nd step: collect metrics for combination of keywords in a greedy fashion
        experiments = new LinkedHashSet<>();

        String keyword = "";
        for (Map.Entry<Double, String> entry : resultsUniqueKeywords.entrySet()) {
            Double metric = entry.getKey();

            // Only use words that improve results over a baseline
            if (metric < baselineMetric) {
                break;
            }

            keyword = keyword + " " + entry.getValue();
            keyword = keyword.trim();

            experiments.add(new Experiment(GOLD_STANDARD, LiteratureArticlesRetrievalRegistry.keyword(YEAR, TrecConfig.SIZE, keyword)));
        }

        //TreeMap<Double, String> resultsCombinationKeywords = runExperiments(builder.build());
    }

    private static TreeMap<Double, String> runExperiments(Set<Experiment> experiments) {
        TreeMap<Double, String> results = new TreeMap<>(Collections.reverseOrder());

        for (Experiment exp : experiments) {
            exp.start();
            try {
                exp.join();

                // Change comparison metric here
                double metric = exp.allMetrics.getInfNDCG();
//				double metric = exp.allMetrics.getP10();
//				double metric = exp.allMetrics.getRPrec();
//				double metric = exp.allMetrics.getP5();
//				double metric = exp.allMetrics.getP15();

                results.put(metric, exp.getExperimentId());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Print the map
        results.entrySet().stream().forEach(System.out::println);

        return results;
    }

}
