package at.medunigraz.imi.bst.trec.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

public class Metrics implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(Metrics.class);
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
        metrics.put(resolveMetricName(name), Math.abs(value));
    }

    private String resolveMetricName(String name) {
        if (name.equalsIgnoreCase("ndcg")) return "ndcg";
        if (name.equalsIgnoreCase("infndcg")) return "infNDCG";
        if (name.equalsIgnoreCase("rprec")) return "Rprec";
        if (name.equalsIgnoreCase("infap")) return "infAP";
        if (name.equalsIgnoreCase("p_5")) return "P_5";
        if (name.equalsIgnoreCase("p_10")) return "P_10";
        if (name.equalsIgnoreCase("p_15")) return "P_15";
        if (name.equalsIgnoreCase("set_f")) return "set_F";
        if (name.equalsIgnoreCase("set_recall")) return "set_recall";
        return name;
    }

    public double getMetric(String name) {
        return metrics.getOrDefault(resolveMetricName(name), 0d);
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
            log.error("Passed metrics are null", e);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Metrics metrics1 = (Metrics) o;
        return Objects.equals(metrics, metrics1.metrics);
    }

    @Override
    public int hashCode() {
        return Objects.hash(metrics);
    }
}
