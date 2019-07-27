package at.medunigraz.imi.bst.trec.experiment.registry;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.trec.experiment.TrecPmRetrieval;

import java.io.File;

public final class ClinicalTrialsRetrievalRegistry {

    private static final File IMPROVED_TEMPLATE = new File(
            ClinicalTrialsRetrievalRegistry.class.getResource("/templates/clinical_trials/hpictboost.json").getFile());
    private static final File PHRASE_TEMPLATE = new File(
            ClinicalTrialsRetrievalRegistry.class.getResource("/templates/clinical_trials/hpictphrase.json").getFile());

    public static TrecPmRetrieval hpictall(int size) {
        return new TrecPmRetrieval(TrecConfig.ELASTIC_CT_INDEX).withExperimentName("hpictall").withSize(size)
                .withSubTemplate(IMPROVED_TEMPLATE).withWordRemoval().withSolidTumor().withDiseasePreferredTerm()
                .withDiseaseSynonym().withGeneSynonym().withGeneDescription().withGeneFamily();
    }

    public static TrecPmRetrieval hpictphrase(int size) {
        return new TrecPmRetrieval(TrecConfig.ELASTIC_CT_INDEX).withExperimentName("hpictphrase").withSize(size)
                .withSubTemplate(PHRASE_TEMPLATE).withWordRemoval().withSolidTumor().withDiseasePreferredTerm()
                .withDiseaseSynonym().withGeneSynonym().withGeneFamily();
    }

    public static TrecPmRetrieval hpictboost(int size) {
        return new TrecPmRetrieval(TrecConfig.ELASTIC_CT_INDEX).withExperimentName("hpictboost").withSize(size)
                .withSubTemplate(IMPROVED_TEMPLATE).withWordRemoval().withSolidTumor().withDiseasePreferredTerm()
                .withDiseaseSynonym().withGeneSynonym().withGeneFamily();
    }

    public static TrecPmRetrieval hpictcommon(int size) {
        return new TrecPmRetrieval(TrecConfig.ELASTIC_CT_INDEX).withExperimentName("hpictcommon").withSize(size)
                .withSubTemplate(IMPROVED_TEMPLATE).withWordRemoval().withDiseasePreferredTerm().withDiseaseSynonym()
                .withGeneSynonym();
    }

    public static TrecPmRetrieval hpictbase(int size) {
        return new TrecPmRetrieval(TrecConfig.ELASTIC_CT_INDEX).withExperimentName("hpictbase").withSize(size)
                .withSubTemplate(IMPROVED_TEMPLATE);
    }
}
