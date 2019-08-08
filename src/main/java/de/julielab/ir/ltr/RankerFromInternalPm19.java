package de.julielab.ir.ltr;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.trec.experiment.TrecPmRetrieval;
import at.medunigraz.imi.bst.trec.experiment.registry.ClinicalTrialsRetrievalRegistry;
import at.medunigraz.imi.bst.trec.experiment.registry.LiteratureArticlesRetrievalRegistry;
import at.medunigraz.imi.bst.trec.model.Task;
import at.medunigraz.imi.bst.trec.model.Topic;
import at.medunigraz.imi.bst.trec.search.ElasticClientFactory;
import ciir.umass.edu.learning.RANKER_TYPE;
import ciir.umass.edu.metric.METRIC;
import de.julielab.ir.OriginalDocumentRetrieval;
import de.julielab.ir.goldstandards.AggregatedTrecQrelGoldStandard;
import de.julielab.ir.goldstandards.TrecPMGoldStandardFactory;
import de.julielab.ir.goldstandards.TrecQrelGoldStandard;
import de.julielab.ir.ltr.features.*;
import de.julielab.ir.ltr.features.features.FastTextEmbeddingFeatures;
import de.julielab.java.utilities.ConfigurationUtilities;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static de.julielab.ir.ltr.features.TrecPmQueryPart.*;

public class RankerFromInternalPm19 implements Ranker<Topic> {
    private static final Logger log = LogManager.getLogger();
    private final String xmiTableName = "_data_xmi.documents";
    private RANKER_TYPE rType = RANKER_TYPE.LAMBDAMART;
    private METRIC trainMetric = METRIC.NDCG;
    private int k = TrecConfig.SIZE;
    private int vocabCutoff = 500;
    private FeaturePreprocessing featurePreprocessing;
    private double[] scalingFactors;
    private RankLibRanker<Topic> ranker;
    private List<TrecQrelGoldStandard<Topic>> trainGoldStandards;
    private Task task;
    private DocumentList<Topic> trainDocuments;
    private File modelFile;

    public RankerFromInternalPm19() {
        try {
            task = Task.PUBMED;
            trainGoldStandards = Arrays.asList(TrecPMGoldStandardFactory.pubmedInternal2019());
            if (!FeatureControlCenter.isInitialized())
            FeatureControlCenter.initialize(ConfigurationUtilities.loadXmlConfiguration(new File("config", "featureConfiguration.xml")));
            featurePreprocessing = new FeaturePreprocessing("pubmedId.keyword", vocabCutoff, xmiTableName);
            AggregatedTrecQrelGoldStandard<Topic> gs = new AggregatedTrecQrelGoldStandard<>(trainGoldStandards);
            trainDocuments = gs.getQrelDocuments();
            modelFile = new File("rankLibModels/internalPm19-val20pct-" + rType + ".mod");
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
        final RankerFromInternalPm19 ranker = new RankerFromInternalPm19();
        ranker.trainModel();

    }

    public void trainModel() {

        train(trainDocuments);

        save(modelFile);
        FastTextEmbeddingFeatures.shutdown();
        OriginalDocumentRetrieval.getInstance().shutdown();
        ElasticClientFactory.getClient().close();
    }

    @Override
    public void train(DocumentList<Topic> documentList) {
        final TrecPmRetrieval fullRetrieval;
        String index;
        final Map<IRScoreFeatureKey, TrecPmRetrieval> subClauseRetrievals;
        if (task == Task.PUBMED) {
            fullRetrieval = LiteratureArticlesRetrievalRegistry.jlpmletor(TrecConfig.SIZE);
            index = TrecConfig.ELASTIC_BA_INDEX;
            subClauseRetrievals = IRFeaturePMRetrievals.getRetrievals(index, EnumSet.of(DISEASE, GENE, DNA, CANCER, CHEMO, NEG_BOOSTS, POS_BOOSTS));
        } else if (task == Task.CLINICAL_TRIALS) {
            fullRetrieval = ClinicalTrialsRetrievalRegistry.jlctletor(TrecConfig.SIZE);
            index = TrecConfig.ELASTIC_CT_INDEX;
            subClauseRetrievals = IRFeatureCTRetrievals.getRetrievals(index, EnumSet.of(AGE, CANCER, STRUCTURED, OTHER, DISEASE, GENE, SEX, POS_BOOSTS, DNA));
        } else throw new IllegalArgumentException("Unsupported task " + task);
        final Map<IRScoreFeatureKey, TrecPmRetrieval> m = new HashMap<>();
        // Scores for the overall query
        m.put(new IRScoreFeatureKey(IRScore.BM25, TrecPmQueryPart.FULL), fullRetrieval);
        // Scores for individual query parts
        m.putAll(subClauseRetrievals);
        featurePreprocessing.setRetrievals(m);
        featurePreprocessing.preprocessTrain(documentList, "");

        scalingFactors = FeatureNormalizationUtils.scaleFeatures(documentList.stream().map(Document::getFeatureVector).collect(Collectors.toList()));

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
        String index;
        final Map<IRScoreFeatureKey, TrecPmRetrieval> subClauseRetrievals;
        if (task == Task.PUBMED) {
            index = TrecConfig.ELASTIC_BA_INDEX;
            subClauseRetrievals = IRFeaturePMRetrievals.getRetrievals(index, EnumSet.of(DISEASE, GENE, DNA, CANCER, CHEMO, NEG_BOOSTS, POS_BOOSTS));
        } else if (task == Task.CLINICAL_TRIALS) {
            index = TrecConfig.ELASTIC_CT_INDEX;
            subClauseRetrievals = IRFeatureCTRetrievals.getRetrievals(index, EnumSet.of(AGE, CANCER, STRUCTURED, OTHER, DISEASE, GENE, SEX, POS_BOOSTS, DNA));
        } else throw new IllegalArgumentException("Unsupported task " + task);
        featurePreprocessing.setRetrievals(subClauseRetrievals);
        featurePreprocessing.preprocessTest(documentList, trainDocuments,"");
        try {
            if (ranker == null)
                load(modelFile);
            final DocumentList<Topic> rankedList = ranker.rank(documentList);
            return rankedList;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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
