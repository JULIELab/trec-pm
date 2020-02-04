package at.medunigraz.imi.bst.trec.experiment;

import at.medunigraz.imi.bst.trec.evaluator.EvaluationCommandFailedException;
import at.medunigraz.imi.bst.trec.evaluator.SampleEval;
import at.medunigraz.imi.bst.trec.evaluator.TrecEval;
import at.medunigraz.imi.bst.trec.model.GoldStandardType;
import at.medunigraz.imi.bst.trec.model.Metrics;
import at.medunigraz.imi.bst.trec.stats.CSVStatsWriter;
import at.medunigraz.imi.bst.trec.stats.XMLStatsWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

public class TrecMetricsCreator {
    private static final Logger LOG = LoggerFactory.getLogger(TrecMetricsCreator.class);
    private String experimentId;
    private String longExperimentId;
    private File results;
    private File goldStandard;
    private int k;
    private boolean calculateTrecEvalWithMissingResults;
    private String statsDir;
    private GoldStandardType goldStandardType;
    private File sampleGoldStandard;
    private Metrics metrics;
    private Map<String, Metrics> metricsPerTopic;

    public TrecMetricsCreator(String shortExperimentId, String longExperimentId, File results, File goldStandard, int k, boolean calculateTrecEvalWithMissingResults, String statsDir, GoldStandardType goldStandardType, File sampleGoldStandard) {
        this.experimentId = shortExperimentId;
        this.longExperimentId = longExperimentId;
        this.results = results;
        this.goldStandard = goldStandard;
        this.k = k;
        this.calculateTrecEvalWithMissingResults = calculateTrecEvalWithMissingResults;
        this.statsDir = statsDir;
        this.goldStandardType = goldStandardType;
        this.sampleGoldStandard = sampleGoldStandard;
    }

    public Map<String, Metrics> getMetricsPerTopic() {
        return metricsPerTopic;
    }

    public Metrics computeMetrics() {
        final String filename = goldStandardType + "_" + experimentId;
        final File trecEvalOutput = new File(statsDir, filename + ".trec_eval");
        try {
            TrecEval te = new TrecEval(goldStandard, results, trecEvalOutput, k, calculateTrecEvalWithMissingResults);
            metricsPerTopic = te.getMetrics();

            if (sampleGoldStandard != null) {

                final File samplevalOutput = new File(statsDir, filename + ".sampleval");
                SampleEval se = new SampleEval(sampleGoldStandard, results, samplevalOutput);

                // TODO Refactor into MetricSet
                Map<String, Metrics> sampleEvalMetrics = se.getMetrics();
                for (Map.Entry<String, Metrics> entry : metricsPerTopic.entrySet()) {
                    String topic = entry.getKey();
                    if (topic == null)
                        throw new IllegalStateException("There is no evaluation result for topic " + topic + " in result file " + results.getAbsolutePath() + ". Perhaps the sample_eval.pl file has the wrong version.");
                    entry.getValue().merge(sampleEvalMetrics.get(topic));
                }
            }

            File statsDirFile = new File(statsDir);
            if (!statsDirFile.exists())
                statsDirFile.mkdir();

            XMLStatsWriter xsw = new XMLStatsWriter(new File(statsDir + filename + ".xml"));
            xsw.write(metricsPerTopic);
            xsw.close();

            CSVStatsWriter csw = new CSVStatsWriter(new File(statsDir + filename + ".csv"));
            csw.write(metricsPerTopic);
            csw.close();

            Metrics allMetrics = metricsPerTopic.get("all");
            LOG.info("Got NDCG = {}, infNDCG = {},  P@5 = {}, P@10 = {}, P@15 = {}, R-Prec = {}, set_recall = {} for collection {}",
                    allMetrics.getNDCG(), allMetrics.getInfNDCG(), allMetrics.getP5(), allMetrics.getP10(), allMetrics.getP15(), allMetrics.getRPrec(), allMetrics.getSetRecall(),
                    longExperimentId);
            metrics = allMetrics;
            LOG.trace("All metrics: {}", allMetrics);
            return allMetrics;
        } catch (EvaluationCommandFailedException e) {
            LOG.warn("Evaluation of collection {} failed due to evaluation command error {}", longExperimentId, e.getMessage());
            return Metrics.ZERO;
        }
    }

    public Metrics getMetrics() {
        if (metrics == null)
            computeMetrics();
        return metrics;
    }
}
