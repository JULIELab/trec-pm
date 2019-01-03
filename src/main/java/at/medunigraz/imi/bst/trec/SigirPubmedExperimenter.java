package at.medunigraz.imi.bst.trec;

import at.medunigraz.imi.bst.trec.experiment.Experiment;
import at.medunigraz.imi.bst.trec.experiment.ExperimentsBuilder;

import java.io.File;
import java.util.Set;

public class SigirPubmedExperimenter {
    public static void main(String[] args) {
        final File baseline = new File(
                PubmedExperimenter.class.getResource("/templates/sigir19_experiments_biomed/baseline.json").getFile());

        final Experiment.GoldStandard goldStandard = Experiment.GoldStandard.OFFICIAL;
        final Experiment.Task target = Experiment.Task.PUBMED;
        final int year = 2017;


        ExperimentsBuilder builder = new ExperimentsBuilder();


        builder.newExperiment().withName("baseline").withYear(year).withGoldStandard(goldStandard).withTarget(target)
                .withSubTemplate(baseline);

        builder.newExperiment().withName("baselinewr").withYear(year).withGoldStandard(goldStandard).withTarget(target)
                .withSubTemplate(baseline).withWordRemoval();

        builder.newExperiment().withName("genes").withYear(year).withGoldStandard(goldStandard).withTarget(target)
                .withSubTemplate(baseline).withWordRemoval().withGeneSynonym().withGeneDescription();

        builder.newExperiment().withName("diseases").withYear(year).withGoldStandard(goldStandard).withTarget(target)
                .withSubTemplate(baseline).withWordRemoval().withDiseasePreferredTerm().withDiseaseSynonym();

        builder.newExperiment().withName("genedis").withYear(year).withGoldStandard(goldStandard).withTarget(target)
                .withSubTemplate(baseline).withWordRemoval().withDiseasePreferredTerm().withDiseaseSynonym().withGeneSynonym().withGeneDescription();


        Set<Experiment> experiments = builder.build();

        for (Experiment exp : experiments) {
            exp.start();
            try {
                exp.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
