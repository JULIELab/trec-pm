package de.julielab.ir.experiments.ablation;

import at.medunigraz.imi.bst.config.TrecConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class AblationExperiments {


    private double requestScoreFromServer(Map<String, String> configuration, String instance, String indexSuffix, String endpoint) throws IOException {
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


    /**
     * Issues two searches: One with the given <tt>referenceParameters</tt> and one where part of the <tt>referenceParameters</tt> is overridden with <tt>ablationOverrides</tt> for deactivate features or set some parameters to neutral values.
     * @param instance The name of the SMAC-like instance to get the performance for (e.g. pm-split0-train)
     * @param indexSuffix The name suffix of the index copy to use. May be the empty string if there are no index copies.
     * @param endpoint The evaluation server endpoint to BA or CT, one of {@link de.julielab.ir.paramopt.HttpParamOptServer#GET_CONFIG_SCORE_CT} and  {@link de.julielab.ir.paramopt.HttpParamOptServer#GET_CONFIG_SCORE_PM}.
     * @param referenceParameters The search-template parameters of the reference against which ablation experiments are performed.
     * @param ablationOverrides Parameters that will be used to override parameters in <tt>referenceParameters</tt> for the ablation.
     * @return A <tt>ComparisonPair</tt> containing the two run scores.
     * @throws IOException
     */
    private ComparisonPair getAblationComparison(String instance, String indexSuffix, String endpoint, Map<String, String> referenceParameters, Map<String, String> ablationOverrides) throws IOException {
        double referenceScore = requestScoreFromServer(referenceParameters, instance, indexSuffix, endpoint);
        Map<String, String> ablationParameters = new HashMap<>(referenceParameters);
        ablationParameters.putAll(ablationOverrides);
        double ablationScore = requestScoreFromServer(ablationParameters, instance, indexSuffix, endpoint);
        return new ComparisonPair(referenceScore, ablationScore);
    }

    private class ComparisonPair {
        private double referenceScore;
        private double ablationScore;

        public ComparisonPair(double referenceScore, double ablationScore) {
            this.referenceScore = referenceScore;
            this.ablationScore = ablationScore;
        }

        /**
         * @return Score of the reference configuration without removing features.
         */
        public double getReferenceScore() {
            return referenceScore;
        }

        /**
         * @return Score of the ablation experiment where some features were removed / neutralized in the reference.
         */
        public double getAblationScore() {
            return ablationScore;
        }
    }
}
