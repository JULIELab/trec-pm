package de.julielab.ir.evaluation;

import at.medunigraz.imi.bst.trec.experiment.Experiment;
import at.medunigraz.imi.bst.trec.experiment.TrecPmRetrieval;
import at.medunigraz.imi.bst.trec.model.*;
import de.julielab.ir.goldstandards.AggregatedTrecQrelGoldStandard;
import de.julielab.ir.goldstandards.TrecPMGoldStandardFactory;
import de.julielab.ir.goldstandards.TrecQrelGoldStandard;
import de.julielab.ir.ltr.Document;
import de.julielab.java.utilities.FileUtilities;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.factory.Sets;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TrecPM1718LitRecallCrossval {

    public static final int CROSSVAL_SIZE = 5;
    private static Logger log = LogManager.getLogger();

    public static void main(String args[]) throws ConfigurationException, IOException {

        final File noClassifierTemplate = new File(
                TrecPM1718LitRecallCrossval.class.getResource("/templates/biomedical_articles/hpipubnone.json").getFile());
        final TrecPmRetrieval retrieval = new TrecPmRetrieval().withTarget(Task.PUBMED).withYear(2017).withResultsDir("myresultsdir/").withSubTemplate(noClassifierTemplate).withGeneSynonym().withDiseaseSynonym().withResistantDrugs();

        String experimentName = "Base";

        final TrecQrelGoldStandard<Topic> trecPmLit2017 = TrecPMGoldStandardFactory.pubmedOfficial2017();
        final TrecQrelGoldStandard<Topic> trecPmLit2018 = TrecPMGoldStandardFactory.pubmedOfficial2018();
        final AggregatedTrecQrelGoldStandard<Topic> aggregatedGoldStandard = new AggregatedTrecQrelGoldStandard<>(trecPmLit2017, trecPmLit2018);

        final List<List<Topic>> topicPartitioning = aggregatedGoldStandard.createStratifiedTopicPartitioning(CROSSVAL_SIZE, Topic::getDisease);


        runRecallExperiment(retrieval, experimentName, new File("recallResults"), aggregatedGoldStandard, topicPartitioning);
    }

    private static void runRecallExperiment(TrecPmRetrieval retrieval, String experimentName, File resultDir, de.julielab.ir.goldstandards.GoldStandard<Topic> aggregatedGoldStandard, List<List<Topic>> topicPartitioning) {
        List<Metrics> allESMetrics = new ArrayList<>();
        Map<Topic, Double> allRecall = new HashMap<>();
        List<Double> meanRecallPerRound = new ArrayList<>();
        for (int i = 0; i < CROSSVAL_SIZE; i++) {
            String roundName = experimentName + "-Round" + i;
            List<Topic> test = topicPartitioning.get(i);
            retrieval.withExperimentName(roundName);

            final Experiment<Topic> experiment = new Experiment(aggregatedGoldStandard, retrieval, new TopicSet(test));
            experiment.run();
            allESMetrics.add(experiment.getAllMetrics());

            final Map<Topic, ResultList<Topic>> resultListsByTopic = experiment.getLastResultListSet().stream().collect(Collectors.toMap(ResultList::getTopic, Function.identity()));
            double meanRecall = 0;
            for (Topic t : test) {
                final ResultList<Topic> resultList = resultListsByTopic.get(t);
                final Set<String> foundDocuments = resultList.getResults().stream().map(Result::getId).collect(Collectors.toSet());
                final Set<String> relevantDocuments = aggregatedGoldStandard.getQrelDocumentsForQuery(t).stream().filter(d -> d.getRelevance() > 0).map(Document::getId).collect(Collectors.toSet());
                final MutableSet<String> foundRelevantDocuments = Sets.intersect(foundDocuments, relevantDocuments);
                double recall = foundRelevantDocuments.size() / (double) relevantDocuments.size();
                meanRecall += recall;
                allRecall.put(t, recall);
            }
            meanRecall /= test.size();
            meanRecallPerRound.add(meanRecall);

        }

        for (Metrics m : allESMetrics) {
            System.out.println(m.getInfNDCG());
        }
        System.out.println("Recall:" + allRecall);
        System.out.println("Mean infNDCG: " + allESMetrics.stream().mapToDouble(Metrics::getInfNDCG).average());
        System.out.println("Mean recall: " + allRecall.values().stream().mapToDouble(d -> d).average());

        if (!resultDir.exists())
            resultDir.mkdirs();

        try(BufferedWriter bw = FileUtilities.getWriterToFile(Path.of(resultDir.getAbsolutePath(), experimentName + ".tsv").toFile())) {
            for (Topic t : allRecall.keySet()) {
                double recall = allRecall.get(t);
                bw.write(aggregatedGoldStandard.getQueryIdFunction().apply(t) + "\t" + recall);
                bw.newLine();
            }
            bw.newLine();
            bw.write("avg\t"+allRecall.values().stream().mapToDouble(d -> d).average().getAsDouble());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class RetrievalHyperParameters {
        private double disease_dismax_boost;
        private double gene_dismax_boost;
        private double disease_boost;
        private double gene_boost;
        private double disease_synonym_boost;
        private double gene_synonym_boost;
        private String disease_match_type;
        private String gene_match_type;
        private boolean withDiseaseSynonyms;
        private boolean withGeneSynonyms;
        private boolean withGeneDescription;
        private boolean withDrugInteraction;
        private String baseSimilarity;
        private String bm25_k1;
        private String bm25_b;
        private String dfr_basic_model;
        private String dfr_after_effect;
        private String dfr_normalization;
        private String dfi_independence_measure;
        private String ib_distribution;
        private String ib_lambda;
        private String ib_normalization;
        private int lmd_mu;
        private double lmjm_lambda;
    }

}
