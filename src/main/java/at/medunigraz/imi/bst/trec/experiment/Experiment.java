package at.medunigraz.imi.bst.trec.experiment;

import java.io.File;
import java.util.*;

import at.medunigraz.imi.bst.retrieval.Retrieval;
import at.medunigraz.imi.bst.trec.model.*;
import de.julielab.ir.ltr.Document;
import de.julielab.ir.ltr.DocumentList;
import de.julielab.ir.ltr.features.IRScore;
import de.julielab.ir.model.QueryDescription;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import at.medunigraz.imi.bst.retrieval.Query;
import at.medunigraz.imi.bst.trec.stats.CSVStatsWriter;
import org.jetbrains.annotations.NotNull;

public class Experiment<Q extends QueryDescription> extends Thread {

    private static final Logger LOG = LogManager.getLogger();
    private static final int YEAR_PUBLISHED_GS = 2017;
    public Metrics allMetrics = null;
    private Retrieval<?> retrieval;
    private Challenge challenge;
    private Task task;
    private GoldStandard goldStandard;
    private de.julielab.ir.goldstandards.GoldStandard goldDataset;
    private int year;
    private String statsDir = "stats/";
    private String resultsDir = "results/";
    private TopicSet topicSet;
    private boolean calculateTrecEvalWithMissingResults = true;
    private int k = 1000;


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

        final List<ResultList<Q>> resultLists = retrieval.retrieve(topicSet.getTopics(), goldDataset.getQueryIdFunction());

        List<DocumentList<Q>> lastDocumentLists = new ArrayList<>();
        for (ResultList<Q> list : resultLists) {
            final DocumentList<Q> documents = new DocumentList<>();
            for (Result r : list.getResults()) {
                final Document<Q> doc = new Document<>();
                doc.setId(r.getId());
                doc.setScore(IRScore.BM25, r.getScore());
                documents.add(doc);
            }
            lastDocumentLists.add(documents);
        }


        File output = retrieval.getOutput();
        File goldStandard = goldDataset != null && goldDataset.getQrelFile() != null ? goldDataset.getQrelFile() : new File(CSVStatsWriter.class.getResource("/gold-standard/" + getGoldStandardFileName()).getPath());
        int k = this.k;
        boolean calculateTrecEvalWithMissingResults = this.calculateTrecEvalWithMissingResults;
        String statsDir = this.statsDir;
        GoldStandard goldStandardType = this.goldStandard;
        final File sampleGoldStandard = getSampleGoldStandard();

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
        return new TopicSet(example, challenge, task, year);
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    /**
     * @return
     * @todo Add support for 2018 topics
     */
    public String getGoldStandardFileName() {
        // So far, we have only an internal gold standard for the 2017 edition on Scientific Abstracts
        if (goldStandard == GoldStandard.INTERNAL && task == Task.PUBMED && year == YEAR_PUBLISHED_GS) {
            return "topics2017-pmid.qrels";
        } else if (goldStandard == GoldStandard.OFFICIAL && (task == Task.PUBMED || task == Task.PUBMED_ONLINE) && year == YEAR_PUBLISHED_GS) {
            return "qrels-treceval-abstracts.2017.txt";
        } else if (goldStandard == GoldStandard.OFFICIAL && task == Task.CLINICAL_TRIALS && year == YEAR_PUBLISHED_GS) {
            return "qrels-treceval-clinical_trials.2017.txt";
        } else if (goldStandard == GoldStandard.OFFICIAL && (task == Task.PUBMED || task == Task.PUBMED_ONLINE) && year == 2018) {
            return "qrels-treceval-abstracts.2018.txt";
        } else if (goldStandard == GoldStandard.OFFICIAL && task == Task.CLINICAL_TRIALS && year == 2018) {
            return "qrels-treceval-clinical_trials.2018.txt";
        } else if (goldStandard == GoldStandard.INTERNAL && (task == Task.PUBMED || task == Task.PUBMED_ONLINE) && year == 2018) {
            return "gsheets-abstracts.2018.qrels";
        } else if (goldStandard == GoldStandard.INTERNAL && task == Task.CLINICAL_TRIALS && year == 2018) {
            return "gsheets-trials.2018.qrels";
        } else {
            throw new UnsupportedOperationException("Invalid combination of gold standard, task and year.");
        }
    }

    private File getSampleGoldStandard() {
        if (goldDataset != null && goldDataset.getSampleQrelFile() != null)
            return goldDataset.getSampleQrelFile();
        if (hasSampleGoldStandard()) {
            if (year == 2017)
                return new File(getClass().getResource("/gold-standard/sample-qrels-final-abstracts.2017.txt").getPath());
            else if (year == 2018 && task == Task.PUBMED)
                return new File("resources/qrels-sample-abstracts.txt");
            else if (year == 2018 && task == Task.CLINICAL_TRIALS)
                return new File("resources/qrels-sample-trials.txt");
            else
                throw new IllegalStateException("There should be a sample gold standard but no condition did meet for year, task, gstype: " + year + ", " + task + ", " + goldStandard);
        } else {
            throw new UnsupportedOperationException("No available sample gold standard.");
        }
    }

    private boolean hasSampleGoldStandard() {
        if (goldDataset != null && goldDataset.getSampleQrelFile() != null)
            return true;
        boolean hasgs = goldStandard == GoldStandard.OFFICIAL;
        hasgs &= task == Task.PUBMED || (task == Task.CLINICAL_TRIALS && year == 2018);
        return hasgs;
    }

    public GoldStandard getGoldStandard() {
        return goldStandard;
    }

    public void setGoldStandard(GoldStandard goldStandard) {
        this.goldStandard = goldStandard;
    }

    public Query getDecorator() {
        return retrieval.getQuery();
    }

    public void setGoldDataset(de.julielab.ir.goldstandards.GoldStandard goldDataset) {
        this.goldDataset = goldDataset;
    }


}
