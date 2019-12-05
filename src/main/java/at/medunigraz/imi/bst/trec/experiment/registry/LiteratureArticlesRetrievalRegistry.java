package at.medunigraz.imi.bst.trec.experiment.registry;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.retrieval.StoredFieldsRegistry;
import at.medunigraz.imi.bst.trec.experiment.TrecPmRetrieval;
import at.medunigraz.imi.bst.trec.model.Challenge;
import at.medunigraz.imi.bst.trec.model.Task;

public final class LiteratureArticlesRetrievalRegistry {

    private static final String IMPROVED_TEMPLATE ="/templates/biomedical_articles/hpipubboost.json";
    private static final String NONE_TEMPLATE ="/templates/biomedical_articles/hpipubnone.json";
    private static final String EXTRA_BOOST_TEMPLATE ="/templates/biomedical_articles/hpipubclass.json";
    private static final String KEYWORD_TEMPLATE ="/templates/biomedical_articles/keyword.json";
    private static final String BOOST_TEMPLATE ="/templates/biomedical_articles/boost.json";
    private static final String JULIE_COMMON_TEMPLATE ="/templates/biomedical_articles/jlpmcommon.json";
    private static final String JULIE_COMMON2_TEMPLATE ="/templates/biomedical_articles/jlpmcommon2.json";
    private static final String JULIE_BOOST_TEMPLATE ="/templates/biomedical_articles/jlpmboost.json";
    private static final String SYNONYMS = "/synonyms/trec-synonyms.txt";

    public static TrecPmRetrieval hpipubclass(int size) {
        return new TrecPmRetrieval(TrecConfig.ELASTIC_BA_INDEX, size).withExperimentName("hpipubclass")
                .withSubTemplate(EXTRA_BOOST_TEMPLATE).withWordRemoval().withGeneSynonym()
                .withDiseasePreferredTerm().withGeneDescription().withDiseaseSynonym();
    }

    public static TrecPmRetrieval hpipubnone(int size) {
        return new TrecPmRetrieval(TrecConfig.ELASTIC_BA_INDEX, size).withExperimentName("hpipubnone")
                .withSubTemplate(NONE_TEMPLATE).withWordRemoval().withGeneSynonym()
                .withDiseasePreferredTerm().withGeneDescription().withDiseaseSynonym();
    }

    public static TrecPmRetrieval hpipubboost(int size) {
        return new TrecPmRetrieval(TrecConfig.ELASTIC_BA_INDEX, size).withExperimentName("hpipubboost")
                .withSubTemplate(IMPROVED_TEMPLATE).withWordRemoval().withGeneSynonym()
                .withDiseasePreferredTerm().withGeneDescription().withDiseaseSynonym();
    }

    public static TrecPmRetrieval hpipubcommon(int size) {
        return new TrecPmRetrieval(TrecConfig.ELASTIC_BA_INDEX, size).withExperimentName("hpipubcommon")
                .withSubTemplate(NONE_TEMPLATE).withWordRemoval().withGeneSynonym()
                .withDiseasePreferredTerm().withDiseaseSynonym();
    }

    public static TrecPmRetrieval hpipubbase(int size) {
        return new TrecPmRetrieval(TrecConfig.ELASTIC_BA_INDEX, size).withExperimentName("hpipubbase")
                .withSubTemplate(NONE_TEMPLATE);
    }

    public static TrecPmRetrieval keyword(int size, String keyword) {
        return new TrecPmRetrieval(TrecConfig.ELASTIC_BA_INDEX, size).withExperimentName(keyword)
                .withProperties("keyword", keyword).withTemplate(KEYWORD_TEMPLATE).withWordRemoval();
    }

    public static TrecPmRetrieval boost(int size, String boost) {
        return new TrecPmRetrieval(TrecConfig.ELASTIC_BA_INDEX, size)
                .withProperties("keyword", boost).withTemplate(BOOST_TEMPLATE).withWordRemoval();
    }

