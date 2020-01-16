package de.julielab.ir.experiments.ablation;

import java.util.Arrays;
import java.util.List;

public class AblationComparisonPair {
    private String ablationName;
    private double[] referenceScores;
    private double[] ablationScores;
    private List<String> metrics;

    public AblationComparisonPair(String ablationName, String metrics, double[] referenceScores, double[] ablationScores) {
        this.ablationName = ablationName;
        this.referenceScores = referenceScores;
        this.ablationScores = ablationScores;
        this.metrics = Arrays.asList(metrics.split(","));
    }

    /**
     * @return Score of the reference configuration without removing features.
     */
    public double[] getReferenceScores() {
        return referenceScores;
    }

    public double getReferenceScore(String metric) {
        int index = getMetricIndex(metric);
        return referenceScores[index];
    }

    private int getMetricIndex(String metric) {
        int index = metrics.indexOf(metric);
        if (index < 0)
            throw new IllegalArgumentException("This object dos not contain a value for the metric " + metric);
        return index;
    }

    public void setReferenceScore(double value, String metric) {
        int index = getMetricIndex(metric);
        referenceScores[index] = value;
    }

    public void setAblationScore(double value, String metric) {
        int index = getMetricIndex(metric);
        ablationScores[index] = value;
    }

    /**
     * @return Score of the ablation experiment where some features were removed / neutralized in the reference.
     */
    public double[] getAblationScores() {
        return ablationScores;
    }

    public double getAblationScore(String metric) {
        int index = getMetricIndex(metric);
        return ablationScores[index];
    }

    public List<String> getMetrics() {
        return metrics;
    }

    public String getAblationName() {
        return ablationName;
    }
}