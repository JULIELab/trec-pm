package at.medunigraz.imi.bst.trec;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.apache.commons.io.FileUtils;

import at.medunigraz.imi.bst.trec.experiment.Experiment;
import at.medunigraz.imi.bst.trec.experiment.ExperimentsBuilder;

public class KeywordExperimenter {
	public static void main(String[] args) {
		final File keywordTemplate = new File(
				KeywordExperimenter.class.getResource("/templates/biomedical_articles/keyword.json").getFile());
		final File keywordsSource = new File(KeywordExperimenter.class.getResource("/negative-keywords/").getFile());

		ExperimentsBuilder builder = new ExperimentsBuilder();

		File[] files = null;
		if (keywordsSource.isDirectory()) {
			files = keywordsSource.listFiles();
		} else {
			files = new File[] { keywordsSource };
		}

		// FIXME set your baseline
		double baselineMetric = 0;

		// 1st step: collect metrics for individual keywords

		for (File keywordFile : files) {
			List<String> lines;
			try {
				lines = FileUtils.readLines(keywordFile, "UTF-8");
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}

			for (String keyword : lines) {
				builder.newExperiment().withName(keyword).withYear(2017).withGoldStandard(Experiment.GoldStandard.OFFICIAL)
						.withTarget(Experiment.Task.PUBMED).withKeyword(keyword).withTemplate(keywordTemplate)
						.withWordRemoval();
			}
		}

		TreeMap<Double, String> resultsUniqueKeywords = runExperiments(builder.build());



		// 2nd step: collect metrics for combination of keywords in a greedy fashion
		builder = new ExperimentsBuilder();

		String keyword = "";
		for (Map.Entry<Double, String> entry : resultsUniqueKeywords.entrySet()) {
			Double metric = entry.getKey();

			// Only use words that improve results over a baseline
			if (metric < baselineMetric) {
				break;
			}

			keyword = keyword + " " + entry.getValue();
			keyword = keyword.trim();

			builder.newExperiment().withName(keyword).withYear(2017).withGoldStandard(Experiment.GoldStandard.OFFICIAL)
					.withTarget(Experiment.Task.PUBMED).withKeyword(keyword).withTemplate(keywordTemplate)
					.withWordRemoval();
		}

		//TreeMap<Double, String> resultsCombinationKeywords = runExperiments(builder.build());
	}

	private static TreeMap<Double, String> runExperiments(Set<Experiment> experiments) {
		TreeMap<Double, String> results = new TreeMap<>(Collections.reverseOrder());

		for (Experiment exp : experiments) {
			exp.start();
			try {
				exp.join();

				// Change comparison metric here
				double metric = exp.allMetrics.getInfNDCG();
//				double metric = exp.allMetrics.getP10();
//				double metric = exp.allMetrics.getRPrec();
//				double metric = exp.allMetrics.getP5();
//				double metric = exp.allMetrics.getP15();

				results.put(metric, exp.getExperimentId());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// Print the map
		results.entrySet().stream().forEach(System.out::println);

		return results;
	}

}