    public static TrecPmRetrieval jlpmcommon(int size) {
        return new TrecPmRetrieval(TrecConfig.ELASTIC_BA_INDEX, size).withExperimentName("jlpmcommon")
                .withSubTemplate(JULIE_COMMON_TEMPLATE)
                .withWordRemoval().withGeneSynonym()
                .withDiseasePreferredTerm().withDiseaseSynonym().withSynonymList(SYNONYMS)
                .withConditionalCancer();
    }

    public static TrecPmRetrieval jlpmcommon2(int size) {
        return new TrecPmRetrieval(TrecConfig.ELASTIC_BA_INDEX, size).withExperimentName("jlpmcommon2")
                .withSubTemplate(JULIE_COMMON2_TEMPLATE)
                .withWordRemoval().withGeneSynonym()
                .withDiseasePreferredTerm().withDiseaseSynonym().withSynonymList(SYNONYMS)
                .withConditionalCancer();
    }

    public static TrecPmRetrieval jlpmletor(int size) {
        return new TrecPmRetrieval(TrecConfig.ELASTIC_BA_INDEX, size).withExperimentName("jlpmletor")
                .withSubTemplate(JULIE_COMMON_TEMPLATE)
                .withWordRemoval().withGeneSynonym()
                .withDiseasePreferredTerm().withDiseaseSynonym().withSynonymList(SYNONYMS)
                .withStoredFields(Challenge.TREC_PM, Task.PUBMED, 2019)
                .withConditionalCancer();
    }

    public static TrecPmRetrieval jlpmltrin(int size) {
        return new TrecPmRetrieval(TrecConfig.ELASTIC_BA_INDEX, size).withExperimentName("jlpmltrin")
                .withSubTemplate(JULIE_COMMON_TEMPLATE)
                .withWordRemoval().withGeneSynonym()
                .withDiseasePreferredTerm().withDiseaseSynonym().withSynonymList(SYNONYMS)
                .withStoredFields(Challenge.TREC_PM, Task.PUBMED, 2019)
                .withConditionalCancer();
    }

    public static TrecPmRetrieval jlpmtrcommon(int size) {
        // XXX Results should be reranked via TreatmentRanker, see LiteratureArticlesExperimenter.java
        return new TrecPmRetrieval(TrecConfig.ELASTIC_BA_INDEX, size).withExperimentName("jlpmtrcommon")
                .withSubTemplate(JULIE_COMMON_TEMPLATE)
                .withStoredFields(StoredFieldsRegistry.getStoredFields(Challenge.TREC_PM, Task.PUBMED, 2019))
                .withWordRemoval().withGeneSynonym()
                .withDiseasePreferredTerm().withDiseaseSynonym().withSynonymList(SYNONYMS)
                .withConditionalCancer();
    }

    public static TrecPmRetrieval jlpmtrboost(int size) {
        // XXX Results should be reranked via TreatmentRanker, see LiteratureArticlesExperimenter.java
        return new TrecPmRetrieval(TrecConfig.ELASTIC_BA_INDEX, size).withExperimentName("jlpmtrboost")
                .withSubTemplate(JULIE_BOOST_TEMPLATE)
                .withStoredFields(StoredFieldsRegistry.getStoredFields(Challenge.TREC_PM, Task.PUBMED, 2019))
                .withWordRemoval().withGeneSynonym()
                .withDiseasePreferredTerm().withDiseaseSynonym().withSynonymList(SYNONYMS)
                .withConditionalCancer();
    }

    /**
     * Uses heavily parametrized templates for use with parameter optimization.
     * @param size
     * @return
     */
    public static TrecPmRetrieval jlpmcommon2Generic(int size) {
        return new TrecPmRetrieval(TrecConfig.ELASTIC_BA_INDEX, size).withExperimentName("jlpmcommon2")
                .withSubTemplate(JULIE_COMMON2_TEMPLATE)
                .withWordRemoval().withGeneSynonym()
                .withDiseasePreferredTerm().withDiseaseSynonym().withSynonymList(SYNONYMS)
                .withConditionalCancer();
    }

}
