package at.medunigraz.imi.bst.trec;

import java.io.File;
import java.util.Set;

import at.medunigraz.imi.bst.trec.experiment.Experiment;
import at.medunigraz.imi.bst.trec.experiment.ExperimentsBuilder;
import at.medunigraz.imi.bst.trec.model.GoldStandard;
import at.medunigraz.imi.bst.trec.model.Task;

public class RunnerDemo {
	public static void main(String[] args) {
		final File pmTemplate = new File(RunnerDemo.class.getResource("/templates/biomedical_articles/hpipubboost.json").getFile());

		final File ctTemplate = new File(RunnerDemo.class.getResource("/templates/clinical_trials/hpictboost.json").getFile());
		final int year = 2017;


		ExperimentsBuilder builder = new ExperimentsBuilder();

		builder.newExperiment().withYear(year).withGoldStandard(GoldStandard.OFFICIAL).withTarget(Task.PUBMED)
				.withSubTemplate(pmTemplate).withWordRemoval();
		builder.newExperiment().withYear(year).withGoldStandard(GoldStandard.OFFICIAL)
				.withTarget(Task.CLINICAL_TRIALS).withSubTemplate(ctTemplate).withWordRemoval();

		Set<Experiment> bestExperiments = builder.build();

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
