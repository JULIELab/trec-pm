package at.medunigraz.imi.bst.trec.experiment;

import java.io.File;
import java.util.*;

import at.medunigraz.imi.bst.retrieval.Retrieval;
import at.medunigraz.imi.bst.trec.evaluator.SampleEval;
import at.medunigraz.imi.bst.trec.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.trec.evaluator.TrecEval;
import at.medunigraz.imi.bst.trec.evaluator.TrecWriter;
import at.medunigraz.imi.bst.retrieval.Query;
import at.medunigraz.imi.bst.trec.stats.CSVStatsWriter;
import at.medunigraz.imi.bst.trec.stats.XMLStatsWriter;

public class Experiment extends Thread {

    private static final Logger LOG = LogManager.getLogger();
    private static final int YEAR_PUBLISHED_GS = 2017;
    public Metrics allMetrics = null;
    private Retrieval retrieval;
    private Challenge challenge;
    private Task task;
    private GoldStandard goldStandard;
    private int year;
    private String experimentName = null;
    private String statsDir = "stats/";
    private String resultsDir = "results/";

    public static String getIndexName(Task task) {
        switch (task) {
            case CLINICAL_TRIALS:
                return TrecConfig.ELASTIC_CT_INDEX;
            case PUBMED:
                return TrecConfig.ELASTIC_BA_INDEX;
            default:
                return "";
        }
    }

    public static String[] getTypes(Task task, GoldStandard goldStandard) {
        String[] ret = new String[0];    // Everything

        if (task == Task.CLINICAL_TRIALS) {
            return new String[]{TrecConfig.ELASTIC_CT_TYPE};
        }

        if (task == Task.PUBMED && goldStandard == GoldStandard.INTERNAL) {
            return new String[]{TrecConfig.ELASTIC_BA_EXTRA_TYPE, TrecConfig.ELASTIC_BA_MEDLINE_TYPE};
        }

        return ret;
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

    @Override
    public void run() {
        final String name = getExperimentId() + " with decorators " + retrieval.getQuery().getName();

        LOG.info("Running collection " + name + "...");

        File example = new File(CSVStatsWriter.class.getResource("/topics/topics" + year + ".xml").getPath());
        TopicSet topicSet = new TopicSet(example, challenge, task, year);

        File resultsDir = new File(this.resultsDir);
        if (!resultsDir.exists())
            resultsDir.mkdir();
        File output = new File(this.resultsDir + getExperimentId() + ".trec_results");
        final String runName = getExperimentName();  // TODO generate from experimentID, but respecting TREC syntax
        TrecWriter tw = new TrecWriter(output, runName);

        // TODO DRY Issue #53
        Collection<Topic> topics = topicSet.getTopics();
        List<ResultList<?>> resultListSet = new ArrayList<>();
        for (Topic topic : topics) {
            List<Result> results = retrieval.getQuery().query(topic);


            if (results.isEmpty())
                throw new IllegalStateException("RESULT EMPTY for " + experimentName);

            ResultList<?> resultList = new ResultList<>(topic);
            resultList.addAll(results);
            resultListSet.add(resultList);
        }

        tw.write(resultListSet);
        tw.close();

        File goldStandard = new File(CSVStatsWriter.class.getResource("/gold-standard/" + getGoldStandardFileName()).getPath());
        TrecEval te = new TrecEval(goldStandard, output);
        Map<String, Metrics> metrics = te.getMetrics();

        if (hasSampleGoldStandard()) {
            SampleEval se = new SampleEval(getSampleGoldStandard(), output);

            // TODO Refactor into MetricSet
            Map<String, Metrics> sampleEvalMetrics = se.getMetrics();
            for (Map.Entry<String, Metrics> entry : metrics.entrySet()) {
                String topic = entry.getKey();
                if (topic == null)
                    throw new IllegalStateException("There is no evaluation result for topic " + topic + " in result file " + output.getAbsolutePath() + ". Perhaps the sample_eval.pl file has the wrong version.");
                entry.getValue().merge(sampleEvalMetrics.get(topic));
            }
        }

        File statsDirFile = new File(statsDir);
        if (!statsDirFile.exists())
            statsDirFile.mkdir();

        XMLStatsWriter xsw = new XMLStatsWriter(new File(statsDir + this.goldStandard + "_" + getExperimentId() + ".xml"));
        xsw.write(metrics);
        xsw.close();

        CSVStatsWriter csw = new CSVStatsWriter(new File(statsDir + this.goldStandard + "_" + getExperimentId() + ".csv"));
        csw.write(metrics);
        csw.close();

        allMetrics = metrics.get("all");
        LOG.info("Got NDCG = {}, infNDCG = {}, P@5 = {}, P@10 = {}, P@15 = {}, R-Prec = {}, set_recall = {} for collection {}",
                allMetrics.getNDCG(), allMetrics.getInfNDCG(), allMetrics.getP5(), allMetrics.getP10(), allMetrics.getP15(), allMetrics.getRPrec(), allMetrics.getSetRecall(),
                name);
        LOG.trace(allMetrics);

        // TODO Experiment API #53
        System.out.println(allMetrics.getInfNDCG() + ";" + name);
    }

    public String getExperimentId() {
        if (experimentName != null) {
            return experimentName.replace(" ", "_");
        }
        return String.format("%s_%d_%s", getShortTaskName(), year, retrieval.getQuery().getName().replace(" ", "_"));
    }

    public String getExperimentName() {
        if (experimentName == null) {
            return "experiment";
        }

        return experimentName;
    }

    public void setExperimentName(String name) {
        this.experimentName = name;
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

    public String getShortTaskName() {
        switch (task) {
            case CLINICAL_TRIALS:
                return "ct";
            case PUBMED:
                return "pmid";
            default:
                return "";
        }
    }

    public Query getDecorator() {
        return retrieval.getQuery();
    }


}
