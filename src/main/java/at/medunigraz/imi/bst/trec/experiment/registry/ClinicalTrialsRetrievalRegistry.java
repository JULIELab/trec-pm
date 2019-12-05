package at.medunigraz.imi.bst.trec.experiment.registry;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.trec.experiment.TrecPmRetrieval;

import java.io.File;

public final class ClinicalTrialsRetrievalRegistry {

    private static final String IMPROVED_TEMPLATE ="/templates/clinical_trials/hpictboost.json";
    private static final String PHRASE_TEMPLATE = "/templates/clinical_trials/hpictphrase.json";
    private static final String JULIE_PHRASE_TEMPLATE = "/templates/clinical_trials/jlctphrase.json";
    private static final String JULIE_PREC_TEMPLATE = "/templates/clinical_trials/jlctprec.json";
    private static final String JULIE_GENES_TEMPLATE = "/templates/clinical_trials/jlctgenes.json";
    private static final String SYNONYMS ="/synonyms/trec-synonyms.txt";

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
                .withDiseaseSynonym().withGeneSynonym().withGeneFamily().withSynonymList(SYNONYMS)
                .withConditionalCancer();
    }

    public static TrecPmRetrieval jlctletor(int size) {
        return new TrecPmRetrieval(TrecConfig.ELASTIC_CT_INDEX, size).withExperimentName("jlctletor")
                .withSubTemplate(JULIE_PHRASE_TEMPLATE).withWordRemoval().withSolidTumor().withDiseasePreferredTerm()
                .withDiseaseSynonym().withGeneSynonym().withGeneFamily().withSynonymList(SYNONYMS)
                .withConditionalCancer();
    }

    public static TrecPmRetrieval jlctltrin(int size) {
        return new TrecPmRetrieval(TrecConfig.ELASTIC_CT_INDEX, size).withExperimentName("jlctltrin")
                .withSubTemplate(JULIE_PHRASE_TEMPLATE).withWordRemoval().withSolidTumor().withDiseasePreferredTerm()
                .withDiseaseSynonym().withGeneSynonym().withGeneFamily().withSynonymList(SYNONYMS);
    }

    public static TrecPmRetrieval jlctprec(int size) {
        return new TrecPmRetrieval(TrecConfig.ELASTIC_CT_INDEX, size).withExperimentName("jlctprec")
                .withSubTemplate(JULIE_PREC_TEMPLATE).withWordRemoval().withSolidTumor().withDiseasePreferredTerm()
                .withDiseaseSynonym().withGeneSynonym().withGeneFamily().withSynonymList(SYNONYMS)
                .withConditionalCancer();
    }

    public static TrecPmRetrieval jlctgenes(int size) {
        return new TrecPmRetrieval(TrecConfig.ELASTIC_CT_INDEX, size).withExperimentName("jlctgenes")
                .withSubTemplate(JULIE_GENES_TEMPLATE).withWordRemoval().withSolidTumor().withDiseasePreferredTerm()
                .withDiseaseSynonym().withGeneSynonym().withGeneFamily().withSynonymList(SYNONYMS)
                .withConditionalCancer();
    }
}
