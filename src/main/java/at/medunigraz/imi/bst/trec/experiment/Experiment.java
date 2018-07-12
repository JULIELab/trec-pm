package at.medunigraz.imi.bst.trec.experiment;

import java.io.File;
import java.util.*;

import at.medunigraz.imi.bst.trec.evaluator.SampleEval;
import at.medunigraz.imi.bst.trec.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.trec.evaluator.TrecEval;
import at.medunigraz.imi.bst.trec.evaluator.TrecWriter;
import at.medunigraz.imi.bst.trec.query.Query;
import at.medunigraz.imi.bst.trec.stats.CSVStatsWriter;
import at.medunigraz.imi.bst.trec.stats.XMLStatsWriter;

public class Experiment extends Thread {

	private static final Logger LOG = LogManager.getLogger();

	private Query decorator;
	
	private Task task;
	
	private GoldStandard goldStandard;

	private static final int YEAR_PUBLISHED_GS = 2017;

	private int year;
	
	public static enum Task {
		CLINICAL_TRIALS, PUBMED
	}
	
	public static enum GoldStandard {
		INTERNAL, OFFICIAL
	}

	@Override
	public void run() {
		final String name = getExperimentId() + " with decorators " + decorator.getName();

		LOG.info("Running collection " + name + "...");

		File example = new File(CSVStatsWriter.class.getResource("/topics/topics" + year + ".xml").getPath());
		TopicSet topicSet = new TopicSet(example);

		File output = new File("results/" + getExperimentId() + ".trec_results");
		final String runName = "experiment";  // TODO generate from experimentID, but respecting TREC syntax
		TrecWriter tw = new TrecWriter(output, runName);

		// TODO DRY Issue #53
		List<ResultList> resultListSet = new ArrayList<>();
		for (Topic topic : topicSet.getTopics()) {
			List<Result> results = decorator.query(topic);

			ResultList resultList = new ResultList(topic);
			resultList.setResults(results);
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
                entry.getValue().merge(sampleEvalMetrics.get(topic));
            }
        }

		XMLStatsWriter xsw = new XMLStatsWriter(new File("stats/" + this.goldStandard + "_" + getExperimentId() + ".xml"));
		xsw.write(metrics);
		xsw.close();

		CSVStatsWriter csw = new CSVStatsWriter(new File("stats/" + this.goldStandard + "_" + getExperimentId() + ".csv"));
		csw.write(metrics);
		csw.close();

		Metrics allMetrics = metrics.get("all");
		LOG.info("Got NDCG = {}, infNDCG = {}, P@10 = {} for collection {}", allMetrics.getNDCG(), allMetrics.getInfNDCG(), allMetrics.getP10(), name);
		LOG.trace(allMetrics);
		
		// TODO Experiment API #53
		System.out.println(te.getNDCG() + ";" + name);
	}

	public void setDecorator(Query decorator) {
		this.decorator = decorator;
	}

	public String getExperimentId() {
		return String.format("%s_%d_%s", getShortTaskName(), year, decorator.getName());
	}

	public void setYear(int year) {
		this.year = year;
	}
	
	public void setTask(Task task) {
		this.task = task;
	}

	public void setGoldStandard(GoldStandard goldStandard) {
		this.goldStandard = goldStandard;
	}

	/**
	 *
	 * @todo Add support for 2018 topics
	 *
	 * @return
	 */
	public String getGoldStandardFileName() {
		// So far, we have only an internal gold standard for the 2017 edition on Scientific Abstracts
		if (goldStandard == GoldStandard.INTERNAL && task == Task.PUBMED && year == YEAR_PUBLISHED_GS) {
			return "topics2017-pmid.qrels";
		} else if (goldStandard == GoldStandard.OFFICIAL && task == Task.PUBMED && year == YEAR_PUBLISHED_GS) {
			return "qrels-treceval-abstracts.2017.txt";
		} else if (goldStandard == GoldStandard.OFFICIAL && task == Task.CLINICAL_TRIALS && year == YEAR_PUBLISHED_GS) {
			return "qrels-treceval-clinical_trials.2017.txt";
		} else {
			throw new RuntimeException("Invalid combination of gold standard, task and year.");
		}
	}

	private File getSampleGoldStandard() {
        if (hasSampleGoldStandard()) {
            return new File(getClass().getResource("/gold-standard/sample-qrels-final-abstracts.txt").getPath());
        } else {
            throw new RuntimeException("No available sample gold standard.");
        }
    }

	private boolean hasSampleGoldStandard() {
	    return goldStandard == GoldStandard.OFFICIAL && task == Task.PUBMED && year == YEAR_PUBLISHED_GS;
    }
	
	public String getIndexName() {
		switch (task) {
		case CLINICAL_TRIALS:
			return TrecConfig.ELASTIC_CT_INDEX;
		case PUBMED:
			return TrecConfig.ELASTIC_BA_INDEX;
		default:
			return "";
		}
	}
	
	public String[] getTypes() {
		String[] ret = new String[0];	// Everything
		
		if (task == Task.CLINICAL_TRIALS) {
			return new String[] { TrecConfig.ELASTIC_CT_TYPE};
		}
		
		if (task == Task.PUBMED && goldStandard == GoldStandard.INTERNAL) {
			return new String[] { TrecConfig.ELASTIC_BA_EXTRA_TYPE, TrecConfig.ELASTIC_BA_MEDLINE_TYPE};
		}
		
		return ret;
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
		return decorator;
	}
	
	
}
