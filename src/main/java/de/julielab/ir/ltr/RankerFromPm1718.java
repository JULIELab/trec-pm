package de.julielab.ir.ltr;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.trec.experiment.TrecPmRetrieval;
import at.medunigraz.imi.bst.trec.experiment.registry.LiteratureArticlesRetrievalRegistry;
import at.medunigraz.imi.bst.trec.model.Topic;
import at.medunigraz.imi.bst.trec.search.ElasticClientFactory;
import ciir.umass.edu.learning.RANKER_TYPE;
import ciir.umass.edu.metric.METRIC;
import de.julielab.ir.OriginalDocumentRetrieval;
import de.julielab.ir.goldstandards.AggregatedTrecQrelGoldStandard;
import de.julielab.ir.goldstandards.TrecPMGoldStandardFactory;
import de.julielab.ir.ltr.features.*;
import de.julielab.ir.ltr.features.features.FastTextEmbeddingFeatures;
import de.julielab.java.utilities.ConfigurationUtilities;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public class RankerFromPm1718 implements Ranker<Topic> {
    private static final Logger log = LogManager.getLogger();
    private final String xmiTableName = "_data_xmi.documents";
    private RANKER_TYPE rType = RANKER_TYPE.LAMBDAMART;
    private METRIC trainMetric = METRIC.NDCG;
    private int k = TrecConfig.SIZE;
    private int vocabCutoff = 500;
    private FeaturePreprocessing featurePreprocessing;
    private double[] scalingFactors;
    private RankLibRanker<Topic> ranker;

    public RankerFromPm1718() {
        try {
            FeatureControlCenter.initialize(ConfigurationUtilities.loadXmlConfiguration(new File("config", "featureConfiguration.xml")));
            featurePreprocessing = new FeaturePreprocessing("pubmedId.keyword", vocabCutoff, xmiTableName);
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
    }

    /**
     * For training.
     *
     * @param args
     */
    public static void main(String args[]) {
        final RankerFromPm1718 rankerFromPm1718 = new RankerFromPm1718();
        rankerFromPm1718.trainModel();

    }

    public void trainModel() {
        AggregatedTrecQrelGoldStandard<Topic> gs1718 = new AggregatedTrecQrelGoldStandard<>(TrecPMGoldStandardFactory.pubmedOfficial2017(), TrecPMGoldStandardFactory.pubmedOfficial2018());
        train(gs1718.getQrelDocuments());
        save(new File("rankLibModels/pm1718-val20pct.mod"));
        FastTextEmbeddingFeatures.shutdown();
        OriginalDocumentRetrieval.getInstance().shutdown();
        ElasticClientFactory.getClient().close();
    }

    @Override
    public void train(DocumentList<Topic> documentList) {
        final TrecPmRetrieval hpipubnone = LiteratureArticlesRetrievalRegistry.hpipubnone(k);
        final Map<IRScoreFeatureKey, TrecPmRetrieval> m = new HashMap<>();
        // Scores for the overall query
        m.put(new IRScoreFeatureKey(IRScore.BM25, TrecPmQueryPart.FULL), hpipubnone);
        // Scores for individual query parts
        m.putAll(IRFeaturePMRetrievals.getRetrievals(TrecConfig.ELASTIC_BA_INDEX, EnumSet.allOf(TrecPmQueryPart.class)));
        featurePreprocessing.setRetrievals(m);
        featurePreprocessing.preprocessTrain(documentList, "");

       // scalingFactors = FeatureNormalizationUtils.scaleFeatures(documentList.stream().map(Document::getFeatureVector).collect(Collectors.toList()));

        log.info("Training LtR model");
        ranker = new RankLibRanker<>(rType, null, trainMetric, k, null);
        long time = System.currentTimeMillis();
        ranker.train(documentList, true, 0.8f, 1);
        time = System.currentTimeMillis() - time;
        log.info("Training of ranker {} on {} documents took {}ms ({}minutes)", rType, documentList.size(), time, time / 1000 / 60);
    }

    @Override
    public void train(DocumentList<Topic> documents, boolean doValidation, float fraction, int randomSeed) {
        ranker.train(documents, doValidation, fraction, randomSeed);
    }

    @Override
    public void load(File modelFile) throws IOException {
        ranker = new RankLibRanker<>(rType, null, trainMetric, k, null);
        ranker.load(modelFile);
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(modelFile.getAbsolutePath() + ".scalingFactors.bin"))) {
            scalingFactors = (double[]) ois.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void save(File modelFile) {
        ranker.save(modelFile);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(modelFile.getAbsolutePath() + ".scalingFactors.bin"))) {
            oos.writeObject(scalingFactors);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public DocumentList rank(DocumentList<Topic> documentList) {
        featurePreprocessing.setRetrievals(IRFeaturePMRetrievals.getRetrievals(TrecConfig.ELASTIC_BA_INDEX, EnumSet.allOf(TrecPmQueryPart.class)));
        featurePreprocessing.preprocessTest(documentList, "");
        final DocumentList<Topic> rankedList = ranker.rank(documentList);
        return rankedList;
    }

    @Override
    public IRScoreFeatureKey getOutputScoreType() {
        return ranker.getOutputScoreType();
    }

    @Override
    public void setOutputScoreType(IRScoreFeatureKey outputScoreType) {
        ranker.setOutputScoreType(outputScoreType);
    }
}
