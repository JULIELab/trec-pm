package at.medunigraz.imi.bst.trec;

import at.medunigraz.imi.bst.trec.experiment.Experiment;
import at.medunigraz.imi.bst.trec.experiment.ExperimentBuilder;
import at.medunigraz.imi.bst.trec.model.GoldStandard;
import at.medunigraz.imi.bst.trec.model.Task;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class PubmedExperimenter {
	public static void main(String[] args) {
		final File improvedTemplate = new File(
				PubmedExperimenter.class.getResource("/templates/biomedical_articles/hpipubboost.json").getFile());
		final File noClassifierTemplate = new File(
				PubmedExperimenter.class.getResource("/templates/biomedical_articles/hpipubnone.json").getFile());
		final File extraBoostTemplate = new File(
				PubmedExperimenter.class.getResource("/templates/biomedical_articles/hpipubclass.json").getFile());

		final GoldStandard goldStandard = GoldStandard.OFFICIAL;
		final Task target = Task.PUBMED;
		final int year = 2017;

		// Judging order: 1
		Experiment hpipubclass = new ExperimentBuilder().withName("hpipubclass").withYear(year).withGoldStandard(goldStandard).withTarget(target)
				.withSubTemplate(extraBoostTemplate).withWordRemoval().withGeneSynonym()
				.withDiseasePreferredTerm().withGeneDescription().withDiseaseSynonym().build();

		// Judging order: 2
		Experiment hpipubnone = new ExperimentBuilder().withName("hpipubnone").withYear(year).withGoldStandard(goldStandard).withTarget(target)
				.withSubTemplate(noClassifierTemplate).withWordRemoval().withGeneSynonym()
                		.withDiseasePreferredTerm().withGeneDescription().withDiseaseSynonym().build();

		// Judging order: 3
		Experiment hpipubboost = new ExperimentBuilder().withName("hpipubboost").withYear(year).withGoldStandard(goldStandard).withTarget(target)
				.withSubTemplate(improvedTemplate).withWordRemoval().withGeneSynonym()
                		.withDiseasePreferredTerm().withGeneDescription().withDiseaseSynonym().build();

		// Judging order: 4
		Experiment hpipubcommon = new ExperimentBuilder().withName("hpipubcommon").withYear(year).withGoldStandard(goldStandard).withTarget(target)
				.withSubTemplate(noClassifierTemplate).withWordRemoval().withGeneSynonym()
				.withDiseasePreferredTerm().withDiseaseSynonym().build();

		// Judging order: 5
		Experiment hpipubbase = new ExperimentBuilder().withName("hpipubbase").withYear(year).withGoldStandard(goldStandard).withTarget(target)
				.withSubTemplate(noClassifierTemplate).build();

		Set<Experiment> experiments = new LinkedHashSet<>(Arrays.asList(hpipubclass, hpipubnone, hpipubboost, hpipubcommon, hpipubbase));

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
