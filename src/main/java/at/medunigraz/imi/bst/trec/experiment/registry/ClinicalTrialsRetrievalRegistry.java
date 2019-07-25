package at.medunigraz.imi.bst.trec.experiment.registry;

import at.medunigraz.imi.bst.trec.experiment.TrecPmRetrieval;
import at.medunigraz.imi.bst.trec.model.Task;

import java.io.File;

public final class ClinicalTrialsRetrievalRegistry {

    private static final File IMPROVED_TEMPLATE = new File(
            ClinicalTrialsRetrievalRegistry.class.getResource("/templates/clinical_trials/hpictboost.json").getFile());
    private static final File PHRASE_TEMPLATE = new File(
            ClinicalTrialsRetrievalRegistry.class.getResource("/templates/clinical_trials/hpictphrase.json").getFile());

    public static TrecPmRetrieval hpictall(int year, int size) {
        return new TrecPmRetrieval().withExperimentName("hpictall").withSize(size).withYear(year).withTarget(Task.CLINICAL_TRIALS)
                .withSubTemplate(IMPROVED_TEMPLATE).withWordRemoval().withSolidTumor().withDiseasePreferredTerm()
                .withDiseaseSynonym().withGeneSynonym().withGeneDescription().withGeneFamily();
    }

    public static TrecPmRetrieval hpictphrase(int year, int size) {
        return new TrecPmRetrieval().withExperimentName("hpictphrase").withSize(size).withYear(year).withTarget(Task.CLINICAL_TRIALS)
                .withSubTemplate(PHRASE_TEMPLATE).withWordRemoval().withSolidTumor().withDiseasePreferredTerm()
                .withDiseaseSynonym().withGeneSynonym().withGeneFamily();
    }

    public static TrecPmRetrieval hpictboost(int year, int size) {
        return new TrecPmRetrieval().withExperimentName("hpictboost").withSize(size).withYear(year).withTarget(Task.CLINICAL_TRIALS)
                .withSubTemplate(IMPROVED_TEMPLATE).withWordRemoval().withSolidTumor().withDiseasePreferredTerm()
                .withDiseaseSynonym().withGeneSynonym().withGeneFamily();
    }

    public static TrecPmRetrieval hpictcommon(int year, int size) {
        return new TrecPmRetrieval().withExperimentName("hpictcommon").withSize(size).withYear(year).withTarget(Task.CLINICAL_TRIALS)
                .withSubTemplate(IMPROVED_TEMPLATE).withWordRemoval().withDiseasePreferredTerm().withDiseaseSynonym()
                .withGeneSynonym();
    }

    public static TrecPmRetrieval hpictbase(int year, int size) {
        return new TrecPmRetrieval().withExperimentName("hpictbase").withSize(size).withYear(year).withTarget(Task.CLINICAL_TRIALS)
                .withSubTemplate(IMPROVED_TEMPLATE);
    }
}