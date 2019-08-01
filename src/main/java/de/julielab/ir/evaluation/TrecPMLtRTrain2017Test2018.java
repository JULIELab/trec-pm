package de.julielab.ir.evaluation;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.trec.evaluator.TrecWriter;
import at.medunigraz.imi.bst.trec.experiment.Experiment;
import at.medunigraz.imi.bst.trec.experiment.TrecMetricsCreator;
import at.medunigraz.imi.bst.trec.experiment.TrecPmRetrieval;
import at.medunigraz.imi.bst.trec.model.*;
import at.medunigraz.imi.bst.trec.search.ElasticClientFactory;
import cc.mallet.types.FeatureVector;
import ciir.umass.edu.learning.RANKER_TYPE;
import ciir.umass.edu.metric.METRIC;
import com.wcohen.ss.TFIDF;
import de.julielab.ir.OriginalDocumentRetrieval;
import de.julielab.ir.TfIdfManager;
import de.julielab.ir.VocabularyRestrictor;
import de.julielab.ir.goldstandards.AggregatedTrecQrelGoldStandard;
import de.julielab.ir.goldstandards.TrecPMGoldStandardFactory;
import de.julielab.ir.goldstandards.TrecQrelGoldStandard;
import de.julielab.ir.ltr.Document;
import de.julielab.ir.ltr.DocumentList;
import de.julielab.ir.ltr.RankLibRanker;
import de.julielab.ir.ltr.features.FeatureControlCenter;
import de.julielab.ir.ltr.features.FeatureNormalizationUtils;
import de.julielab.ir.ltr.features.IRScore;
import de.julielab.java.utilities.ConfigurationUtilities;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.math3.analysis.function.Exp;
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

public class TrecPMLtRTrain2017Test2018 {

    private static Logger log = LogManager.getLogger();

