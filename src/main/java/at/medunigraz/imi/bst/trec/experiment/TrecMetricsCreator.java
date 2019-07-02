package at.medunigraz.imi.bst.trec.experiment;

import at.medunigraz.imi.bst.trec.evaluator.SampleEval;
import at.medunigraz.imi.bst.trec.evaluator.TrecEval;
import at.medunigraz.imi.bst.trec.model.GoldStandard;
import at.medunigraz.imi.bst.trec.model.Metrics;
import at.medunigraz.imi.bst.trec.stats.CSVStatsWriter;
import at.medunigraz.imi.bst.trec.stats.XMLStatsWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Map;

public class TrecMetricsCreator {
    private static final Logger LOG = LogManager.getLogger();
    private String experimentId;
    private String longExperimentId;
    private File output;
    private File goldStandard;
    private int k;
    private boolean calculateTrecEvalWithMissingResults;
    private String statsDir;
    private GoldStandard goldStandardType;
    private File sampleGoldStandard;
    private Metrics metrics;

    public TrecMetricsCreator(String shortExperimentId, String longExperimentId, File output, File goldStandard, int k, boolean calculateTrecEvalWithMissingResults, String statsDir, GoldStandard goldStandardType, File sampleGoldStandard) {
        this.experimentId = shortExperimentId;
        this.longExperimentId = longExperimentId;
        this.output = output;
        this.goldStandard = goldStandard;
        this.k = k;
        this.calculateTrecEvalWithMissingResults = calculateTrecEvalWithMissingResults;
        this.statsDir = statsDir;
        this.goldStandardType = goldStandardType;
        this.sampleGoldStandard = sampleGoldStandard;
    }

    public Metrics computeMetrics() {
        TrecEval te = new TrecEval(goldStandard, output, k, calculateTrecEvalWithMissingResults);
        Map<String, Metrics> metricsPerTopic = te.getMetrics();

        if (sampleGoldStandard != null) {
            SampleEval se = new SampleEval(sampleGoldStandard, output);

            // TODO Refactor into MetricSet
            Map<String, Metrics> sampleEvalMetrics = se.getMetrics();
            for (Map.Entry<String, Metrics> entry : metricsPerTopic.entrySet()) {
                String topic = entry.getKey();
                if (topic == null)
                    throw new IllegalStateException("There is no evaluation result for topic " + topic + " in result file " + output.getAbsolutePath() + ". Perhaps the sample_eval.pl file has the wrong version.");
                entry.getValue().merge(sampleEvalMetrics.get(topic));
            }
        }

        File statsDirFile = new File(statsDir);
        if (!statsDirFile.exists())
            statsDirFile.mkdir();

        XMLStatsWriter xsw = new XMLStatsWriter(new File(statsDir + goldStandardType + "_" + experimentId + ".xml"));
        xsw.write(metricsPerTopic);
        xsw.close();

        CSVStatsWriter csw = new CSVStatsWriter(new File(statsDir + goldStandardType + "_" + experimentId + ".csv"));
        csw.write(metricsPerTopic);
        csw.close();

        Metrics allMetrics = metricsPerTopic.get("all");
        LOG.info("Got NDCG = {}, infNDCG = {}, P@5 = {}, P@10 = {}, P@15 = {}, R-Prec = {}, set_recall = {} for collection {}",
                allMetrics.getNDCG(), allMetrics.getInfNDCG(), allMetrics.getP5(), allMetrics.getP10(), allMetrics.getP15(), allMetrics.getRPrec(), allMetrics.getSetRecall(),
                longExperimentId);
        LOG.trace(allMetrics);
        metrics = allMetrics;
        return allMetrics;
    }

    public Metrics getMetrics() {
        if (metrics == null)
            computeMetrics();
        return metrics;
    }
}