package at.medunigraz.imi.bst.trec;

import at.medunigraz.imi.bst.trec.experiment.Experiment;
import at.medunigraz.imi.bst.trec.experiment.ExperimentsBuilder;
import at.medunigraz.imi.bst.trec.model.GoldStandard;
import at.medunigraz.imi.bst.trec.model.Task;

import java.io.File;
import java.util.Set;

public class BoostExperimenter {
	public static void main(String[] args) {
		final File relaxedTemplate = new File(RunnerDemo.class.getResource("/templates/biomedical_articles/boost.json").getFile());

		ExperimentsBuilder builder = new ExperimentsBuilder();

		for (float i = 1; i <= 5; i += 0.5) {
			builder.newExperiment().withYear(2017).withGoldStandard(GoldStandard.OFFICIAL)
					.withTarget(Task.PUBMED).withProperties("keyword", String.valueOf(i)).withTemplate(relaxedTemplate)
					.withWordRemoval();
		}

		Set<Experiment> experiments = builder.build();

		for (Experiment exp : experiments) {
			exp.start();
			try {
				exp.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		for (Experiment exp : experiments) {

		}
	}

}
