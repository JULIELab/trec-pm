package de.julielab.ir.experiments.ablation;

import at.medunigraz.imi.bst.trec.model.Metrics;
import de.julielab.ir.paramopt.HttpParamOptClient;

import java.io.Serializable;
import java.util.*;

public class AblationComparisonPair implements Serializable {
    private String ablationName;
    private Map<String, Metrics> referenceScores = new HashMap<>();
    private Map<String, Metrics> ablationScores = new HashMap<>();

    public AblationComparisonPair(String ablationName, String metrics, double[] referenceScores, double[] ablationScores) {
        this.ablationName = ablationName;
        this.referenceScores.put("all", new Metrics());
        this.ablationScores.put("all", new Metrics());
        List<String> metricList = Arrays.asList(metrics.split(","));
        for (int i = 0; i < metricList.size(); i++) {
            String metricName = metricList.get(i);
            this.referenceScores.get("all").put(metricName, referenceScores[i]);
            this.ablationScores.get("all").put(metricName, ablationScores[i]);
        }
    }

    public AblationComparisonPair(String ablationName, Map<String, Metrics> referenceMetrics, Map<String, Metrics> ablationMetrics) {
        this.ablationName = ablationName;
        this.referenceScores = referenceMetrics;
        this.ablationScores = ablationMetrics;
    }

    /**
     * @return Score of the reference configuration without removing features.
     */
    public Metrics getReferenceScores() {
        return referenceScores.get("all");
    }

    public Map<String, Metrics> getReferenceMetrics() {
        return referenceScores;
    }

    public Map<String, Metrics> getAblationMetrics() {
        return ablationScores;
    }

    public Metrics getReferenceScores(String topic) {
        return referenceScores.get(topic);
    }

    public double getReferenceScore(String metric) {
        return referenceScores.get("all").getMetric(metric);
    }

    public double getReferenceScore(String topic, String metric) {
        return referenceScores.get(topic).getMetric(metric);
    }


    public void setReferenceScore(double value, String metric) {
        referenceScores.get("all").put(metric, value);
    }

    public void setAblationScore(double value, String metric) {
        ablationScores.get("all").put(metric, value);
    }

    /**
     * @return Score of the ablation experiment where some features were removed / neutralized in the reference.
     */
    public Metrics getAblationScores() {
        return ablationScores.get("all");
    }

    public Metrics getAblationScores(String topic) {
        return ablationScores.get(topic);
    }

    public double getAblationScore(String metric) {
        return ablationScores.get("all").getMetric(metric);
    }

    public double getAblationScore(String topic, String metric) {
        return ablationScores.get(topic).getMetric(metric);
    }

    public String getAblationName() {
        return ablationName;
    }
}