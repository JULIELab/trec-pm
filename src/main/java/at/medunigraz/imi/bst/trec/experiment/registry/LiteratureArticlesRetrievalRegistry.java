package at.medunigraz.imi.bst.trec.experiment.registry;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.trec.experiment.TrecPmRetrieval;

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

    public static TrecPmRetrieval hpipubclass(int size) {
        return new TrecPmRetrieval(TrecConfig.ELASTIC_BA_INDEX).withExperimentName("hpipubclass").withSize(size)
                .withSubTemplate(EXTRA_BOOST_TEMPLATE).withWordRemoval().withGeneSynonym()
                .withDiseasePreferredTerm().withGeneDescription().withDiseaseSynonym();
    }

    public static TrecPmRetrieval hpipubnone(int size) {
        return new TrecPmRetrieval(TrecConfig.ELASTIC_BA_INDEX).withExperimentName("hpipubnone").withSize(size)
                .withSubTemplate(NONE_TEMPLATE).withWordRemoval().withGeneSynonym()
                .withDiseasePreferredTerm().withGeneDescription().withDiseaseSynonym();
    }

    public static TrecPmRetrieval hpipubboost(int size) {
        return new TrecPmRetrieval(TrecConfig.ELASTIC_BA_INDEX).withExperimentName("hpipubboost").withSize(size)
                .withSubTemplate(IMPROVED_TEMPLATE).withWordRemoval().withGeneSynonym()
                .withDiseasePreferredTerm().withGeneDescription().withDiseaseSynonym();
    }

    public static TrecPmRetrieval hpipubcommon(int size) {
        return new TrecPmRetrieval(TrecConfig.ELASTIC_BA_INDEX).withExperimentName("hpipubcommon").withSize(size)
                .withSubTemplate(NONE_TEMPLATE).withWordRemoval().withGeneSynonym()
                .withDiseasePreferredTerm().withDiseaseSynonym();
    }

    public static TrecPmRetrieval hpipubbase(int size) {
        return new TrecPmRetrieval(TrecConfig.ELASTIC_BA_INDEX).withExperimentName("hpipubbase").withSize(size)
                .withSubTemplate(NONE_TEMPLATE);
    }

    public static TrecPmRetrieval keyword(int size, String keyword) {
        return new TrecPmRetrieval(TrecConfig.ELASTIC_BA_INDEX).withExperimentName(keyword).withSize(size)
                .withProperties("keyword", keyword).withTemplate(KEYWORD_TEMPLATE).withWordRemoval();
    }

    public static TrecPmRetrieval boost(int size, String boost) {
        return new TrecPmRetrieval(TrecConfig.ELASTIC_BA_INDEX).withSize(size)
                .withProperties("keyword", boost).withTemplate(BOOST_TEMPLATE).withWordRemoval();
    }

}
