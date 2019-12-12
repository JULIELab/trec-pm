package de.julielab.ir.ltr;

import at.medunigraz.imi.bst.trec.model.Topic;
import cc.mallet.types.Alphabet;
import cc.mallet.types.AugmentableFeatureVector;
import ciir.umass.edu.learning.DataPoint;
import ciir.umass.edu.learning.RANKER_TYPE;
import ciir.umass.edu.learning.RankList;
import ciir.umass.edu.metric.METRIC;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class RankLibRankerTest {

    @Test
    public void testConvertToRankList() throws Exception {
        // Create test documents with test features. First we need an alphabet for the feature vectors.
        Alphabet a = new Alphabet();
        a.lookupIndices(new Object[]{"feature1", "feature2", "feature3"}, true);

        // Now create the documents themselves.
        Document<Topic> d1 = new Document<>();
        d1.setId("#1");
        d1.setRelevance(2);
        AugmentableFeatureVector fv1 = new AugmentableFeatureVector(a);
        fv1.add(0, 1);
        d1.setFeatureVector(fv1);

        Document<Topic> d2 = new Document<>();
        d2.setId("#2");
        d2.setRelevance(1);
        AugmentableFeatureVector fv2 = new AugmentableFeatureVector(a);
        fv2.add(1, 1);
        fv2.add(3, 5);
        d2.setFeatureVector(fv2);

        Document<Topic> d3 = new Document<>();
        d3.setId("#3");
        d3.setRelevance(0);
        AugmentableFeatureVector fv3 = new AugmentableFeatureVector(a);
        fv3.add(2, 1);
        fv3.add(0, 7);
        fv3.add(47, 9);
        d3.setFeatureVector(fv3);

        DocumentList<Topic> dl = new DocumentList<>(Arrays.asList(d1, d2, d3));

        RankLibRanker<Topic> ranker = new RankLibRanker<>(RANKER_TYPE.LAMBDAMART, null, METRIC.NDCG, 10, null);
        Method m = RankLibRanker.class.getDeclaredMethod("convertToRankList", DocumentList.class);
        m.setAccessible(true);
        Map<String, RankList> rankListMap = (Map<String, RankList>) m.invoke(ranker, dl);

        RankList rl = rankListMap.get("<NoQueryDescriptionGiven>");
        DataPoint dp1 = rl.get(0);
        assertEquals(1, dp1.getFeatureValue(1), 0);

        DataPoint dp2 = rl.get(1);
        assertEquals(1, dp2.getFeatureValue(2), 0);
        assertEquals(5, dp2.getFeatureValue(4), 0);

        DataPoint dp3 = rl.get(2);
        assertEquals(1, dp3.getFeatureValue(3), 0);
        assertEquals(7, dp3.getFeatureValue(1), 0);
        assertEquals(9, dp3.getFeatureValue(48), 0);
    }

    @Test
    public void test() {
        // Create test documents with test features. First we need an alphabet for the feature vectors.
        Alphabet a = new Alphabet();
        a.lookupIndices(new Object[]{"feature1", "feature2", "feature3"}, true);

        // Now create the documents themselves.
        Document<Topic> d1 = new Document<>();
        d1.setId("#1");
        d1.setRelevance(2);
        AugmentableFeatureVector fv1 = new AugmentableFeatureVector(a);
        fv1.add(0, 1);
        d1.setFeatureVector(fv1);

        Document<Topic> d2 = new Document<>();
        d2.setId("#2");
        d2.setRelevance(1);
        AugmentableFeatureVector fv2 = new AugmentableFeatureVector(a);
        fv2.add(1, 1);
        d2.setFeatureVector(fv2);

        Document<Topic> d3 = new Document<>();
        d3.setId("#3");
        d3.setRelevance(0);
        AugmentableFeatureVector fv3 = new AugmentableFeatureVector(a);
        fv3.add(2, 1);
        d3.setFeatureVector(fv3);

        DocumentList<Topic> dl = new DocumentList<>(Arrays.asList(d1, d2, d3));

        RankLibRanker<Topic> ranker = new RankLibRanker<>(RANKER_TYPE.LAMBDAMART, null, METRIC.NDCG, 10, null);
        ranker.train(dl, false, 1, 1);

        double score = ranker.score(dl, METRIC.NDCG, 10);
        assertEquals(1.0, score, 0);
    }
}
