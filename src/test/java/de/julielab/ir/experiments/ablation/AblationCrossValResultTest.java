package de.julielab.ir.experiments.ablation;

import de.julielab.ir.paramopt.HttpParamOptServer;
import org.assertj.core.data.Offset;
import org.junit.BeforeClass;
import org.junit.Test;

import static de.julielab.ir.paramopt.HttpParamOptServer.INFNDCG;
import static de.julielab.ir.paramopt.HttpParamOptServer.METRICS;
import static org.assertj.core.api.Assertions.assertThat;
public class AblationCrossValResultTest {

    private static AblationCrossValResult result;

    @BeforeClass
    public static void setup() {
        result = new AblationCrossValResult();
        result.add(new AblationComparisonPair("1", INFNDCG, new double[]{0.8}, new double[]{0.1}));
        result.add(new AblationComparisonPair("2", INFNDCG, new double[]{0.2}, new double[]{0.5}));
        result.add(new AblationComparisonPair("3", INFNDCG, new double[]{0.6}, new double[]{0.7}));
    }
    @Test
    public void getMeanReferenceScore() {
        assertThat(result.getMeanReferenceScore(INFNDCG)).isCloseTo(0.533, Offset.offset(0.001));
    }

    @Test
    public void getMeanAblationScore() {
        assertThat(result.getMeanAblationScore(INFNDCG)).isCloseTo(0.433, Offset.offset(0.001));
    }

    @Test
    public void getReferenceStandardDeviation() {
        assertThat(result.getReferenceStandardDeviation(INFNDCG)).isCloseTo(0.305505, Offset.offset(0.000001));
    }

    @Test
    public void getAblationStandardDeviation() {
        assertThat(result.getAblationStandardDeviation(INFNDCG)).isCloseTo(0.305505, Offset.offset(0.000001));
    }
}