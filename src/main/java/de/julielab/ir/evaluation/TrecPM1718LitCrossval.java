package de.julielab.ir.evaluation;

import at.medunigraz.imi.bst.trec.PubmedExperimenter;
import at.medunigraz.imi.bst.trec.evaluator.TrecWriter;
import at.medunigraz.imi.bst.trec.experiment.Experiment;
import at.medunigraz.imi.bst.trec.experiment.TrecMetricsCreator;
import at.medunigraz.imi.bst.trec.experiment.TrecPmRetrieval;
import at.medunigraz.imi.bst.trec.model.*;
import at.medunigraz.imi.bst.trec.stats.CSVStatsWriter;
import ciir.umass.edu.learning.RANKER_TYPE;
import ciir.umass.edu.metric.METRIC;
import com.wcohen.ss.TFIDF;
import de.julielab.ir.OriginalDocumentRetrieval;
import de.julielab.ir.TfIdfManager;
import de.julielab.ir.VocabularyRestrictor;
import de.julielab.ir.goldstandards.AggregatedTrecQrelGoldStandard;
import de.julielab.ir.goldstandards.TrecQrelGoldStandard;
import de.julielab.ir.ltr.Document;
import de.julielab.ir.ltr.DocumentList;
import de.julielab.ir.ltr.RankLibRanker;
import de.julielab.ir.ltr.features.FeatureControlCenter;
import de.julielab.ir.ltr.features.IRScore;
import de.julielab.java.utilities.ConfigurationUtilities;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class TrecPM1718LitCrossval {

    public static final int CROSSVAL_SIZE = 5;
    private static Logger log = LogManager.getLogger();

    public static void main(String args[]) throws ConfigurationException, IOException {

        RANKER_TYPE rType = RANKER_TYPE.COOR_ASCENT;
        METRIC trainMetric = METRIC.NDCG;
        int k = 1000;

        int vocabCutoff = 50;

        FeatureControlCenter.initialize(ConfigurationUtilities.loadXmlConfiguration(new File("config", "featureConfiguration.xml")));


        File topicsFile2017 = new File(CSVStatsWriter.class.getResource("/topics/topics2017.xml").getPath());
        final TopicSet topics2017 = new TopicSet(topicsFile2017, Challenge.TREC_PM, Task.PUBMED, 2017);
        File topicsFile2018 = new File(CSVStatsWriter.class.getResource("/topics/topics2018.xml").getPath());
        final TopicSet topics2018 = new TopicSet(topicsFile2018, Challenge.TREC_PM, Task.PUBMED, 2018);

        final TrecQrelGoldStandard<Topic> trecPmLit2017 = new TrecQrelGoldStandard<>(Challenge.TREC_PM, Task.PUBMED, 2017, topics2017.getTopics(), Path.of("src", "main", "resources", "gold-standard", "qrels-treceval-abstracts.2017.txt").toFile(), Path.of("src", "main", "resources", "gold-standard", "sample-qrels-final-abstracts.2017.txt").toFile());
        final TrecQrelGoldStandard<Topic> trecPmLit2018 = new TrecQrelGoldStandard<>(Challenge.TREC_PM, Task.PUBMED, 2018, topics2018.getTopics(), Path.of("src", "main", "resources", "gold-standard", "qrels-treceval-abstracts.2018.txt").toFile(), Path.of("src", "main", "resources", "gold-standard", "qrels-sample-abstracts.2018.txt").toFile());
        final AggregatedTrecQrelGoldStandard<Topic> aggregatedGoldStandard = new AggregatedTrecQrelGoldStandard<>(Path.of("aggregatedQrels", "trecPmLit2017-2018.qrel").toFile(), Path.of("aggregatedQrels", "sampleTrecPmLit2017-2018.qrel").toFile(), trecPmLit2017, trecPmLit2018);

        final List<List<Topic>> topicPartitioning = aggregatedGoldStandard.createStratifiedTopicPartitioning(CROSSVAL_SIZE, Topic::getDisease);

        final File noClassifierTemplate = new File(
                PubmedExperimenter.class.getResource("/templates/biomedical_articles/hpipubnone.json").getFile());
        final TrecPmRetrieval retrieval = new TrecPmRetrieval().withTarget(Task.PUBMED).withGoldStandard(GoldStandard.OFFICIAL).withYear(2017).withResultsDir("myresultsdir/").withSubTemplate(noClassifierTemplate).withGeneSynonym().withDiseaseSynonym();

        List<Double> rankLibScores = new ArrayList<>();
        List<Metrics> allESMetrics = new ArrayList<>();
        List<Metrics> allLtrMetrics = new ArrayList<>();
        for (int i = 0; i < CROSSVAL_SIZE; i++) {
            final String ltrFoldId = getLtrFoldId(i, aggregatedGoldStandard, rType, trainMetric, k, vocabCutoff, FeatureControlCenter.getInstance().getActiveFeatureDescriptionString());
            final String vocabularyId = getVocabularyId(i, "Alltext", "Pubmed", vocabCutoff);
            final String tfidfFoldId = getTfidfFoldId(i, aggregatedGoldStandard);
            File modelFile = Path.of("rankLibModels", ltrFoldId).toFile();
            log.info("Crossval round {}", ltrFoldId);
            int thisround = i;
            List<Topic> test = topicPartitioning.get(i);
            final List<Topic> train = IntStream.range(0, CROSSVAL_SIZE).filter(round -> round != thisround).mapToObj(topicPartitioning::get).flatMap(Collection::stream).collect(Collectors.toList());
            final DocumentList<Topic> testDocs = aggregatedGoldStandard.getQrelDocumentsForQueries(test);
            final DocumentList<Topic> trainDocs = aggregatedGoldStandard.getQrelDocumentsForQueries(train);

            final List<String> trainDocumentText = OriginalDocumentRetrieval.getInstance().getDocumentText(trainDocs.getSubsetWithUniqueDocumentIds()).collect(Collectors.toList());
            final TFIDF trainTfIdf = TfIdfManager.getInstance().trainAndSetTfIdf(tfidfFoldId, trainDocumentText.stream());

            final Set<String> vocabulary = VocabularyRestrictor.getInstance().calculateVocabulary(vocabularyId, trainDocumentText.stream(), VocabularyRestrictor.Restriction.TFIDF, vocabCutoff);
            FeatureControlCenter.getInstance().createFeatures(trainDocs, train, trainTfIdf, vocabulary);
            FeatureControlCenter.getInstance().createFeatures(testDocs, test, trainTfIdf, vocabulary);

            final RankLibRanker<Topic> ranker = new RankLibRanker<>(rType, null, trainMetric, k, null);
            if (!modelFile.exists()) {
                long time = System.currentTimeMillis();
                ranker.train(trainDocs);
                time = System.currentTimeMillis() - time;
                log.info("Training of ranker {} on {} documents took {}ms ({}minutes)", rType, trainDocs.size(), time, time / 1000 / 60);
                ranker.save(modelFile);
            } else {
                ranker.load(modelFile);
            }
            final DocumentList<Topic> result = ranker.rank(testDocs);
            final double rankLibScore = ranker.score(result, METRIC.NDCG, 10);
            rankLibScores.add(rankLibScore);

            retrieval.withExperimentName("pmround" + i + "es");

            final Experiment<Topic> experiment = new Experiment();
            experiment.setGoldDataset(aggregatedGoldStandard);
            experiment.setTopicSet(new TopicSet(test));
            experiment.setRetrieval(retrieval);
            experiment.setGoldStandard(GoldStandard.OFFICIAL);
            experiment.setCalculateTrecEvalWithMissingResults(false);
            experiment.run();
            allESMetrics.add(experiment.getAllMetrics());

            final List<ResultList<Topic>> lastResultListSet = experiment.getLastResultListSet();
            List<DocumentList<Topic>> lastDocumentLists = new ArrayList<>();
            for (ResultList<Topic> list : lastResultListSet) {
                final DocumentList<Topic> documents = new DocumentList<>();
                for (Result r : list.getResults()) {
                    final Document<Topic> doc = new Document<>();
                    doc.setId(r.getId());
                    doc.setScore(IRScore.BM25, r.getScore());
                    doc.setQueryDescription(list.getTopic());
                    documents.add(doc);
                }
                lastDocumentLists.add(documents);
            }

            for (DocumentList<Topic> list : lastDocumentLists) {
                FeatureControlCenter.getInstance().createFeatures(list, Stream.concat(topics2017.getTopics().stream(), topics2018.getTopics().stream()).collect(Collectors.toList()), trainTfIdf, vocabulary);
                ranker.rank(list);
            }
            final File output = Path.of("myresultsdir-ltr", "pmround" + i + "ltr.results").toFile();
            try (final TrecWriter tw = new TrecWriter(output, "round" + i + "ltr")) {
                tw.writeDocuments(lastDocumentLists, IRScore.LTR, aggregatedGoldStandard.getQueryIdFunction());
            }


            final TrecMetricsCreator trecMetricsCreator = new TrecMetricsCreator("pmround" + i + "ltr", "pmround" + i + "ltr", output, aggregatedGoldStandard.getQrelFile(), 1000, false, "stats-tr/", GoldStandard.OFFICIAL, aggregatedGoldStandard.getSampleQrelFile());
            final Metrics metrics = trecMetricsCreator.computeMetrics();
            allLtrMetrics.add(metrics);

        }

        for (Metrics m : allESMetrics) {
            System.out.println(m.getInfNDCG());
        }
        System.out.println("Mean from ES: " + allESMetrics.stream().mapToDouble(Metrics::getInfNDCG).average());
        System.out.println("Mean from LtR: " + allLtrMetrics.stream().mapToDouble(Metrics::getInfNDCG).average());
    }

    private static String getVocabularyId(int fold, String field, String corpusType, int cutoff) {
        return "Vocab-Fold:" + fold + "-Field:" + field + "-Corpustype:" + corpusType + "-Cutoff" + cutoff;
    }

    private static String getLtrFoldId(int fold, de.julielab.ir.goldstandards.GoldStandard<?> goldStandard, RANKER_TYPE rType, METRIC trainMetric, int k, int cutoff, String featureConfig) {
        return "Ltr-Fold:" + fold + "-Goldstandard:" + goldStandard.getDatasetId() + "-Rankertype:" + rType + "-Trainmetric:" + trainMetric + "MetricK:" + k + "-Cutoff:"+cutoff+"-Features:"+featureConfig;
    }

    private static String getTfidfFoldId(int fold, de.julielab.ir.goldstandards.GoldStandard<?> goldStandard) {
        return "Tfidf-Fold:" + fold + "-Goldstandard:" + goldStandard.getDatasetId();
    }

}
