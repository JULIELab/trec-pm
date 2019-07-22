package at.medunigraz.imi.bst.trec.experiment.registry;

import at.medunigraz.imi.bst.trec.experiment.TrecPmRetrieval;
import at.medunigraz.imi.bst.trec.model.Task;

import java.io.File;

public final class ScientificArticlesRetrievalRegistry {

    private static final File IMPROVED_TEMPLATE = new File(
            ScientificArticlesRetrievalRegistry.class.getResource("/templates/biomedical_articles/hpipubboost.json").getFile());
    private static final File NONE_TEMPLATE = new File(
            ScientificArticlesRetrievalRegistry.class.getResource("/templates/biomedical_articles/hpipubnone.json").getFile());
    private static final File EXTRA_BOOST_TEMPLATE = new File(
            ScientificArticlesRetrievalRegistry.class.getResource("/templates/biomedical_articles/hpipubclass.json").getFile());

    public static TrecPmRetrieval hpipubclass(int year, int size) {
        return new TrecPmRetrieval().withExperimentName("hpipubclass").withSize(size).withYear(year).withTarget(Task.PUBMED)
                .withSubTemplate(EXTRA_BOOST_TEMPLATE).withWordRemoval().withGeneSynonym()
                .withDiseasePreferredTerm().withGeneDescription().withDiseaseSynonym();
    }

    public static TrecPmRetrieval hpipubnone(int year, int size) {
        return new TrecPmRetrieval().withExperimentName("hpipubnone").withSize(size).withYear(year).withTarget(Task.PUBMED)
                .withSubTemplate(NONE_TEMPLATE).withWordRemoval().withGeneSynonym()
                .withDiseasePreferredTerm().withGeneDescription().withDiseaseSynonym();
    }

    public static TrecPmRetrieval hpipubboost(int year, int size) {
        return new TrecPmRetrieval().withExperimentName("hpipubboost").withSize(size).withYear(year).withTarget(Task.PUBMED)
                .withSubTemplate(IMPROVED_TEMPLATE).withWordRemoval().withGeneSynonym()
                .withDiseasePreferredTerm().withGeneDescription().withDiseaseSynonym();
    }

    public static TrecPmRetrieval hpipubcommon(int year, int size) {
        return new TrecPmRetrieval().withExperimentName("hpipubcommon").withSize(size).withYear(year).withTarget(Task.PUBMED)
                .withSubTemplate(NONE_TEMPLATE).withWordRemoval().withGeneSynonym()
                .withDiseasePreferredTerm().withDiseaseSynonym();
    }

    public static TrecPmRetrieval hpipubbase(int year, int size) {
        return new TrecPmRetrieval().withExperimentName("hpipubbase").withSize(size).withYear(year).withTarget(Task.PUBMED)
                .withSubTemplate(NONE_TEMPLATE);
    }

}
