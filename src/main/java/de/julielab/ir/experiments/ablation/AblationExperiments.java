package de.julielab.ir.experiments.ablation;

import at.medunigraz.imi.bst.config.TrecConfig;
import de.julielab.ir.Multithreading;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class AblationExperiments {


    private double[] requestScoreFromServer(Map<String, String> configuration, String instance, String indexSuffix, String endpoint, String metricsToReturn) throws IOException {
        double[] scores = null;
        String urlString = "http://localhost:" + TrecConfig.EVALSERVER_PORT + "/" + endpoint;
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);

        OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());

        // Create the request string
        String data = String.format("instance=%s&instance_info=none&cutoff_time=0&cutoff_length=0&seed=1&index_suffix=%s&metrics=%s", instance, indexSuffix, metricsToReturn);
        StringBuilder sb = new StringBuilder(data);
        for (String key : configuration.keySet())
            sb.append("&").append(key).append("=").append(configuration.get(key));
        writer.write(sb.toString());

        writer.flush();
        String line;
        BufferedReader reader = new BufferedReader(new
                InputStreamReader(conn.getInputStream()));
        while ((line = reader.readLine()) != null) {
            scores = Arrays.stream(line.split(",")).mapToDouble(Double::valueOf).toArray();
        }
        writer.close();
        reader.close();

        return scores;
    }


    /**
     * Issues two searches: One with the given <tt>referenceParameters</tt> and one where part of the <tt>referenceParameters</tt> is overridden with <tt>ablationOverrides</tt> for deactivate features or set some parameters to neutral values.
     *
     * @param ablationName
     * @param instance            The name of the SMAC-like instance to get the performance for (e.g. pm-split0-train)
     * @param indexSuffix         The name suffix of the index copy to use. May be the empty string if there are no index copies.
     * @param endpoint            The evaluation server endpoint to BA or CT, one of {@link de.julielab.ir.paramopt.HttpParamOptServer#GET_CONFIG_SCORE_CT} and  {@link de.julielab.ir.paramopt.HttpParamOptServer#GET_CONFIG_SCORE_PM}.
     * @param metricsToReturn
     * @param referenceParameters The search-template parameters of the reference against which ablation experiments are performed.
     * @param ablationOverrides   Parameters that will be used to override parameters in <tt>referenceParameters</tt> for the ablation.
     * @return A <tt>ComparisonPair</tt> containing the two run scores.
     * @throws IOException
     */
    private AblationComparisonPair getAblationComparison(String ablationName, String instance, String indexSuffix, String endpoint, String metricsToReturn, Map<String, String> referenceParameters, Map<String, String> ablationOverrides) throws IOException {
        double[] referenceScores = requestScoreFromServer(referenceParameters, instance, indexSuffix, endpoint, metricsToReturn);
        Map<String, String> ablationParameters = new HashMap<>(referenceParameters);
        ablationParameters.putAll(ablationOverrides);
        double[] ablationScores = requestScoreFromServer(ablationParameters, instance, indexSuffix, endpoint, metricsToReturn);
        return new AblationComparisonPair(ablationName, metricsToReturn, referenceScores, ablationScores);
    }

    /**
     * @param ablationParameterMaps Either one parameter set for each crossval split (commonly used for bottom-up ablation) or one single parameter set to be applied for all splits (for top-down ablation).
     * @param referenceParameters   The reference parameter set for each split (in top-down ablation) or a single reference parameter set (for the bottom-up baseline).
     * @param instances
     * @param indexSuffixes
     * @param metricsToReturn
     * @param endpoint
     * @return
     * @throws IOException
     */
    public Map<String, AblationCrossValResult> getAblationCrossValResult(List<Map<String, Map<String, String>>> ablationParameterMaps, List<Map<String, String>> referenceParameters, List<String> instances, List<String> indexSuffixes, String metricsToReturn, String endpoint) throws IOException {
        Map<String, AblationCrossValResult> ablationResult = new LinkedHashMap<>();
        List<Future<?>> jobs = new ArrayList<>();
        // For each cross val split...
        for (int i = 0; i < instances.size(); i++) {
            final int finalI = i;
            Future<?> future = Multithreading.getInstance().submit(() -> {
                try {
                    Map<String, Map<String, String>> ablationParameterMap = ablationParameterMaps.size() > 1 ? ablationParameterMaps.get(finalI) : ablationParameterMaps.get(0);
                    // Apply each ablation group
                    for (String ablationGroupName : ablationParameterMap.keySet()) {
                        String instance = instances.get(finalI);
                        String indexSuffix = indexSuffixes.get(finalI);
                        Map<String, String> referenceParametersForThisSplit = referenceParameters.size() > 1 ? referenceParameters.get(finalI) : referenceParameters.get(0);
                        AblationComparisonPair comparison = getAblationComparison(ablationGroupName, instance, indexSuffix, endpoint, metricsToReturn, referenceParametersForThisSplit, ablationParameterMap.get(ablationGroupName));
                        // Get the cross val result object for the current group and add the result for this cross val split
                        ablationResult.compute(ablationGroupName, (k, v) -> v == null ? new AblationCrossValResult(ablationGroupName) : v).add(comparison);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            jobs.add(future);
        }
        // Wait for the jobs to finish
        for (Future job : jobs) {
            try {
                job.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        return ablationResult;
    }

}
