package at.medunigraz.imi.bst.trec.model;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class Metrics {


    public static final Metrics ZERO = new Metrics();

    static {
        ZERO.put("ndcg", 0);
        ZERO.put("infNDCG", 0);
        ZERO.put("Rprec", 0);
        ZERO.put("infAP", 0);
        ZERO.put("P_5", 0);
        ZERO.put("P_10", 0);
        ZERO.put("P_15", 0);
        ZERO.put("set_F", 0);
        ZERO.put("set_recall", 0);
    }

    private Map<String, Double> metrics = new TreeMap<>();

    public void put(String name, double value) {
        metrics.put(name, value);
    }

    public double getMetric(String name) {
        return metrics.getOrDefault(name, 0d);
    }

    public boolean hasMetric(String name) {
        return metrics.containsKey(name);
    }

    public String getMetricsAsString() {
        StringBuilder sb = new StringBuilder();

        Set<Map.Entry<String, Double>> entries = metrics.entrySet();
        for (Map.Entry<String, Double> entry : entries) {
            sb.append(entry.getKey());
            sb.append("=");
            sb.append(entry.getValue());
            sb.append("\n");
        }

        return sb.toString();
    }

    public void merge(Metrics b) {
        // TODO check for duplicate keys, e.g. "infAP"
        try {
            metrics.putAll(b.metrics);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public double getNDCG() {
        return getMetric("ndcg");
    }

    public double getInfNDCG() {
        return getMetric("infNDCG");
    }

    public double getRPrec() {
        return getMetric("Rprec");
    }

    public double getInfAP() {
        return getMetric("infAP");
    }

    public double getP5() {
        return getMetric("P_5");
    }

    public double getP10() {
        return getMetric("P_10");
    }

    public double getP15() {
        return getMetric("P_15");
    }

    public double getF() {
        return getMetric("set_F");
    }

    public double getSetRecall() {
        return getMetric("set_recall");
    }
}
