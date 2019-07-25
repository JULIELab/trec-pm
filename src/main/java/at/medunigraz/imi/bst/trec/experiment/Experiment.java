package at.medunigraz.imi.bst.trec.experiment;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.retrieval.Query;
import at.medunigraz.imi.bst.retrieval.Retrieval;
import at.medunigraz.imi.bst.trec.model.Metrics;
import at.medunigraz.imi.bst.trec.model.ResultList;
import at.medunigraz.imi.bst.trec.model.TopicSet;
import de.julielab.ir.goldstandards.GoldStandard;
import de.julielab.ir.model.QueryDescription;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class Experiment<Q extends QueryDescription> extends Thread {

    private static final Logger LOG = LogManager.getLogger();
    public Metrics allMetrics = null;
    private Retrieval<?, Q> retrieval;
    private GoldStandard goldDataset;
    private String statsDir = "stats/";
    private String resultsDir = "results/";
    private TopicSet topicSet;
    private boolean calculateTrecEvalWithMissingResults = true;
    private int k = TrecConfig.SIZE;
    private List<ResultList<Q>> lastResultListSet;

    /**
     * Build an Experiment using the topics provided by the gold standard.
     *
     * @param goldStandard
     * @param retrieval
     */
    public Experiment(GoldStandard goldStandard, Retrieval retrieval) {
        this(goldStandard, retrieval, new TopicSet(goldStandard.getQueriesAsList()));
    }

    /**
     * Build an Experiment using the topics provided.
     * @param goldStandard
     * @param retrieval
     * @param topics
     */
    public Experiment(GoldStandard goldStandard, Retrieval retrieval, TopicSet topics) {
        this.goldDataset = goldStandard;
        this.retrieval = retrieval;
        this.topicSet = topics;
    }


    public Retrieval getRetrieval() {
        return retrieval;
    }

    public void setRetrieval(Retrieval retrieval) {
        this.retrieval = retrieval;
    }

    public String getStatsDir() {
        return statsDir;
    }

    public void setStatsDir(String statsDir) {
        this.statsDir = statsDir.endsWith(File.separator) ? statsDir : statsDir + File.separator;
    }

    public void setResultsDir(String resultsDir) {
        this.resultsDir = resultsDir.endsWith(File.separator) ? resultsDir : resultsDir + File.separator;
    }

    public String getExperimentId() {
        return retrieval.getExperimentId();
    }

    public TopicSet getTopicSet() {
        return topicSet;
    }

    public void setTopicSet(TopicSet topicSet) {
        this.topicSet = topicSet;
    }

    public boolean isCalculateTrecEvalWithMissingResults() {
        return calculateTrecEvalWithMissingResults;
    }

    /**
     * <p>For the trec_eval script, specify if non-existing result entries should count as 0 in the 'all' performance values.</p>
     * <p>The sample_eval.pl script does not allow a setting here and always works as if this setting would be set to <tt>false</tt>.</p>
     *
     * @param calculateTrecEvalWithMissingResults Whether or not to calculate the evaluation scores including or excluding missing result documents.
     */
    public void setCalculateTrecEvalWithMissingResults(boolean calculateTrecEvalWithMissingResults) {
        this.calculateTrecEvalWithMissingResults = calculateTrecEvalWithMissingResults;
    }

    public int getK() {
        return k;
    }

    /**
     * <p>The number of top documents to calculate scores for with trec_eval. Defaults to 1000.</p>
     *
     * @param k The number of the top documents.
     */
    public void setK(int k) {
        this.k = k;
    }


    public List<ResultList<Q>> getLastResultListSet() {
        return lastResultListSet;
    }

    @Override
    public void run() {
        final String experimentId = retrieval.getExperimentId();
        final String longExperimentId = experimentId + " with decorators " + retrieval.getQuery().getName();

        LOG.info("Running collection " + longExperimentId + "...");

        if (retrieval.getResultsDir() == null)
            retrieval.withResultsDir(this.resultsDir);

        final Function<QueryDescription, String> queryIdFunction = goldDataset != null ? goldDataset.getQueryIdFunction() : q -> String.valueOf(q.getNumber());
        lastResultListSet = retrieval.retrieve((Collection<Q>) topicSet.getTopics(), queryIdFunction);


        File output = retrieval.getOutput();
        int k = this.k;
        boolean calculateTrecEvalWithMissingResults = this.calculateTrecEvalWithMissingResults;
        String statsDir = this.statsDir;

        Metrics allMetrics = new TrecMetricsCreator(experimentId, longExperimentId, output, getQrelFile(), k, calculateTrecEvalWithMissingResults, statsDir, goldDataset.getType(), getSampleQrelFile())
                .computeMetrics();

        this.allMetrics = allMetrics;

        // TODO Experiment API #53
//        System.out.println(allMetrics.getInfNDCG() + ";" + longExperimentId);
    }

    public Metrics getAllMetrics() {
        return allMetrics;
    }

    private File getQrelFile() {
        File qrelFile = new File("qrels", String.format("%s.qrels", getExperimentId()));
        goldDataset.writeQrelFile(qrelFile);
        return qrelFile;
    }

    private File getSampleQrelFile() {
        if (goldDataset.isSampleGoldStandard()) {
            final File sampleQrelFile = new File("qrels", String.format("sample-%s.qrels", getExperimentId()));
            goldDataset.writeSampleQrelFile(sampleQrelFile);
            return sampleQrelFile;
        }
        return null;
    }

    public Query getDecorator() {
        return retrieval.getQuery();
    }

    public void setGoldDataset(GoldStandard goldDataset) {
        this.goldDataset = goldDataset;
    }
}
