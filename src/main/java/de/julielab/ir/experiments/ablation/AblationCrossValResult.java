package de.julielab.ir.experiments.ablation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalDouble;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

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

    @Override
    public String toString() {
        List<Object[]> lines = new ArrayList<>();
        lines.add(new Object[]{"name", "reference", "ablation"});
        for (AblationComparisonPair comparison : this) {
            lines.add(new Object[]{comparison.getAblationName(), comparison.getReferenceScore(), comparison.getAblationScore()});
        }
        return lines.stream().map(line -> Arrays.stream(line).map(String::valueOf).collect(Collectors.joining("\t"))).collect(Collectors.joining(System.getProperty("line.separator")));
    }
}
