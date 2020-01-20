package de.julielab.ir.evaluation;

import de.julielab.ir.experiments.ablation.AblationComparisonPair;
import de.julielab.ir.experiments.ablation.AblationCrossValResult;
import de.julielab.ir.paramopt.HttpParamOptClient;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CrossEvaluation {
    public static AblationCrossValResult runEval(List<Map<String, String>> configurations, String instanceFmtStr, String indexSuffixFmtStr, String endpoint, String metricsToReturn) throws IOException {
        AblationCrossValResult reference = new AblationCrossValResult("reference");
        for (int i = 0; i < configurations.size(); i++) {
            String instance = String.format(instanceFmtStr, i);
            String indexSuffix = String.format(indexSuffixFmtStr, i);
            double[] doubles = HttpParamOptClient.requestScoreFromServer(configurations.get(0), instance, indexSuffix, endpoint, metricsToReturn);
            reference.add(new AblationComparisonPair("reference", metricsToReturn, doubles, new double[doubles.length]));
        }
        return reference;
    }
}
