package at.medunigraz.imi.bst.trec;

import at.medunigraz.imi.bst.trec.experiment.Experiment;
import at.medunigraz.imi.bst.trec.experiment.ExperimentsBuilder;
import at.medunigraz.imi.bst.trec.model.GoldStandard;
import at.medunigraz.imi.bst.trec.model.Task;

import java.io.File;
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


		ExperimentsBuilder builder = new ExperimentsBuilder();


		// Judging order: 1
		builder.newExperiment().withName("hpipubclass").withYear(year).withGoldStandard(goldStandard).withTarget(target)
				.withSubTemplate(extraBoostTemplate).withWordRemoval().withGeneSynonym()
				.withDiseasePreferredTerm().withGeneDescription().withDiseaseSynonym();

		// Judging order: 2
		builder.newExperiment().withName("hpipubnone").withYear(year).withGoldStandard(goldStandard).withTarget(target)
				.withSubTemplate(noClassifierTemplate).withWordRemoval().withGeneSynonym()
                		.withDiseasePreferredTerm().withGeneDescription().withDiseaseSynonym();

		// Judging order: 3
		builder.newExperiment().withName("hpipubboost").withYear(year).withGoldStandard(goldStandard).withTarget(target)
				.withSubTemplate(improvedTemplate).withWordRemoval().withGeneSynonym()
                		.withDiseasePreferredTerm().withGeneDescription().withDiseaseSynonym();

		// Judging order: 4
		builder.newExperiment().withName("hpipubcommon").withYear(year).withGoldStandard(goldStandard).withTarget(target)
				.withSubTemplate(noClassifierTemplate).withWordRemoval().withGeneSynonym()
				.withDiseasePreferredTerm().withDiseaseSynonym();

		// Judging order: 5
		builder.newExperiment().withName("hpipubbase").withYear(year).withGoldStandard(goldStandard).withTarget(target)
				.withSubTemplate(noClassifierTemplate);

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
