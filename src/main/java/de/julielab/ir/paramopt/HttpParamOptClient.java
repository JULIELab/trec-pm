package de.julielab.ir.paramopt;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.trec.model.Metrics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class HttpParamOptClient {
    public static Map<String, Metrics> requestScoreFromServer(Map<String, String> configuration, String instance, String indexSuffix, String endpoint, String metricsToReturn, boolean metricsPerTopic) throws IOException {
        String urlString = "http://localhost:" + TrecConfig.EVALSERVER_PORT + "/" + endpoint;
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);

        OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());

        // Create the request string
        String data = String.format("instance=%s&instance_info=none&cutoff_time=0&cutoff_length=0&seed=1&index_suffix=%s&metrics=%s&metrics_per_topic=%s", instance, indexSuffix, metricsToReturn, metricsPerTopic);
        StringBuilder sb = new StringBuilder(data);
        for (String key : configuration.keySet())
            sb.append("&").append(key).append("=").append(configuration.get(key));
        writer.write(sb.toString());

        writer.flush();
        String line;
        BufferedReader reader = new BufferedReader(new
                InputStreamReader(conn.getInputStream()));
        Map<String, Metrics> metrics = new HashMap<>();
        while ((line = reader.readLine()) != null) {
            // allinf,allrprec,allp10;topic29:29inf,29rprec,29p10;topic3:3inf,3rprec,3p10;...
            String[] metricParts = line.split(";");
            metrics.put("all", parseMetrics(metricParts[0], metricsToReturn));

            for (int i = 1; i < metricParts.length; i++) {
                String[] metricsForTopic = metricParts[i].split(":");
                metrics.put(metricsForTopic[0], parseMetrics(metricsForTopic[1], metricsToReturn));
            }
        }
        writer.close();
        reader.close();

        return metrics;
    }

    private static Metrics parseMetrics(String csvMetrics, String metricsToReturn) {
        Metrics ret = new Metrics();
        String[] metricNames = metricsToReturn.split(",");
        String[] numbers = csvMetrics.split(",");
        for (int i = 0; i < numbers.length; i++) {
            String metricName = metricNames[i];
            String number = numbers[i];
            ret.put(metricName, Double.valueOf(number));
        }
        return ret;
    }
}
