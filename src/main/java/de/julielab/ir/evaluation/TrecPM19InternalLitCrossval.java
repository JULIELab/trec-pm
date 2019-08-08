package de.julielab.ir.evaluation;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.trec.evaluator.TrecWriter;
import at.medunigraz.imi.bst.trec.experiment.Experiment;
import at.medunigraz.imi.bst.trec.experiment.TrecMetricsCreator;
import at.medunigraz.imi.bst.trec.experiment.TrecPmRetrieval;
import at.medunigraz.imi.bst.trec.model.*;
import ciir.umass.edu.learning.RANKER_TYPE;
import ciir.umass.edu.metric.METRIC;
import com.wcohen.ss.TFIDF;
import de.julielab.ir.OriginalDocumentRetrieval;
import de.julielab.ir.TfIdfManager;
import de.julielab.ir.VocabularyRestrictor;
import de.julielab.ir.goldstandards.GoldStandard;
import de.julielab.ir.goldstandards.TrecPMGoldStandardFactory;
import de.julielab.ir.ltr.Document;
import de.julielab.ir.ltr.DocumentList;
import de.julielab.ir.ltr.RankLibRanker;
import de.julielab.ir.ltr.features.FeatureControlCenter;
import de.julielab.ir.ltr.features.IRScore;
import de.julielab.ir.ltr.features.IRScoreFeatureKey;
import de.julielab.ir.ltr.features.TrecPmQueryPart;
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

public class TrecPM19InternalLitCrossval {

    public static final int CROSSVAL_SIZE = 5;
    private static Logger log = LogManager.getLogger();

    public static void main(String args[]) throws ConfigurationException, IOException {

        RANKER_TYPE rType = RANKER_TYPE.COOR_ASCENT;
        METRIC trainMetric = METRIC.NDCG;
        int k = TrecConfig.SIZE;

        int vocabCutoff = 50;

        String xmiTableName = "_data_xmi.documents";

        FeatureControlCenter.initialize(ConfigurationUtilities.loadXmlConfiguration(new File("config", "featureConfiguration.xml")));


        final GoldStandard<Topic> gs = TrecPMGoldStandardFactory.pubmedInternal2019();

        final List<List<Topic>> topicPartitioning = gs.createRandomizedQueryPartitioning(CROSSVAL_SIZE, 1);

        final File noClassifierTemplate = new File(
                TrecPM19InternalLitCrossval.class.getResource("/templates/biomedical_articles/hpipubnone.json").getFile());
        final TrecPmRetrieval retrieval = new TrecPmRetrieval(TrecConfig.ELASTIC_BA_INDEX).withResultsDir("myresultsdir/").withSubTemplate(noClassifierTemplate).withGeneSynonym().withUmlsDiseaseSynonym();

        List<Double> rankLibScores = new ArrayList<>();
        List<Metrics> allESMetrics = new ArrayList<>();
        List<Metrics> allLtrMetrics = new ArrayList<>();
        for (int i = 0; i < CROSSVAL_SIZE; i++) {
            final String ltrFoldId = getLtrFoldId(i, gs, rType, trainMetric, k, vocabCutoff, FeatureControlCenter.getInstance().getActiveFeatureDescriptionString());
            final String vocabularyId = getVocabularyId(i, "Alltext", "Pubmed", vocabCutoff);
            final String tfidfFoldId = getTfidfFoldId(i, gs);
            File modelFile = Path.of("rankLibModels", ltrFoldId).toFile();
            log.info("Crossval round {}", ltrFoldId);
            int thisround = i;
            List<Topic> test = topicPartitioning.get(i);
            final List<Topic> train = IntStream.range(0, CROSSVAL_SIZE).filter(round -> round != thisround).mapToObj(topicPartitioning::get).flatMap(Collection::stream).collect(Collectors.toList());
            final DocumentList<Topic> testDocs = gs.getQrelDocumentsForQueries(test);
            final DocumentList<Topic> trainDocs = gs.getQrelDocumentsForQueries(train);

            final List<String> trainDocumentText = OriginalDocumentRetrieval.getInstance().getDocumentText(trainDocs.getSubsetWithUniqueDocumentIds(), xmiTableName).collect(Collectors.toList());
            final TFIDF trainTfIdf = TfIdfManager.getInstance().trainAndSetTfIdf(tfidfFoldId, trainDocumentText.stream());

            final Set<String> vocabulary = VocabularyRestrictor.getInstance().calculateVocabulary(vocabularyId, trainDocumentText.stream(), VocabularyRestrictor.Restriction.TFIDF, vocabCutoff);
            FeatureControlCenter.getInstance().createFeatures(trainDocs, trainTfIdf, vocabulary, xmiTableName);
            FeatureControlCenter.getInstance().createFeatures(testDocs, trainTfIdf, vocabulary, xmiTableName);

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

            final Experiment<Topic> experiment = new Experiment(gs, retrieval, new TopicSet(test));
            allESMetrics.add(experiment.run());

            final List<ResultList<Topic>> lastResultListSet = experiment.getLastResultListSet();
            List<DocumentList<Topic>> lastDocumentLists = new ArrayList<>();
            for (ResultList<Topic> list : lastResultListSet) {
                final DocumentList<Topic> documents = new DocumentList<>();
                for (Result r : list.getResults()) {
                    final Document<Topic> doc = new Document<>();
                    doc.setId(r.getId());
                    doc.setScore(new IRScoreFeatureKey(IRScore.BM25, TrecPmQueryPart.FULL), r.getScore());
                    doc.setQueryDescription(list.getTopic());
                    documents.add(doc);
                }
                lastDocumentLists.add(documents);
            }

            for (DocumentList<Topic> list : lastDocumentLists) {
                FeatureControlCenter.getInstance().createFeatures(list, trainTfIdf, vocabulary, xmiTableName);
                ranker.rank(list);
            }
            final File output = Path.of("myresultsdir-ltr", "pmround" + i + "ltr.results").toFile();
            try (final TrecWriter tw = new TrecWriter(output, "round" + i + "ltr")) {
                tw.writeDocuments(lastDocumentLists, new IRScoreFeatureKey(IRScore.LTR, TrecPmQueryPart.FULL), gs.getQueryIdFunction());
            }


            final File qRelFile = Path.of("qrelsTemp", "trecPmLitInternal2019.qrel").toFile();
            gs.writeQrelFile(qRelFile);
            final File sampleQrelFile = Path.of("qrelsTemp", "sampleTrecPmLitInternal2019.qrel").toFile();
            gs.writeSampleQrelFile(sampleQrelFile);

            final TrecMetricsCreator trecMetricsCreator = new TrecMetricsCreator("pmround" + i + "ltr", "pmround" + i + "ltr", output, qRelFile, TrecConfig.SIZE, false, "stats-tr/", GoldStandardType.INTERNAL, sampleQrelFile);
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
        return "Ltr-Fold:" + fold + "-Goldstandard:" + goldStandard.getDatasetId() + "-Rankertype:" + rType + "-Trainmetric:" + trainMetric + "MetricK:" + k + "-Cutoff:" + cutoff + "-Features:" + featureConfig;
    }

    private static String getTfidfFoldId(int fold, de.julielab.ir.goldstandards.GoldStandard<?> goldStandard) {
        return "Tfidf-Fold:" + fold + "-Goldstandard:" + goldStandard.getDatasetId();
    }

}
