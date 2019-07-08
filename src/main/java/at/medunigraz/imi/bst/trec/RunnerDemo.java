package at.medunigraz.imi.bst.trec;

import at.medunigraz.imi.bst.trec.experiment.Experiment;
import at.medunigraz.imi.bst.trec.experiment.ExperimentBuilder;
import at.medunigraz.imi.bst.trec.model.GoldStandard;
import at.medunigraz.imi.bst.trec.model.Task;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class RunnerDemo {
	public static void main(String[] args) {
		final File pmTemplate = new File(RunnerDemo.class.getResource("/templates/biomedical_articles/hpipubboost.json").getFile());

		final File ctTemplate = new File(RunnerDemo.class.getResource("/templates/clinical_trials/hpictboost.json").getFile());
		final int year = 2017;


		Experiment ba = new ExperimentBuilder().withYear(year).withGoldStandard(GoldStandard.OFFICIAL).withTarget(Task.PUBMED)
				.withSubTemplate(pmTemplate).withWordRemoval().build();
		Experiment ct = new ExperimentBuilder().withYear(year).withGoldStandard(GoldStandard.OFFICIAL)
				.withTarget(Task.CLINICAL_TRIALS).withSubTemplate(ctTemplate).withWordRemoval().build();

		Set<Experiment> bestExperiments = new LinkedHashSet<>(Arrays.asList(ba, ct));

		for (Experiment exp : bestExperiments) {
			exp.start();
			try {
				exp.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		for (Experiment exp : bestExperiments) {

		}
	}

}
