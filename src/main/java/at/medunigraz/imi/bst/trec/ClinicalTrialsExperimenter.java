package at.medunigraz.imi.bst.trec;

import at.medunigraz.imi.bst.trec.experiment.Experiment;
import at.medunigraz.imi.bst.trec.experiment.ExperimentsBuilder;
import at.medunigraz.imi.bst.trec.model.GoldStandard;
import at.medunigraz.imi.bst.trec.model.Task;

import java.io.File;
import java.util.Set;

public class ClinicalTrialsExperimenter {
	public static void main(String[] args) {
		final File improvedTemplate = new File(
				ClinicalTrialsExperimenter.class.getResource("/templates/clinical_trials/hpictboost.json").getFile());
		final File phraseTemplate = new File(
				ClinicalTrialsExperimenter.class.getResource("/templates/clinical_trials/hpictphrase.json").getFile());

		final GoldStandard goldStandard = GoldStandard.INTERNAL;
		final Task target = Task.CLINICAL_TRIALS;
		final int year = 2019;

		ExperimentsBuilder builder = new ExperimentsBuilder();

		// Judging order: 1
		builder.newExperiment().withName("hpictall").withYear(year).withGoldStandard(goldStandard).withTarget(target)
                .withSubTemplate(improvedTemplate).withWordRemoval().withSolidTumor().withDiseasePreferredTerm()
                .withDiseaseSynonym().withGeneSynonym().withGeneDescription().withGeneFamily();

		// Judging order: 2
		builder.newExperiment().withName("hpictphrase").withYear(year).withGoldStandard(goldStandard).withTarget(target)
				.withSubTemplate(phraseTemplate).withWordRemoval().withSolidTumor().withDiseasePreferredTerm()
				.withDiseaseSynonym().withGeneSynonym().withGeneFamily();

		// Judging order: 3
		builder.newExperiment().withName("hpictboost").withYear(year).withGoldStandard(goldStandard).withTarget(target)
				.withSubTemplate(improvedTemplate).withWordRemoval().withSolidTumor().withDiseasePreferredTerm()
				.withDiseaseSynonym().withGeneSynonym().withGeneFamily();

	  	// Judging order: 4
	  	builder.newExperiment().withName("hpictcommon").withYear(year).withGoldStandard(goldStandard).withTarget(target)
				.withSubTemplate(improvedTemplate).withWordRemoval().withDiseasePreferredTerm().withDiseaseSynonym()
				.withGeneSynonym();

		// Judging order: 5
		builder.newExperiment().withName("hpictbase").withYear(year).withGoldStandard(goldStandard).withTarget(target)
				.withSubTemplate(improvedTemplate);

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
