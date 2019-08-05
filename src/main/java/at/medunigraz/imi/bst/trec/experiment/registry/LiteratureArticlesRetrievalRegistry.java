package at.medunigraz.imi.bst.trec.experiment.registry;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.retrieval.StoredFieldsRegistry;
import at.medunigraz.imi.bst.trec.experiment.TrecPmRetrieval;
import at.medunigraz.imi.bst.trec.model.Challenge;
import at.medunigraz.imi.bst.trec.model.Task;

import java.io.File;

public final class LiteratureArticlesRetrievalRegistry {

    private static final File IMPROVED_TEMPLATE = new File(
            LiteratureArticlesRetrievalRegistry.class.getResource("/templates/biomedical_articles/hpipubboost.json").getFile());
    private static final File NONE_TEMPLATE = new File(
            LiteratureArticlesRetrievalRegistry.class.getResource("/templates/biomedical_articles/hpipubnone.json").getFile());
    private static final File EXTRA_BOOST_TEMPLATE = new File(
            LiteratureArticlesRetrievalRegistry.class.getResource("/templates/biomedical_articles/hpipubclass.json").getFile());
    private static final File KEYWORD_TEMPLATE = new File(
            LiteratureArticlesRetrievalRegistry.class.getResource("/templates/biomedical_articles/keyword.json").getFile());
    private static final File BOOST_TEMPLATE = new File(
            LiteratureArticlesRetrievalRegistry.class.getResource("/templates/biomedical_articles/boost.json").getFile());
    private static final File JULIE_COMMON_TEMPLATE = new File(
            LiteratureArticlesRetrievalRegistry.class.getResource("/templates/biomedical_articles/jlpmcommon.json").getFile());
    private static final File JULIE_BOOST_TEMPLATE = new File(
            LiteratureArticlesRetrievalRegistry.class.getResource("/templates/biomedical_articles/jlpmboost.json").getFile());
    private static final File SYNONYMS = new File(
            ClinicalTrialsRetrievalRegistry.class.getResource("/synonyms/trec-synonyms.txt").getFile());

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

}
