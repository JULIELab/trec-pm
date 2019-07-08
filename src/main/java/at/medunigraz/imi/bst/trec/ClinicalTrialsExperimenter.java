package at.medunigraz.imi.bst.trec;

import at.medunigraz.imi.bst.trec.experiment.Experiment;
import at.medunigraz.imi.bst.trec.experiment.ExperimentBuilder;
import at.medunigraz.imi.bst.trec.model.GoldStandard;
import at.medunigraz.imi.bst.trec.model.Task;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashSet;
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

		// Judging order: 1
		Experiment hpictall = new ExperimentBuilder().withName("hpictall").withYear(year).withGoldStandard(goldStandard).withTarget(target)
                .withSubTemplate(improvedTemplate).withWordRemoval().withSolidTumor().withDiseasePreferredTerm()
                .withDiseaseSynonym().withGeneSynonym().withGeneDescription().withGeneFamily().build();

		// Judging order: 2
		Experiment hpictphrase = new ExperimentBuilder().withName("hpictphrase").withYear(year).withGoldStandard(goldStandard).withTarget(target)
				.withSubTemplate(phraseTemplate).withWordRemoval().withSolidTumor().withDiseasePreferredTerm()
				.withDiseaseSynonym().withGeneSynonym().withGeneFamily().build();

		// Judging order: 3
		Experiment hpictboost = new ExperimentBuilder().withName("hpictboost").withYear(year).withGoldStandard(goldStandard).withTarget(target)
				.withSubTemplate(improvedTemplate).withWordRemoval().withSolidTumor().withDiseasePreferredTerm()
				.withDiseaseSynonym().withGeneSynonym().withGeneFamily().build();

	  	// Judging order: 4
		Experiment hpictcommon = new ExperimentBuilder().withName("hpictcommon").withYear(year).withGoldStandard(goldStandard).withTarget(target)
				.withSubTemplate(improvedTemplate).withWordRemoval().withDiseasePreferredTerm().withDiseaseSynonym()
				.withGeneSynonym().build();

		// Judging order: 5
		Experiment hpictbase = new ExperimentBuilder().withName("hpictbase").withYear(year).withGoldStandard(goldStandard).withTarget(target)
				.withSubTemplate(improvedTemplate).build();

		Set<Experiment> experiments = new LinkedHashSet<>(Arrays.asList(hpictall, hpictphrase, hpictboost, hpictcommon, hpictbase));

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
