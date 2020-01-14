package de.julielab.ir.experiments.ablation;

import org.assertj.core.data.Offset;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
public class AblationCrossValResultTest {

    private static AblationCrossValResult result;

    @BeforeClass
    public static void setup() {
        result = new AblationCrossValResult();
        result.add(new AblationComparisonPair("1", 0.8, 0.1));
        result.add(new AblationComparisonPair("2", 0.2, 0.5));
        result.add(new AblationComparisonPair("3", 0.6, 0.7));
    }
    @Test
    public void getMeanReferenceScore() {
        assertThat(result.getMeanReferenceScore()).isCloseTo(0.533, Offset.offset(0.001));
    }

    @Test
    public void getMeanAblationScore() {
        assertThat(result.getMeanAblationScore()).isCloseTo(0.433, Offset.offset(0.001));
    }

    @Test
    public void getReferenceStandardDeviation() {
        assertThat(result.getReferenceStandardDeviation()).isCloseTo(0.305505, Offset.offset(0.000001));
    }

    @Test
    public void getAblationStandardDeviation() {
        assertThat(result.getAblationStandardDeviation()).isCloseTo(0.305505, Offset.offset(0.000001));
    }
}