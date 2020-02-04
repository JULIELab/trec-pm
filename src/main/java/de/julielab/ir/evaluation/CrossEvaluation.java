package de.julielab.ir.evaluation;

import at.medunigraz.imi.bst.trec.model.Metrics;
import de.julielab.ir.experiments.ablation.AblationComparisonPair;
import de.julielab.ir.experiments.ablation.AblationCrossValResult;
import de.julielab.ir.paramopt.HttpParamOptClient;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CrossEvaluation {
    public static AblationCrossValResult runEval(List<Map<String, String>> configurations, String instanceFmtStr, String indexSuffixFmtStr, String endpoint, String metricsToReturn, boolean metricsPerTopic) throws IOException {
        AblationCrossValResult reference = new AblationCrossValResult("reference");
        for (int i = 0; i < configurations.size(); i++) {
            String instance = String.format(instanceFmtStr, i);
            String indexSuffix = String.format(indexSuffixFmtStr, i);
            Map<String, Metrics> metrics = HttpParamOptClient.requestScoreFromServer(configurations.get(0), instance, indexSuffix, endpoint, metricsToReturn, metricsPerTopic);
            reference.add(new AblationComparisonPair("reference",  metrics, null));
        }
        return reference;
    }
}
