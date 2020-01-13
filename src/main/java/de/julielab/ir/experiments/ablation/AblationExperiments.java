package de.julielab.ir.experiments.ablation;

import at.medunigraz.imi.bst.config.TrecConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class AblationExperiments {
    private double requestScoreFromServer(Map<String, String> configuration,  String instance, String indexSuffix, String endpoint) throws IOException {
        String urlString = "http://localhost:" + TrecConfig.EVALSERVER_PORT + "/" + endpoint;
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);

        OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());

        // Create the request string
        String data = String.format("instance=%s&instance_info=none&cutoff_time=0&cutoff_length=0&seed=1&index_suffix=$indexSuffix", instance, indexSuffix);
        StringBuilder sb = new StringBuilder(data);
        for (String key : configuration.keySet())
            sb.append("&").append(key).append("=").append(configuration.get(key));
        writer.write(sb.toString());

        writer.flush();
        String line;
        BufferedReader reader = new BufferedReader(new
                InputStreamReader(conn.getInputStream()));
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
        writer.close();
        reader.close();

    return 0;
    }
}
