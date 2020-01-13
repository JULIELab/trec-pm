package de.julielab.ir.experiments.ablation;

import java.util.ArrayList;
import java.util.OptionalDouble;
import java.util.function.ToDoubleFunction;

public class AblationCrossValResult extends ArrayList<AblationComparisonPair> {
    public double getMeanReferenceScore() {
        return getMeanScore(AblationComparisonPair::getReferenceScore);
    }

    public double getMeanAblationScore() {
        return getMeanScore(AblationComparisonPair::getAblationScore);
    }

    public double getReferenceScoreVariance() {
        return getVariance(AblationComparisonPair::getReferenceScore);
    }

    public double getAblationScoreVariance() {
        return getVariance(AblationComparisonPair::getAblationScore);
    }

    public double getReferenceStandardDeviation() {
        return Math.sqrt(getReferenceScoreVariance());
    }
    public double getAblationStandardDeviation() {
        return Math.sqrt(getAblationScoreVariance());
    }

    /**
     * This computes the sample variance (dividing by N-1 instead of N).
     * @param scoreFun
     * @return
     */
    private double getVariance(ToDoubleFunction<AblationComparisonPair> scoreFun) {
        double meanScore = getMeanScore(scoreFun);
        return stream().mapToDouble(c -> Math.pow(scoreFun.applyAsDouble(c) - meanScore, 2)/(size()-1)).sum();
    }

    private double getMeanScore(ToDoubleFunction<AblationComparisonPair> scoreFun) {
        OptionalDouble avgOpt = stream().mapToDouble(scoreFun).average();
        if (avgOpt.isPresent())
            return avgOpt.getAsDouble();
        return 0;
    }
}