    public static void main(String args[]) throws ConfigurationException, IOException {

        RANKER_TYPE rType = RANKER_TYPE.COOR_ASCENT;
        METRIC trainMetric = METRIC.NDCG;
        int k = TrecConfig.SIZE;

        final String xmiTableName = "_data_xmi.documents";

        int vocabCutoff = 100;

        FeatureControlCenter.initialize(ConfigurationUtilities.loadXmlConfiguration(new File("config", "featureConfiguration.xml")));


        final TrecQrelGoldStandard<Topic> trecPmLit2017 = TrecPMGoldStandardFactory.pubmedOfficial2018();
        final TrecQrelGoldStandard<Topic> trecPmLit2018 = TrecPMGoldStandardFactory.pubmedOfficial2018();


        final File noClassifierTemplate = new File(
                TrecPMLtRTrain2017Test2018.class.getResource("/templates/biomedical_articles/hpipubnone.json").getFile());
        final File noClassifierTemplateTrain = new File(
                TrecPMLtRTrain2017Test2018.class.getResource("/templates/biomedical_articles/hpipubnone_ltrtrain.json").getFile());
        final TrecPmRetrieval retrieval = new TrecPmRetrieval(TrecConfig.ELASTIC_BA_INDEX).withResultsDir("myresultsdir/").withSubTemplate(noClassifierTemplate).withGeneSynonym().withUmlsDiseaseSynonym();
        final TrecPmRetrieval trainRetrieval = new TrecPmRetrieval(TrecConfig.ELASTIC_BA_INDEX).withResultsDir("myresultsdir/").withProperties("gsfilterfield", "inOfficial2017gs").withSubTemplate(noClassifierTemplateTrain).withGeneSynonym().withUmlsDiseaseSynonym();

        final String ltrFoldId = getLtrFoldId(0, trecPmLit2017, rType, trainMetric, k, vocabCutoff, FeatureControlCenter.getInstance().getActiveFeatureDescriptionString());
        final String vocabularyId = getVocabularyId(0, "Alltext", "Pubmed", vocabCutoff);
        final String tfidfFoldId = getTfidfFoldId(0, trecPmLit2017);
        File modelFile = Path.of("rankLibModels", ltrFoldId).toFile();
        List<Topic> test = trecPmLit2018.getQueriesAsList();
        final List<Topic> train = trecPmLit2017.getQueriesAsList();
        final DocumentList<Topic> testDocs = trecPmLit2018.getQrelDocumentsForQueries(test);
        final DocumentList<Topic> trainDocs = trecPmLit2017.getQrelDocumentsForQueries(train);

        final List<String> trainDocumentText = OriginalDocumentRetrieval.getInstance().getDocumentText(trainDocs.getSubsetWithUniqueDocumentIds(), xmiTableName).collect(Collectors.toList());
        final TFIDF trainTfIdf = TfIdfManager.getInstance().trainAndSetTfIdf(tfidfFoldId, trainDocumentText.stream());

        final Set<String> vocabulary = VocabularyRestrictor.getInstance().calculateVocabulary(vocabularyId, trainDocumentText.stream(), VocabularyRestrictor.Restriction.TFIDF, vocabCutoff);

        Experiment<Topic> trainFeatureExp = new Experiment<>(trecPmLit2017, trainRetrieval);
        trainFeatureExp.run();
        final DocumentList<Topic> list = trainFeatureExp.getLastResultAsSingleDocumentList();
        for (Document<Topic> d : list) {
            System.out.println(d.getIrScores());
        }
        System.out.println(list.stream().map(Document::getId).collect(Collectors.toSet()).size());



        FeatureControlCenter.getInstance().createFeatures(trainDocs, train, trainTfIdf, vocabulary, xmiTableName);
        FeatureControlCenter.getInstance().createFeatures(testDocs, test, trainTfIdf, vocabulary, xmiTableName);

        final double[] scalingFactors = FeatureNormalizationUtils.scaleFeatures(trainDocs.stream().map(Document::getFeatureVector).collect(Collectors.toList()));
        testDocs.forEach(d -> FeatureNormalizationUtils.rangeScaleFeatures(d.getFeatureVector(), scalingFactors));
        for (Document d : testDocs) {
            final FeatureVector fv = d.getFeatureVector();
            if (d.getRelevance() == 2)
                System.out.println(fv);
        }

        log.info("Training LtR model");
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

        retrieval.withExperimentName("pmround" + 0 + "es");

        log.info("Retrieving test documents from ElasticSearch");
        final Experiment<Topic> experiment = new Experiment(trecPmLit2018, retrieval, new TopicSet(test));
        experiment.run();

        List<DocumentList<Topic>> lastDocumentLists = experiment.getLastResultAsDocumentLists();
        log.info("Ranking test documents with the LtR model");
        for (DocumentList<Topic> list2 : lastDocumentLists) {
            FeatureControlCenter.getInstance().createFeatures(list2, trecPmLit2018.getQueriesAsList(), trainTfIdf, vocabulary, xmiTableName);
            ranker.rank(list2);
            list2.sortByScore(IRScore.LTR);
        }

        final DocumentList<Topic> l = lastDocumentLists.get(0);
        for (Document<Topic> d : l) {
            System.out.println(d.getIrScores());
        }

        log.info("Writing results");
        final File output = Path.of("myresultsdir-ltr", "pmround" + 0 + "ltr.results").toFile();
        try (final TrecWriter tw = new TrecWriter(output, "round" + 0 + "ltr")) {
            tw.writeDocuments(lastDocumentLists, IRScore.LTR, trecPmLit2018.getQueryIdFunction());
        }

        log.info("Writing qrel files for LtR evaluation");
        final File qRelFile = Path.of("aggregatedQrels", "trecPmLit2017-2018.qrel").toFile();
        trecPmLit2018.writeQrelFile(qRelFile);
        final File sampleQrelFile = Path.of("aggregatedQrels", "sampleTrecPmLit2017-2018.qrel").toFile();
        trecPmLit2018.writeSampleQrelFile(sampleQrelFile);

        log.info("Calculating scores.");
        final TrecMetricsCreator trecMetricsCreator = new TrecMetricsCreator("pmround" + 0 + "ltr", "pmround" + 0 + "ltr", output, qRelFile, TrecConfig.SIZE, false, "stats-tr/", GoldStandardType.OFFICIAL, sampleQrelFile);
        final Metrics metrics = trecMetricsCreator.computeMetrics();

        log.info("Finished.");
        OriginalDocumentRetrieval.getInstance().shutdown();
        ElasticClientFactory.getClient().close();
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
