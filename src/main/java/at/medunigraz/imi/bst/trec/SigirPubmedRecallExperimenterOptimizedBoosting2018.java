package at.medunigraz.imi.bst.trec;

import at.medunigraz.imi.bst.trec.experiment.Experiment;
import at.medunigraz.imi.bst.trec.experiment.ExperimentsBuilder;

import java.io.File;
import java.util.Map;
import java.util.Set;

public class SigirPubmedRecallExperimenterOptimizedBoosting2018 extends SuperSigirPubmedRecallExperimenter {
    public static void main(String[] args) {

        final Experiment.GoldStandard goldStandard = Experiment.GoldStandard.OFFICIAL;
        final Experiment.Task target = Experiment.Task.PUBMED;
        final int year = 2018;


    Map<String, String> templateProperties = SigirParameters.SIGIR19_HPIPUBNONE_WEIGHTS;
        templateProperties.put("pm_class_field", "pmclass2017lstmgru.keyword" );
        ExperimentsBuilder builder = new ExperimentsBuilder();
        File all_nopmclass = new File(
                PubmedExperimenter.class.getResource("/templates/sigir19_finalruns/all_nopmclass.json").getFile());
        File all_pmclass_must = new File(
                PubmedExperimenter.class.getResource("/templates/sigir19_finalruns/all_pmclass_must.json").getFile());
        File all_pmclass_should = new File(
                PubmedExperimenter.class.getResource("/templates/sigir19_finalruns/all_pmclass_should.json").getFile());
        File baseline_pmclass_must = new File(
                PubmedExperimenter.class.getResource("/templates/sigir19_finalruns/baseline_pmclass_must.json").getFile());
        File baseline_pmclass_should = new File(
                PubmedExperimenter.class.getResource("/templates/sigir19_finalruns/baseline_pmclass_should.json").getFile());


        builder.newExperiment().withName("all_nopmclass").withYear(year).withGoldStandard(goldStandard).withTarget(target)
                .withSubTemplate(all_nopmclass, templateProperties).withGeneSynonym()
                .withDiseasePreferredTerm().withGeneDescription().withDiseaseSynonym();

        builder.newExperiment().withName("all_nopmclass_wr").withYear(year).withGoldStandard(goldStandard).withTarget(target)
                .withSubTemplate(all_nopmclass, templateProperties).withWordRemoval().withGeneSynonym()
                .withDiseasePreferredTerm().withGeneDescription().withDiseaseSynonym();
//
//        builder.newExperiment().withName("all_pmclass_must").withYear(year).withGoldStandard(goldStandard).withTarget(target)
//                .withSubTemplate(all_pmclass_must, templateProperties).withGeneSynonym()
//                .withDiseasePreferredTerm().withGeneDescription().withDiseaseSynonym();
//
//        builder.newExperiment().withName("all_pmclass_must_wr").withYear(year).withGoldStandard(goldStandard).withTarget(target)
//                .withSubTemplate(all_pmclass_must, templateProperties).withGeneSynonym().withWordRemoval()
//                .withDiseasePreferredTerm().withGeneDescription().withDiseaseSynonym();
//
//        builder.newExperiment().withName("all_pmclass_should").withYear(year).withGoldStandard(goldStandard).withTarget(target)
//                .withSubTemplate(all_pmclass_should, templateProperties).withGeneSynonym()
//                .withDiseasePreferredTerm().withGeneDescription().withDiseaseSynonym();
//
//        builder.newExperiment().withName("all_pmclass_should_wr").withYear(year).withGoldStandard(goldStandard).withTarget(target)
//                .withSubTemplate(all_pmclass_should, templateProperties).withGeneSynonym().withWordRemoval()
//                .withDiseasePreferredTerm().withGeneDescription().withDiseaseSynonym();
//
//        builder.newExperiment().withName("baseline_pmclass_must").withYear(year).withGoldStandard(goldStandard).withTarget(target)
//                .withSubTemplate(baseline_pmclass_must, templateProperties).withGeneSynonym()
//                .withDiseasePreferredTerm().withGeneDescription().withDiseaseSynonym();
//
//        builder.newExperiment().withName("baseline_pmclass_must_wr").withYear(year).withGoldStandard(goldStandard).withTarget(target)
//                .withSubTemplate(baseline_pmclass_must, templateProperties).withWordRemoval().withGeneSynonym()
//                .withDiseasePreferredTerm().withGeneDescription().withDiseaseSynonym();
//
//        builder.newExperiment().withName("baseline_pmclass_should").withYear(year).withGoldStandard(goldStandard).withTarget(target)
//                .withSubTemplate(baseline_pmclass_should, templateProperties).withGeneSynonym()
//                .withDiseasePreferredTerm().withGeneDescription().withDiseaseSynonym();
//
//        builder.newExperiment().withName("baseline_pmclass_should_wr").withYear(year).withGoldStandard(goldStandard).withTarget(target)
//                .withSubTemplate(baseline_pmclass_should, templateProperties).withWordRemoval().withGeneSynonym()
//                .withDiseasePreferredTerm().withGeneDescription().withDiseaseSynonym();

        Set<Experiment> experiments = builder.build();

        for (Experiment exp : experiments) {
            exp.start();
        }

        for (Experiment exp : experiments) {
            try {
                exp.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
