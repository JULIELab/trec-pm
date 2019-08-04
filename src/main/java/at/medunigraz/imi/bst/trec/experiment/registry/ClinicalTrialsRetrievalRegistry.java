package at.medunigraz.imi.bst.trec.experiment.registry;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.trec.experiment.TrecPmRetrieval;

import java.io.File;

public final class ClinicalTrialsRetrievalRegistry {

    private static final File IMPROVED_TEMPLATE = new File(
            ClinicalTrialsRetrievalRegistry.class.getResource("/templates/clinical_trials/hpictboost.json").getFile());
    private static final File PHRASE_TEMPLATE = new File(
            ClinicalTrialsRetrievalRegistry.class.getResource("/templates/clinical_trials/hpictphrase.json").getFile());
    private static final File JULIE_PHRASE_TEMPLATE = new File(
            ClinicalTrialsRetrievalRegistry.class.getResource("/templates/clinical_trials/jlctphrase.json").getFile());
    private static final File SYNONYMS = new File(
            ClinicalTrialsRetrievalRegistry.class.getResource("/synonyms/trec-synonyms.txt").getFile());

    public static TrecPmRetrieval hpictall(int size) {
        return new TrecPmRetrieval(TrecConfig.ELASTIC_CT_INDEX, size).withExperimentName("hpictall")
                .withSubTemplate(IMPROVED_TEMPLATE).withWordRemoval().withSolidTumor().withDiseasePreferredTerm()
                .withDiseaseSynonym().withGeneSynonym().withGeneDescription().withGeneFamily();
    }

    public static TrecPmRetrieval hpictphrase(int size) {
        return new TrecPmRetrieval(TrecConfig.ELASTIC_CT_INDEX, size).withExperimentName("hpictphrase")
                .withSubTemplate(PHRASE_TEMPLATE).withWordRemoval().withSolidTumor().withDiseasePreferredTerm()
                .withDiseaseSynonym().withGeneSynonym().withGeneFamily();
    }

    public static TrecPmRetrieval hpictboost(int size) {
        return new TrecPmRetrieval(TrecConfig.ELASTIC_CT_INDEX, size).withExperimentName("hpictboost")
                .withSubTemplate(IMPROVED_TEMPLATE).withWordRemoval().withSolidTumor().withDiseasePreferredTerm()
                .withDiseaseSynonym().withGeneSynonym().withGeneFamily();
    }

    public static TrecPmRetrieval hpictcommon(int size) {
        return new TrecPmRetrieval(TrecConfig.ELASTIC_CT_INDEX, size).withExperimentName("hpictcommon")
                .withSubTemplate(IMPROVED_TEMPLATE).withWordRemoval().withDiseasePreferredTerm().withDiseaseSynonym()
                .withGeneSynonym();
    }

    public static TrecPmRetrieval hpictbase(int size) {
        return new TrecPmRetrieval(TrecConfig.ELASTIC_CT_INDEX, size).withExperimentName("hpictbase")
                .withSubTemplate(IMPROVED_TEMPLATE);
    }

    public static TrecPmRetrieval jlctphrase(int size) {
        return new TrecPmRetrieval(TrecConfig.ELASTIC_CT_INDEX, size).withExperimentName("jlctphrase")
                .withSubTemplate(JULIE_PHRASE_TEMPLATE).withWordRemoval().withSolidTumor().withDiseasePreferredTerm()
                .withDiseaseSynonym().withGeneSynonym().withGeneFamily().withSynonymList(SYNONYMS);
    }

    public static TrecPmRetrieval jlctletor(int size) {
        return new TrecPmRetrieval(TrecConfig.ELASTIC_CT_INDEX, size).withExperimentName("jlctletor")
                .withSubTemplate(JULIE_PHRASE_TEMPLATE).withWordRemoval().withSolidTumor().withDiseasePreferredTerm()
                .withDiseaseSynonym().withGeneSynonym().withGeneFamily().withSynonymList(SYNONYMS);
    }
}
