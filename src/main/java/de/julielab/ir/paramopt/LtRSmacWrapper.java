package de.julielab.ir.paramopt;

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
import de.julielab.ir.goldstandards.TrecPMGoldStandardFactory;
import de.julielab.ir.goldstandards.TrecQrelGoldStandard;
import de.julielab.ir.ltr.Document;
import de.julielab.ir.ltr.DocumentList;
import de.julielab.ir.ltr.RankLibRanker;
import de.julielab.ir.ltr.features.*;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static de.julielab.ir.paramopt.RCConstants.RETRIEVAL_PARAMETERS;
import static de.julielab.ir.paramopt.RCConstants.TEMPLATE;
import static de.julielab.java.utilities.ConfigurationUtilities.slash;

public class LtRSmacWrapper extends SmacWrapperBase {
    private final static Logger log = LoggerFactory.getLogger(LtRSmacWrapper.class);
    private final TrecQrelGoldStandard<Topic> gs;
    private final String documentTable;
    int vocabCutoff = 50;
    private int nPartitions = 10;
    private RANKER_TYPE rType = RANKER_TYPE.COOR_ASCENT;
    private METRIC trainMetric = METRIC.NDCG;
    private int k = TrecConfig.SIZE;

    public static void main(String args[]) throws ConfigurationException {
        new LtRSmacWrapper().parseAndRunConfiguration(args);
    }

    public LtRSmacWrapper() {
        gs = TrecPMGoldStandardFactory.pubmedInternal2019();
        this.documentTable = "_data_xmi.documents";
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

    @Override
    protected double calculateScore(HierarchicalConfiguration<ImmutableNode> config, String instance, int seed) {
        FeatureControlCenter.initialize(config.configurationAt(FCConstants.LTRFEATURES));
        final List<List<Topic>> splits = gs.createStratifiedQueryPartitioning(nPartitions, t -> t.getDisease());
        int splitNum = Integer.valueOf(instance.replace("crossval-", ""));

        return calculateScoreForSplit(config, splitNum, splits);
    }

    protected double calculateScoreForSplit(HierarchicalConfiguration<ImmutableNode> config, int splitNum, List<List<Topic>> splits) {
        try {
            List<Topic> test = splits.get(splitNum);
            final List<Topic> train = IntStream.range(0, nPartitions).filter(round -> round != splitNum).mapToObj(splits::get).flatMap(Collection::stream).collect(Collectors.toList());


            final String ltrFoldId = getLtrFoldId(splitNum, gs, rType, trainMetric, k, vocabCutoff, FeatureControlCenter.getInstance().getActiveFeatureDescriptionString());
            final String vocabularyId = getVocabularyId(splitNum, "Alltext", "Pubmed", vocabCutoff);
            final String tfidfFoldId = getTfidfFoldId(splitNum, gs);
            File modelFile = Path.of("rankLibModels", ltrFoldId).toFile();

            final DocumentList<Topic> testDocs = gs.getQrelDocumentsForQueries(test);


            final Triple<RankLibRanker<Topic>, Set<String>, TFIDF> rankerAndVocabulary = trainRanker(test, train, vocabularyId, tfidfFoldId, modelFile, testDocs);
            RankLibRanker<Topic> ranker = rankerAndVocabulary.getLeft();
            Set<String> vocabulary = rankerAndVocabulary.getMiddle();
            TFIDF trainTfIdf = rankerAndVocabulary.getRight();

            String template = config.getString(slash(RETRIEVAL_PARAMETERS, TEMPLATE));
            final TrecPmRetrieval retrieval = new TrecPmRetrieval(TrecConfig.ELASTIC_BA_INDEX).withResultsDir("myresultsdir/").withSubTemplate(template).withGeneSynonym().withUmlsDiseaseSynonym();
            final DocumentList<Topic> result = ranker.rank(testDocs);

            retrieval.withExperimentName("pmround" + splitNum + "es");

            final Experiment<Topic> experiment = new Experiment(gs, retrieval, new TopicSet(test));

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
                FeatureControlCenter.getInstance().createFeatures(list, trainTfIdf, vocabulary, documentTable);
                ranker.rank(list);
            }
            final File output = Path.of("myresultsdir-ltr", "pmround" + splitNum + "ltr.results").toFile();
            try (final TrecWriter tw = new TrecWriter(output, "round" + splitNum + "ltr")) {
                tw.writeDocuments(lastDocumentLists, new IRScoreFeatureKey(IRScore.LTR, TrecPmQueryPart.FULL), gs.getQueryIdFunction());
            }

            final File qRelFile = Path.of("aggregatedQrels", "trecPmLit2017-2018.qrel").toFile();
            gs.writeQrelFile(qRelFile);
            final File sampleQrelFile = Path.of("aggregatedQrels", "sampleTrecPmLit2017-2018.qrel").toFile();
            gs.writeSampleQrelFile(sampleQrelFile);

            final TrecMetricsCreator trecMetricsCreator = new TrecMetricsCreator("pmround" + splitNum + "ltr", "pmround" + splitNum + "ltr", output, qRelFile, TrecConfig.SIZE, false, "stats-tr/", GoldStandardType.OFFICIAL, sampleQrelFile);
            final Metrics metrics = trecMetricsCreator.computeMetrics();

            return metrics.getInfNDCG();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Triple<RankLibRanker<Topic>, Set<String>, TFIDF> trainRanker(List<Topic> test, List<Topic> train, String vocabularyId, String tfidfFoldId, File modelFile, DocumentList<Topic> testDocs) throws IOException {
        final DocumentList<Topic> trainDocs = gs.getQrelDocumentsForQueries(train);
        final List<String> trainDocumentText = OriginalDocumentRetrieval.getInstance().getDocumentText(trainDocs.getSubsetWithUniqueDocumentIds(), documentTable).collect(Collectors.toList());
        final TFIDF trainTfIdf = TfIdfManager.getInstance().trainAndSetTfIdf(tfidfFoldId, trainDocumentText.stream());
        final Set<String> vocabulary = VocabularyRestrictor.getInstance().calculateVocabulary(vocabularyId, trainDocumentText.stream(), VocabularyRestrictor.Restriction.TFIDF, vocabCutoff);
        FeatureControlCenter.getInstance().createFeatures(testDocs,  trainTfIdf, vocabulary, documentTable);
        FeatureControlCenter.getInstance().createFeatures(trainDocs, trainTfIdf, vocabulary, documentTable);
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
        return new ImmutableTriple<>(ranker, vocabulary, trainTfIdf);
    }
}
