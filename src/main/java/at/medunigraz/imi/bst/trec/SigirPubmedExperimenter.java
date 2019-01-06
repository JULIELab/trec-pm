package at.medunigraz.imi.bst.trec;

import at.medunigraz.imi.bst.trec.experiment.Experiment;
import at.medunigraz.imi.bst.trec.experiment.ExperimentsBuilder;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SigirPubmedExperimenter {
    public static void main(String[] args) {
        final File baseline = new File(
                PubmedExperimenter.class.getResource("/templates/sigir19_experiments_biomed/baseline.json").getFile());

        final Experiment.GoldStandard goldStandard = Experiment.GoldStandard.OFFICIAL;
        final Experiment.Task target = Experiment.Task.PUBMED;
        final int year = 2017;


        Map<String, String> templateProperties = new HashMap<>();
        templateProperties.put("disease_boost", "1");
        templateProperties.put("disease_prefterm_boost", "1");
        templateProperties.put("disease_syn_boost", "1");
        templateProperties.put("gene_boost", "1");
        templateProperties.put("gene_syn_boost", "1");
        templateProperties.put("gene_desc_boost", "1");

        ExperimentsBuilder builder = new ExperimentsBuilder();

//        builder.newExperiment().withName("baseline").withYear(year).withGoldStandard(goldStandard).withTarget(target)
//                .withSubTemplate(baseline, templateProperties);
//
//        builder.newExperiment().withName("baselinewr").withYear(year).withGoldStandard(goldStandard).withTarget(target)
//                .withSubTemplate(baseline, templateProperties).withWordRemoval();

        builder.newExperiment().withName("diseases").withYear(year).withGoldStandard(goldStandard).withTarget(target)
                .withSubTemplate(baseline, templateProperties).withWordRemoval().withDiseasePreferredTerm().withDiseaseSynonym();

//        builder.newExperiment().withName("genes").withYear(year).withGoldStandard(goldStandard).withTarget(target)
//                .withSubTemplate(baseline, templateProperties).withWordRemoval().withGeneSynonym().withGeneDescription();
//
//        builder.newExperiment().withName("genedis").withYear(year).withGoldStandard(goldStandard).withTarget(target)
//                .withSubTemplate(baseline, templateProperties).withWordRemoval().withDiseasePreferredTerm().withDiseaseSynonym().withGeneSynonym().withGeneDescription();


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
