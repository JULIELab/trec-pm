package at.medunigraz.imi.bst.trec.evaluator;

import at.medunigraz.imi.bst.trec.model.Metrics;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public abstract class AbstractEvaluator implements Evaluator {

    private static final Logger LOG = LogManager.getLogger();

    private static final Set<String> STRING_METRICS = new HashSet<>();

    static {
        STRING_METRICS.add("runid");
        STRING_METRICS.add("relstring");
    }

    protected Map<String, Metrics> metricsPerTopic = new TreeMap<>();

    protected File goldStandard, results, output;

    private static final String TARGET = "all";

    public AbstractEvaluator(File goldStandard, File results, File output) {
        this.goldStandard = goldStandard;
        this.results = results;
        this.output = output;
    }

    protected String[] collectStream(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        List<String> list = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            LOG.trace(line);
            list.add(line);
        }

        String[] ret = new String[list.size()];
        return list.toArray(ret);
    }

    protected void parseOutput(String[] output) {
        for (String s : output) {
            String[] fields = s.split("\\s+");

            // Rprec	all	0.0000
            if (fields.length != 3) {
                continue;
            }

            String metricName = fields[0];
            String topic = fields[1];
            String metricValue = fields[2];

            // We ignore metrics with string values
            if (STRING_METRICS.contains(metricName)) {
                continue;
            }

            if (!metricsPerTopic.containsKey(topic)) {
                metricsPerTopic.put(topic, new Metrics());
            }

            Metrics old = metricsPerTopic.get(topic);
            old.put(metricName, Double.valueOf(metricValue));
            metricsPerTopic.put(topic, old);
        }
    }

    public abstract List<String> getFullCommand();

    @Override
    public void evaluate() {
        String command = String.join(" ", getFullCommand());
        LOG.debug(command);

        ProcessBuilder pb = new ProcessBuilder(getFullCommand());
        pb.redirectOutput(output);

        Process proc = null;
        String[] error = null;
        String[] output = new String[0];
        try {
            proc = pb.start();
            error = collectStream(proc.getErrorStream());
            proc.waitFor(10, TimeUnit.SECONDS);
            output = FileUtils.readLines(this.output, "UTF-8").toArray(output);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int exit = proc.exitValue();
        if (exit != 0) {
            LOG.error(String.format("Process exited with code %d and error message:", exit));
            for (String e : error) {
                LOG.error(e);
            }
            throw new IllegalStateException("Could not run the evaluation.");
        }

        parseOutput(output);
    }

    public String getMetricsAsString() {
        StringBuilder sb = new StringBuilder();

        Set<Map.Entry<String, Metrics>> entries = metricsPerTopic.entrySet();
        for (Map.Entry<String, Metrics> entry : entries) {
            sb.append("\n");
            sb.append("Topic: ");
            sb.append(entry.getKey());
            sb.append("\n");
            sb.append(entry.getValue().getMetricsAsString());
        }

        return sb.toString();
    }

    public Map<String, Metrics> getMetrics() {
        return this.metricsPerTopic;
    }

    public double getMetricByTopic(String topic, String metric) {
        return metricsPerTopic.get(topic).getMetric(metric);
    }

    public Metrics getMetricsByTopic(String topic) {
        return metricsPerTopic.get(topic);
    }

    @Override
    public double getNDCG() {
        return getMetricByTopic(TARGET, "ndcg");
    }

    @Override
    public double getRPrec() {
        return getMetricByTopic(TARGET, "Rprec");
    }

    @Override
    public double getInfAP() {
        return getMetricByTopic(TARGET, "infAP");
    }

    @Override
    public double getP10() {
        return getMetricByTopic(TARGET, "P_10");
    }

    @Override
    public double getF() {
        return getMetricByTopic(TARGET, "set_F");
    }

    public double getInfNDCG() {
        return getMetricByTopic(TARGET, "infNDCG");
    }
}
