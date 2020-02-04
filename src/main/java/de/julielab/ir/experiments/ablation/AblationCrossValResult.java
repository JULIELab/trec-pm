package de.julielab.ir.experiments.ablation;

import java.util.ArrayList;
import java.util.OptionalDouble;
import java.util.function.ToDoubleFunction;

public class AblationCrossValResult extends ArrayList<AblationComparisonPair> {
    private String ablationGroupName;

    public AblationCrossValResult(String ablationGroupName) {
        this.ablationGroupName = ablationGroupName;
    }

    public String getAblationGroupName() {
        return ablationGroupName;
    }

    public double getMeanReferenceScore(String metric) {
        return getMeanScore(p -> p.getReferenceScore(metric));
    }

    public double getMeanAblationScore(String metric) {
        return getMeanScore(p -> p.getAblationScore(metric));
    }

    public double getReferenceScoreVariance(String metric) {
        return getVariance(p -> p.getReferenceScore(metric));
    }

    public double getAblationScoreVariance(String metric) {
        return getVariance(p -> p.getAblationScore(metric));
    }

    public double getReferenceStandardDeviation(String metric) {
        return Math.sqrt(getReferenceScoreVariance(metric));
    }

    public double getAblationStandardDeviation(String metric) {
        return Math.sqrt(getAblationScoreVariance(metric));
    }

    /**
     * This computes the sample variance (dividing by N-1 instead of N).
     *
     * @param scoreFun
     * @return
     */
    private double getVariance(ToDoubleFunction<AblationComparisonPair> scoreFun) {
        double meanScore = getMeanScore(scoreFun);
        return stream().mapToDouble(c -> Math.pow(scoreFun.applyAsDouble(c) - meanScore, 2) / (size() - 1)).sum();
    }

    private double getMeanScore(ToDoubleFunction<AblationComparisonPair> scoreFun) {
        OptionalDouble avgOpt = stream().mapToDouble(scoreFun).average();
        if (avgOpt.isPresent())
            return avgOpt.getAsDouble();
        return 0;
    }

//    @Override
//    public String toString() {
//        List<List<Object>> lines = new ArrayList<>();
//        List<String> metrics = Collections.emptyList();
//        if (!isEmpty())
//            metrics = get(0).getMetrics();
//        lines.add(Arrays.asList("name", "reference", "ablation"));
//        for (AblationComparisonPair comparison : this) {
//            lines.add(new Object[]{comparison.getAblationName(), comparison.getReferenceScore(), comparison.getAblationScore()});
//        }
//        return lines.stream().map(line -> Arrays.stream(line).map(String::valueOf).collect(Collectors.joining("\t"))).collect(Collectors.joining(System.getProperty("line.separator")));
//    }
}
