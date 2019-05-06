package de.julielab.ir.ltr;

import cc.mallet.types.FeatureVector;
import ciir.umass.edu.features.FeatureManager;
import ciir.umass.edu.learning.*;
import ciir.umass.edu.metric.METRIC;
import ciir.umass.edu.metric.MetricScorerFactory;
import de.julielab.ir.ltr.features.FeatureControlCenter;
import de.julielab.ir.model.QueryDescription;
import de.julielab.java.utilities.FileUtilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RankLibRanker<Q extends QueryDescription> implements Ranker<Q> {

    private final MetricScorerFactory metricScorerFactory;
    private ciir.umass.edu.learning.Ranker ranker;
    private RANKER_TYPE rType;
    private int[] features;
    private METRIC trainMetric;
    private int k;

    /**
     * <p>Creates an object that has all information to create a RankLib ranker but does not immediately do it.</p>
     * <p>The actual ranker is create by using the {@link #train(DocumentList)}</p>
     *
     * @param rType       The type of ranker to create.
     * @param features    The feature indices the ranker should be trained with and which should be used for ranking.
     * @param trainMetric The metric to be optimized for during training.
     * @param k           The top-number of documents to be used for the training metric.
     */
    public RankLibRanker(RANKER_TYPE rType, int[] features, METRIC trainMetric, int k) {
        this.rType = rType;
        this.features = features;
        this.trainMetric = trainMetric;
        this.k = k;
        metricScorerFactory = new MetricScorerFactory();
    }

    @Override
    public void train(DocumentList<Q> documents) {
        final Map<String, RankList> rankLists = convertToRankList(documents);
        this.features = this.features != null ? this.features : FeatureManager.getFeatureFromSampleVector(new ArrayList(rankLists.values()));
        ranker = new RankerTrainer().train(rType, new ArrayList(rankLists.values()), features, metricScorerFactory.createScorer(trainMetric, k));
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

            // In RankLib, the 0 index is unused.
            float[] ranklibValues = new float[indices.length + 1];
            int[] ranklibIndices = new int[indices.length + 1];
            if (values == null) {
                // binary vector
                Arrays.fill(ranklibValues, 1f);
            } else {
                for (int i = 0; i < values.length; i++)
                    ranklibValues[i + 1] = (float) values[i];
            }
            System.arraycopy(indices, 0, ranklibIndices, 1, indices.length);
            DataPoint dp = new SparseDataPoint(ranklibValues, ranklibIndices, d.getQueryDescription().getCrossDatasetId(), d.getRelevance());
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
            ranker.loadFromString(br.lines().collect(Collectors.joining(System.getProperty("line.separator"))));
        }
    }

    @Override
    public void save(File modelFile) {
        ranker.save(modelFile.getAbsolutePath());
    }

    @Override
    public DocumentList rank(DocumentList<Q> documents) {
        final DocumentList<QueryDescription> ret = new DocumentList<>();

        Map<String, Document> docsById = documents.stream().collect(Collectors.toMap(d -> d.getQueryDescription().getCrossDatasetId() + d.getId(), Function.identity()));

        if (docsById.size() != documents.size())
            throw new IllegalArgumentException("The passed document do not have unique IDs. The input document list has size " + documents + ", its ID map form only " + docsById.size());

        final Map<String, RankList> rankLists = convertToRankList(documents);
        final List<RankList> resultRankLists = ranker.rank(new ArrayList(rankLists.values()));
        for (RankList rl : resultRankLists) {
            for (int i = 0; i < rl.size(); i++) {
                DataPoint dp = rl.get(i);
                final String docId = dp.getDescription();
                ret.add(docsById.get(dp.getID() + docId));
            }
        }
        return ret;
    }

    public ciir.umass.edu.learning.Ranker getRankLibRanker() {
        return ranker;
    }


}
