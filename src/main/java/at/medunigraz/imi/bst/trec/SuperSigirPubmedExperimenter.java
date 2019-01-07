package at.medunigraz.imi.bst.trec;

import at.medunigraz.imi.bst.trec.experiment.Experiment;
import at.medunigraz.imi.bst.trec.experiment.ExperimentsBuilder;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SuperSigirPubmedExperimenter {


    protected static void runExperiments(Map<String, String> templateProperties, Experiment.GoldStandard goldStandard, Experiment.Task target, int year) {
        final File baseline = new File(
                PubmedExperimenter.class.getResource("/templates/sigir19_experiments_biomed/baseline.json").getFile());
        final File baselinePlusGeneField = new File(
                PubmedExperimenter.class.getResource("/templates/sigir19_experiments_biomed/baseline_plus_genefield.json").getFile());
        final File baselineOnlyGeneField = new File(
                PubmedExperimenter.class.getResource("/templates/sigir19_experiments_biomed/baseline_only_genefield.json").getFile());
        final File withPositiveBoosters = new File(
                PubmedExperimenter.class.getResource("/templates/sigir19_experiments_biomed/with_pos_boosters.json").getFile());
        final File withPositiveAndNegativeBoosters = new File(
                PubmedExperimenter.class.getResource("/templates/sigir19_experiments_biomed/with_pos_neg_boosters.json").getFile());
        final File withPosNegBoostersAdditionalSignals = new File(
                PubmedExperimenter.class.getResource("/templates/sigir19_experiments_biomed/with_pos_neg_boosters_additional_signals.json").getFile());
        final File noClassifierTemplate = new File(
                PubmedExperimenter.class.getResource("/templates/biomedical_articles/hpipubnone.json").getFile());
        final File gsClassifierTemplate = new File(
                PubmedExperimenter.class.getResource("/templates/sigir19_experiments_biomed/hpipubgspm.json").getFile());
        final File mustClassifierTemplate = new File(
                PubmedExperimenter.class.getResource("/templates/sigir19_experiments_biomed/hpipubboost_must.json").getFile());


        ExperimentsBuilder builder = new ExperimentsBuilder();

        builder.newExperiment().withName("baseline").withYear(year).withGoldStandard(goldStandard).withTarget(target)
                .withSubTemplate(baseline, templateProperties);

        builder.newExperiment().withName("baselinewr").withYear(year).withGoldStandard(goldStandard).withTarget(target)
                .withSubTemplate(baseline, templateProperties).withWordRemoval();

        builder.newExperiment().withName("diseases").withYear(year).withGoldStandard(goldStandard).withTarget(target)
                .withSubTemplate(baseline, templateProperties).withWordRemoval().withDiseasePreferredTerm().withDiseaseSynonym();

        builder.newExperiment().withName("diseaseslptb").withYear(year).withGoldStandard(goldStandard).withTarget(target)
                .withSubTemplate(baseline, templateProperties).withWordRemoval().withDiseasePreferredTerm().withDiseaseSynonym();

        builder.newExperiment().withName("genesnodesc").withYear(year).withGoldStandard(goldStandard).withTarget(target)
                .withSubTemplate(baseline, templateProperties).withWordRemoval().withGeneSynonym();

        builder.newExperiment().withName("genes").withYear(year).withGoldStandard(goldStandard).withTarget(target)
                .withSubTemplate(baseline, templateProperties).withWordRemoval().withGeneSynonym().withGeneDescription();

        builder.newExperiment().withName("genesplus").withYear(year).withGoldStandard(goldStandard).withTarget(target)
                .withSubTemplate(baselinePlusGeneField, templateProperties).withWordRemoval().withGeneSynonym().withGeneDescription();

        builder.newExperiment().withName("genesonly").withYear(year).withGoldStandard(goldStandard).withTarget(target)
                .withSubTemplate(baselineOnlyGeneField, templateProperties).withWordRemoval().withGeneSynonym().withGeneDescription();

        builder.newExperiment().withName("genedis").withYear(year).withGoldStandard(goldStandard).withTarget(target)
                .withSubTemplate(baseline, templateProperties).withWordRemoval().withDiseasePreferredTerm().withDiseaseSynonym().withGeneSynonym().withGeneDescription();

        builder.newExperiment().withName("genedispb").withYear(year).withGoldStandard(goldStandard).withTarget(target)
                .withSubTemplate(withPositiveBoosters, templateProperties).withWordRemoval().withDiseasePreferredTerm().withDiseaseSynonym().withGeneSynonym().withGeneDescription();

        builder.newExperiment().withName("genespb").withYear(year).withGoldStandard(goldStandard).withTarget(target)
                .withSubTemplate(withPositiveBoosters, templateProperties).withWordRemoval().withGeneSynonym().withGeneDescription();

        builder.newExperiment().withName("genedispb").withYear(year).withGoldStandard(goldStandard).withTarget(target)
                .withSubTemplate(withPositiveBoosters, templateProperties).withWordRemoval().withGeneSynonym().withGeneDescription().withDiseaseSynonym().withDiseasePreferredTerm();

        builder.newExperiment().withName("genedispbnb").withYear(year).withGoldStandard(goldStandard).withTarget(target)
                .withSubTemplate(withPositiveAndNegativeBoosters, templateProperties).withWordRemoval().withGeneSynonym().withGeneDescription().withDiseaseSynonym().withDiseasePreferredTerm();

        builder.newExperiment().withName("genedisbstadd").withYear(year).withGoldStandard(goldStandard).withTarget(target)
                .withSubTemplate(withPosNegBoostersAdditionalSignals, templateProperties).withWordRemoval().withGeneSynonym().withGeneDescription().withDiseaseSynonym().withDiseasePreferredTerm();

        builder.newExperiment().withName("hpipubboost_must").withYear(year).withGoldStandard(goldStandard).withTarget(target)
                .withSubTemplate(mustClassifierTemplate).withWordRemoval().withGeneSynonym()
                .withDiseasePreferredTerm().withGeneDescription().withDiseaseSynonym();

        builder.newExperiment().withName("hpipubgspm").withYear(year).withGoldStandard(goldStandard).withTarget(target)
                .withSubTemplate(gsClassifierTemplate, "pm_gs_boost", "1").withWordRemoval().withGeneSynonym()
                .withDiseasePreferredTerm().withGeneDescription().withDiseaseSynonym();

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
