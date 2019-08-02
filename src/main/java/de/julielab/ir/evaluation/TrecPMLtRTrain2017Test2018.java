package de.julielab.ir.evaluation;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.retrieval.Retrieval;
import at.medunigraz.imi.bst.trec.evaluator.TrecWriter;
import at.medunigraz.imi.bst.trec.experiment.Experiment;
import at.medunigraz.imi.bst.trec.experiment.TrecMetricsCreator;
import at.medunigraz.imi.bst.trec.experiment.TrecPmRetrieval;
import at.medunigraz.imi.bst.trec.model.*;
import at.medunigraz.imi.bst.trec.search.ElasticClientFactory;
import ciir.umass.edu.learning.RANKER_TYPE;
import ciir.umass.edu.metric.METRIC;
import com.wcohen.ss.TFIDF;
import de.julielab.ir.OriginalDocumentRetrieval;
import de.julielab.ir.TfIdfManager;
import de.julielab.ir.VocabularyRestrictor;
import de.julielab.ir.goldstandards.TrecPMGoldStandardFactory;
import de.julielab.ir.goldstandards.TrecQrelGoldStandard;
import de.julielab.ir.ltr.Document;
import de.julielab.ir.ltr.DocumentList;
import de.julielab.ir.ltr.RankLibRanker;
import de.julielab.ir.ltr.features.FeatureControlCenter;
import de.julielab.ir.ltr.features.FeatureNormalizationUtils;
import de.julielab.ir.ltr.features.FeaturePreprocessing;
import de.julielab.ir.ltr.features.IRScore;
import de.julielab.java.utilities.ConfigurationUtilities;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class TrecPMLtRTrain2017Test2018 {

    private static Logger log = LogManager.getLogger();

    public static void main(String args[]) throws ConfigurationException, IOException {

        RANKER_TYPE rType = RANKER_TYPE.LAMBDAMART;
        METRIC trainMetric = METRIC.NDCG;
        int k = TrecConfig.SIZE;

        final String xmiTableName = "_data_xmi.documents";

        int vocabCutoff = 500;

        final File noClassifierTemplate = new File(
                TrecPMLtRTrain2017Test2018.class.getResource("/templates/biomedical_articles/hpipubnone.json").getFile());
        final TrecPmRetrieval retrieval = new TrecPmRetrieval(TrecConfig.ELASTIC_BA_INDEX).withResultsDir("myresultsdir/").withSubTemplate(noClassifierTemplate).withGeneSynonym().withUmlsDiseaseSynonym().withWordRemoval();
        FeatureControlCenter.initialize(ConfigurationUtilities.loadXmlConfiguration(new File("config", "featureConfiguration.xml")));
        final FeaturePreprocessing featurePreprocessing = new FeaturePreprocessing("pubmedId.keyword", vocabCutoff, xmiTableName);

        final Map<IRScore, TrecPmRetrieval> m = new HashMap<>();
        m.put(IRScore.BM25, retrieval);
        featurePreprocessing.setRetrievals(m);


        final TrecQrelGoldStandard<Topic> trecPmLit2017 = TrecPMGoldStandardFactory.pubmedOfficial2018();
        final TrecQrelGoldStandard<Topic> trecPmLit2018 = TrecPMGoldStandardFactory.pubmedOfficial2018();



        final String ltrFoldId = getLtrFoldId(0, trecPmLit2017, rType, trainMetric, k, vocabCutoff, FeatureControlCenter.getInstance().getActiveFeatureDescriptionString());
        File modelFile = Path.of("rankLibModels", ltrFoldId).toFile();
        List<Topic> test = trecPmLit2018.getQueriesAsList();
        final List<Topic> train = trecPmLit2017.getQueriesAsList();
        final DocumentList<Topic> trainDocs = trecPmLit2017.getQrelDocumentsForQueries(train);

        featurePreprocessing.preprocessTrain(trainDocs, "");

        final double[] scalingFactors = FeatureNormalizationUtils.scaleFeatures(trainDocs.stream().map(Document::getFeatureVector).collect(Collectors.toList()));

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

        retrieval.withExperimentName("pmround" + 0 + "es");

        log.info("Retrieving test documents from ElasticSearch");
        final Experiment<Topic> experiment = new Experiment(trecPmLit2018, retrieval, new TopicSet(test));
        experiment.run();

        List<DocumentList<Topic>> lastDocumentLists = experiment.getLastResultAsDocumentLists();
        log.info("Ranking test documents with the LtR model");
        for (DocumentList<Topic> list2 : lastDocumentLists) {
            featurePreprocessing.preprocessTest(list2, "");
            list2.stream().map(Document::getFeatureVector).forEach(fv -> FeatureNormalizationUtils.rangeScaleFeatures(fv, scalingFactors));
            ranker.rank(list2);
        }

        log.info("Writing results");
        final File output = Path.of("myresultsdir-ltr", "pmround" + 0 + "ltr.results").toFile();
        try (final TrecWriter tw = new TrecWriter(output, "round" + 0 + "ltr")) {
            tw.writeDocuments(lastDocumentLists, ranker.getOutputScoreType(), trecPmLit2018.getQueryIdFunction());
        }

        log.info("Writing qrel files for LtR evaluation");
        final File qRelFile = Path.of("results-ltr", "trecPmLit2018.qrel").toFile();
        trecPmLit2018.writeQrelFile(qRelFile);
        final File sampleQrelFile = Path.of("results-ltr", "sampleTrecPmLit2018.qrel").toFile();
        trecPmLit2018.writeSampleQrelFile(sampleQrelFile);

        log.info("Calculating scores.");
        final TrecMetricsCreator trecMetricsCreator = new TrecMetricsCreator("pmround" + 0 + "ltr", "pmround" + 0 + "ltr", output, qRelFile, TrecConfig.SIZE, false, "stats-tr/", GoldStandardType.OFFICIAL, sampleQrelFile);
        trecMetricsCreator.computeMetrics();

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
