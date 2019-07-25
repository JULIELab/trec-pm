package at.medunigraz.imi.bst.trec.experiment;

import at.medunigraz.imi.bst.retrieval.Query;
import at.medunigraz.imi.bst.retrieval.Retrieval;
import at.medunigraz.imi.bst.trec.model.*;
import at.medunigraz.imi.bst.trec.stats.CSVStatsWriter;
import de.julielab.ir.goldstandards.GoldStandard;
import de.julielab.ir.model.QueryDescription;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class Experiment<Q extends QueryDescription> extends Thread {

    private static final Logger LOG = LogManager.getLogger();
    private static final int YEAR_PUBLISHED_GS = 2017;
    public Metrics allMetrics = null;
    private Retrieval<?, Q> retrieval;
    private Challenge challenge;
    private Task task;
    @Deprecated private GoldStandardType goldStandardType;
    private de.julielab.ir.goldstandards.GoldStandard goldDataset;
    private int year;
    private String statsDir = "stats/";
    private String resultsDir = "results/";
    private TopicSet topicSet;
    private boolean calculateTrecEvalWithMissingResults = true;
    private int k = 1000;
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
        this.goldStandardType = goldStandard.getType();
    }


    public Retrieval getRetrieval() {
        return retrieval;
    }

    public void setRetrieval(Retrieval retrieval) {
        this.retrieval = retrieval;
    }

    public void setChallenge(Challenge challenge) {
        this.challenge = challenge;
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

        // Load the default TopicSet which is all topics for the given year
        if (topicSet == null)
            topicSet = loadTopics();

        if (retrieval.getResultsDir() == null)
            retrieval.withResultsDir(this.resultsDir);

        final Function<QueryDescription, String> queryIdFunction = goldDataset != null ? goldDataset.getQueryIdFunction() : q -> String.valueOf(q.getNumber());
        lastResultListSet = retrieval.retrieve((Collection<Q>) topicSet.getTopics(), queryIdFunction);


        File output = retrieval.getOutput();
        File goldStandard = getQrelFile();
        int k = this.k;
        boolean calculateTrecEvalWithMissingResults = this.calculateTrecEvalWithMissingResults;
        String statsDir = this.statsDir;
        final File sampleGoldStandard = hasSampleGoldStandard() ? getSampleQrelFile() : null;

        Metrics allMetrics = new TrecMetricsCreator(experimentId, longExperimentId, output, goldStandard, k, calculateTrecEvalWithMissingResults, statsDir, goldStandardType, sampleGoldStandard).computeMetrics();

        this.allMetrics = allMetrics;

        // TODO Experiment API #53
//        System.out.println(allMetrics.getInfNDCG() + ";" + longExperimentId);
    }

    public Metrics getAllMetrics() {
        return allMetrics;
    }

    @NotNull
    private TopicSet loadTopics() {
        File example = new File(CSVStatsWriter.class.getResource("/topics/topics" + year + ".xml").getPath());
        return new TopicSet(example, challenge, year);
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public File getQrelFile() {
        if (goldDataset != null) {
            File qrelFile = new File("qrels", String.format("%s.qrels", getExperimentId()));
            goldDataset.writeQrelFile(qrelFile);
            return qrelFile;
        }
        // Old, fallback, code path. Should be replaced by `GoldStandardBuilder`.
        return new File(Experiment.class.getResource("/gold-standard/" + getGoldStandardFileName()).getPath());
    }

    /**
     * @return
     * @todo Add support for 2018 topics
     */
    public String getGoldStandardFileName() {
        // Internal gold standard for the 2017 edition on Scientific Abstracts
        if (goldStandardType == GoldStandardType.INTERNAL && task == Task.PUBMED && year == YEAR_PUBLISHED_GS) {
            return "topics2017-pmid.qrels";
        } else if (goldStandardType == GoldStandardType.OFFICIAL && (task == Task.PUBMED || task == Task.PUBMED_ONLINE) && year == YEAR_PUBLISHED_GS) {
            return "qrels-treceval-abstracts.2017.txt";
        } else if (goldStandardType == GoldStandardType.OFFICIAL && task == Task.CLINICAL_TRIALS && year == YEAR_PUBLISHED_GS) {
            return "qrels-treceval-clinical_trials.2017.txt";
        } else if (goldStandardType == GoldStandardType.OFFICIAL && (task == Task.PUBMED || task == Task.PUBMED_ONLINE) && year == 2018) {
            return "qrels-treceval-abstracts.2018.txt";
        } else if (goldStandardType == GoldStandardType.OFFICIAL && task == Task.CLINICAL_TRIALS && year == 2018) {
            return "qrels-treceval-clinical_trials.2018.txt";
        } else if (goldStandardType == GoldStandardType.INTERNAL && (task == Task.PUBMED || task == Task.PUBMED_ONLINE) && year == 2018) {
            return "gsheets-abstracts.2018.qrels";
        } else if (goldStandardType == GoldStandardType.INTERNAL && task == Task.CLINICAL_TRIALS && year == 2018) {
            return "gsheets-trials.2018.qrels";
        } else if (goldStandardType == GoldStandardType.INTERNAL && (task == Task.PUBMED || task == Task.PUBMED_ONLINE) && year == 2019) {
            return "gsheets-abstracts-2019.qrels";
        } else if (goldStandardType == GoldStandardType.INTERNAL && task == Task.CLINICAL_TRIALS && year == 2019) {
            return "gsheets-trials-2019.qrels";
        }else {
            throw new UnsupportedOperationException("Invalid combination of gold standard, task and year.");
        }
    }

    private File getSampleQrelFile() {
        // New code path used when Experiment is not created with the deprecated `ExperimentBuilder`.
        if (goldDataset != null && goldDataset.isSampleGoldStandard()) {
            final File sampleQrelFile = new File("qrels", String.format("sample-%s.qrels", getExperimentId()));
            goldDataset.writeSampleQrelFile(sampleQrelFile);
            return sampleQrelFile;
        }
        // Old, fallback, code path. Should be replaced by `GoldStandardBuilder`.
        if (hasSampleGoldStandard()) {
            if (year == 2017)
                return new File(getClass().getResource("/gold-standard/sample-qrels-final-abstracts.2017.txt").getPath());
            else if (year == 2018 && task == Task.PUBMED)
                return new File(getClass().getResource("/gold-standard/qrels-treceval-abstracts.2018.txt").getPath());
            else if (year == 2018 && task == Task.CLINICAL_TRIALS)
                return new File(getClass().getResource("/gold-standard/qrels-sample-ct.2018.txt").getPath());
            else
                throw new IllegalStateException("There should be a sample gold standard but no condition did meet for year, task, gstype: " + year + ", " + task + ", " + goldStandardType);
        } else {
            throw new UnsupportedOperationException("No available sample gold standard.");
        }
    }

    private boolean hasSampleGoldStandard() {
        if (goldDataset != null && goldDataset.isSampleGoldStandard())
            return true;
        boolean hasgs = goldStandardType == GoldStandardType.OFFICIAL;
        hasgs &= task == Task.PUBMED || (task == Task.CLINICAL_TRIALS && year == 2018);
        return hasgs;
    }

    public Query getDecorator() {
        return retrieval.getQuery();
    }

    public void setGoldDataset(de.julielab.ir.goldstandards.GoldStandard goldDataset) {
        this.goldDataset = goldDataset;
        this.goldStandardType = goldDataset.getType();
    }


}
