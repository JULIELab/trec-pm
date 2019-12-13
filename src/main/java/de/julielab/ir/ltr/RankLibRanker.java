package de.julielab.ir.ltr;

import cc.mallet.types.Alphabet;
import cc.mallet.types.FeatureVector;
import ciir.umass.edu.features.*;
import ciir.umass.edu.learning.*;
import ciir.umass.edu.metric.METRIC;
import ciir.umass.edu.metric.MetricScorer;
import ciir.umass.edu.metric.MetricScorerFactory;
import de.julielab.ir.ltr.features.IRScore;
import de.julielab.ir.ltr.features.IRScoreFeatureKey;
import de.julielab.ir.ltr.features.TrecPmQueryPart;
import de.julielab.ir.model.QueryDescription;
import de.julielab.java.utilities.FileUtilities;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RankLibRanker<Q extends QueryDescription> implements Ranker<Q> {
    private final static Logger log = LoggerFactory.getLogger(RankLibRanker.class);
    private final MetricScorerFactory metricScorerFactory;
    private ciir.umass.edu.learning.Ranker ranker;
    private RANKER_TYPE rType;
    private int[] features;
    private METRIC trainMetric;
    private int k;
    private Normalizer featureNormalizer;
    private IRScoreFeatureKey outputScoreType = new IRScoreFeatureKey(IRScore.BM25, TrecPmQueryPart.FULL);

    /**
     * <p>Creates an object that has all information to create a RankLib ranker but does not immediately do it.</p>
     * <p>The actual ranker is create by using the {@link #train(DocumentList)}</p>
     *
     * @param rType       The type of ranker to create.
     * @param features    The feature indices the ranker should be trained with and which should be used for ranking.
     * @param trainMetric The metric to be optimized for during training.
     * @param k           The top-number of documents to be used for the training metric.
     * @param normalizer  The feature normalizer to use, may be null. Possible values are <tt>sum</tt>, <tt>zscore</tt> and <tt>linear</tt>.
     */
    public RankLibRanker(RANKER_TYPE rType, int[] features, METRIC trainMetric, int k, String normalizer) {
        this.rType = rType;
        this.features = features;
        this.trainMetric = trainMetric;
        this.k = k;
        metricScorerFactory = new MetricScorerFactory();
        // This causes the RankLib datapoints to return 0f for feature values they don't have. We need this because
        // in the original RankLib, there was a static field enumerating all known features loaded within the current JVM.
        // I removed that because it is not thread safe.
        DataPoint.missingZero = true;
        if (normalizer != null) {
            if (normalizer.equalsIgnoreCase("sum"))
                featureNormalizer = new SumNormalizor();
            else if (normalizer.equalsIgnoreCase("zscore"))
                featureNormalizer = new ZScoreNormalizor();
            else if (normalizer.equalsIgnoreCase("linear"))
                featureNormalizer = new LinearNormalizer();
            else {
                throw new IllegalArgumentException("Unknown normalizer: " + normalizer);
            }
        }
    }

    public double score(DocumentList<Q> documentList, METRIC scoringMetric, int k) {
        final MetricScorer scorer = metricScorerFactory.createScorer(scoringMetric, k);
        final Map<String, RankList> rankLists = convertToRankList(documentList);
        return scorer.score(rankLists.values().stream().collect(Collectors.toList()));
    }

    @Override
    public void train(DocumentList<Q> documents) {
        log.info("Training on {} documents without validation set.", documents.size());
        final Map<String, RankList> rankLists = convertToRankList(documents);
        this.features = this.features != null ? this.features : FeatureManager.getFeatureFromSampleVector(new ArrayList(rankLists.values()));
        ranker = new RankerTrainer().train(rType, new ArrayList(rankLists.values()), features, metricScorerFactory.createScorer(trainMetric, k));
    }

    @Override
    public void train(DocumentList<Q> documents, boolean doValidation, float fraction, int randomSeed) {
        if (!doValidation)
            log.info("Training on {} documents without validation set.", documents.size());
        else
            log.info("Training on {} documents where a fraction of {} is used for training and the rest for validation. The split is done randomly with a seed of {}.", documents.size(), fraction, randomSeed);
        final Map<String, RankList> rankLists = convertToRankList(documents);
        if (featureNormalizer != null)
            rankLists.values().forEach(featureNormalizer::normalize);
        List<RankList> train;
        List<RankList> validation;
        if (doValidation) {
            final Pair<Map<String, RankList>, Map<String, RankList>> trainValData = makeValidationSplit(rankLists, fraction, randomSeed);
            train = new ArrayList(trainValData.getLeft().values());
            validation = new ArrayList<>(trainValData.getRight().values());
        } else {
            train = new ArrayList<>(rankLists.values());
            validation = Collections.emptyList();
        }

        this.features = this.features != null ? this.features : FeatureManager.getFeatureFromSampleVector(new ArrayList(rankLists.values()));
        ranker = new RankerTrainer().train(rType, train, validation, features, metricScorerFactory.createScorer(trainMetric, k));
        if (!documents.isEmpty()) {
            final Alphabet alphabet = documents.get(0).getFeatureVector().getAlphabet();
            log.trace("LtR features: " + alphabet);
        }
    }

    private Pair<Map<String, RankList>, Map<String, RankList>> makeValidationSplit(Map<String, RankList> allData, float fraction, int randomSeed) {
        if (fraction < 0 || fraction >= 1)
            throw new IllegalArgumentException("The fraction to be taken from the training data for validation is specified as " + fraction + " but it must be in [0, 1).");
        int size = (int) (fraction * allData.size());
        log.info("Splitting into training size of {} and validation size of {} queries", size, allData.size() - size);
        final List<RankList> shuffledData = new ArrayList<>(allData.values());
        Collections.shuffle(shuffledData, new Random(randomSeed));
        Map<String, RankList> train = new HashMap<>();
        Map<String, RankList> val = new HashMap<>();
        for (int i = 0; i < size; i++)
            train.put(shuffledData.get(i).getID(), shuffledData.get(i));
        for (int i = size; i < shuffledData.size(); i++)
            val.put(shuffledData.get(i).getID(), shuffledData.get(i));
        return new ImmutablePair<>(train, val);
    }

    /**
     * <p>Creates RankLib {@link SparseDataPoint} objects for each documents and groups them by query ID.</p>
     *
     * @param documents The documents to convert into the RankLib format.
     * @return A map where each query ID occurring in the input documents is mapped to the RankList made from the documents for this query ID.
     */
    private Map<String, RankList> convertToRankList(DocumentList<Q> documents) {
        final LinkedHashMap<String, List<DataPoint>> dataPointsByQueryId = documents.stream().map(d -> {
            final FeatureVector fv = d.getFeatureVector();
            if (fv == null)
                throw new IllegalArgumentException("Cannot train a ranker because the input documents have no feature vector.");
            final double[] values = fv.getValues();
            final int[] indices = fv.getIndices();

            float[] ranklibValues = new float[fv.numLocations()];
            int[] ranklibIndices = new int[fv.numLocations()];
            if (values == null) {
                // binary vector
                Arrays.fill(ranklibValues, 1f);
            } else {
                for (int i = 0; i < fv.numLocations(); i++)
                    ranklibValues[i] = (float) values[i];
            }
            for (int i = 0; i < fv.numLocations(); i++)
                // RankLib indices start counting at 1, MALLET at 0
                ranklibIndices[i] = indices[i] + 1;
            String queryId = d.getQueryDescription() != null ? d.getQueryDescription().getCrossDatasetId() : "<NoQueryDescriptionGiven>";
            DataPoint dp = new SparseDataPoint(ranklibValues, ranklibIndices, queryId, d.getRelevance());
            // The description field of the DataPoint is used to store the document ID
            dp.setDescription(d.getId());
            return dp;
        }).collect(Collectors.groupingBy(DataPoint::getID, LinkedHashMap::new, Collectors.toList()));
        final LinkedHashMap<String, RankList> rankLists = new LinkedHashMap<>();
        dataPointsByQueryId.forEach((key, value) -> rankLists.put(key, new RankList(value)));
        return rankLists;
    }

    @Override
    public void load(File modelFile) throws IOException {
        try (final BufferedReader br = FileUtilities.getReaderFromFile(modelFile)) {
            final String model = br.lines().collect(Collectors.joining(System.getProperty("line.separator")));
            ranker = new RankerFactory().loadRankerFromString(model);
        }
    }

    @Override
    public void save(File modelFile) {
        if (!modelFile.getParentFile().exists())
            modelFile.getParentFile().mkdirs();
        ranker.save(modelFile.getAbsolutePath());
    }

    /**
     * RankLib models are stored as strings listing the model parameters. This method can be used to return this exact string.
     * @return The model data.
     */
    public String getModelAsString() {
        return ranker.model();
    }

    /**
     * RankLib models are stored as string listing the model parameters. Such a string can be retrieved from {@link #getModelAsString()}.
     * Passing that string to this method sets the ranker to the given parameters.
     * @param modelString The model data.
     */
    public void loadFromString(String modelString) {
        ranker = new RankerFactory().loadRankerFromString(modelString);
    }

    @Override
    public DocumentList<Q> rank(DocumentList<Q> documents) {
        final DocumentList<Q> ret = new DocumentList<>();

        Map<String, Document> docsById = documents.stream().collect(Collectors.toMap(d -> d.getQueryDescription().getCrossDatasetId() + d.getId(), Function.identity()));

        if (docsById.size() != documents.size())
            throw new IllegalArgumentException("The passed document do not have unique IDs. The input document list has size " + documents + ", its ID map form only " + docsById.size());

        final Map<String, RankList> rankLists = convertToRankList(documents);
        if (featureNormalizer != null)
            rankLists.values().forEach(featureNormalizer::normalize);
        for (RankList rl : rankLists.values()) {
            for (int i = 0; i < rl.size(); i++) {
                final DataPoint dp = rl.get(i);
                final double score = ranker.eval(dp);
                final String docId = dp.getDescription();
                final Document doc = docsById.get(dp.getID() + docId);
                doc.setScore(outputScoreType, score);
                ret.add(doc);
            }
        }

        Collections.sort(ret, Comparator.<Document<Q>>comparingDouble(d -> d.getIrScore(outputScoreType)).reversed());

//        final List<RankList> resultRankLists = ranker.rank(new ArrayList(rankLists.values()));
//        for (RankList rl : resultRankLists) {
//            for (int i = 0; i < rl.size(); i++) {
//                DataPoint dp = rl.get(i);
//                final String docId = dp.getDescription();
//                final Document doc = docsById.get(dp.getID() + docId);
//                doc.setScore(outputScoreType, dp.getCached());
//                ret.add(doc);
//            }
//        }

        return ret;
    }

    @Override
    public IRScoreFeatureKey getOutputScoreType() {
        return outputScoreType;
    }

    @Override
    public void setOutputScoreType(IRScoreFeatureKey outputScoreType) {

        this.outputScoreType = outputScoreType;
    }

    public ciir.umass.edu.learning.Ranker getRankLibRanker() {
        return ranker;
    }


}
