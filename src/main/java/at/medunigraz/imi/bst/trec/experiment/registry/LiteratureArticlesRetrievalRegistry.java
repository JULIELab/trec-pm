package at.medunigraz.imi.bst.trec.experiment.registry;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.retrieval.StoredFieldsRegistry;
import at.medunigraz.imi.bst.trec.experiment.TrecPmRetrieval;
import at.medunigraz.imi.bst.trec.model.Challenge;
import at.medunigraz.imi.bst.trec.model.Task;
import de.julielab.ir.es.BM25Parameters;
import de.julielab.ir.es.SimilarityParameters;
import de.julielab.ir.ltr.features.FeatureControlCenter;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

import java.util.HashMap;
import java.util.Map;

import static de.julielab.ir.ltr.features.PMFCConstants.*;
import static de.julielab.java.utilities.ConfigurationUtilities.slash;

public final class LiteratureArticlesRetrievalRegistry {

    private static final String IMPROVED_TEMPLATE = "/templates/biomedical_articles/hpipubboost.json";
    private static final String NONE_TEMPLATE = "/templates/biomedical_articles/hpipubnone.json";
    private static final String EXTRA_BOOST_TEMPLATE = "/templates/biomedical_articles/hpipubclass.json";
    private static final String KEYWORD_TEMPLATE = "/templates/biomedical_articles/keyword.json";
    private static final String BOOST_TEMPLATE = "/templates/biomedical_articles/boost.json";
    private static final String JULIE_COMMON_TEMPLATE = "/templates/biomedical_articles/jlpmcommon.json";
    private static final String JULIE_COMMON2_TEMPLATE = "/templates/biomedical_articles/jlpmcommon2.json";
    private static final String JULIE_COMMON2_TEMPLATE_NO_EXTRA = "/templates/biomedical_articles/jlpmcommon2_noextra.json";
    private static final String JULIE_COMMON2_ONE_POS_CLAUSE_TEMPLATE = "/templates/biomedical_articles/jlpmcommon2_onepositiveclause.json";
    private static final String JULIE_BOOST_TEMPLATE = "/templates/biomedical_articles/jlpmboost.json";
    private static final String SYNONYMS_FILE = "/synonyms/trec-synonyms.txt";

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
                .withDiseasePreferredTerm().withDiseaseSynonym().withSynonymList(SYNONYMS_FILE)
                .withConditionalCancer();
    }

    public static TrecPmRetrieval jlpmcommon2(int size) {
        return new TrecPmRetrieval(TrecConfig.ELASTIC_BA_INDEX, size).withExperimentName("jlpmcommon2")
                .withSubTemplate(JULIE_COMMON2_TEMPLATE)
                .withWordRemoval().withGeneSynonym()
                .withDiseasePreferredTerm().withDiseaseSynonym().withSynonymList(SYNONYMS_FILE)
                .withConditionalCancer().withSimilarityParameters(new BM25Parameters(1,1));
    }

    public static TrecPmRetrieval jlpmcommon2noextra(int size) {
        return new TrecPmRetrieval(TrecConfig.ELASTIC_BA_INDEX, size).withExperimentName("jlpmcommon2")
                .withSubTemplate(JULIE_COMMON2_TEMPLATE_NO_EXTRA)
                .withWordRemoval().withGeneSynonym()
                .withDiseasePreferredTerm().withDiseaseSynonym().withSynonymList(SYNONYMS_FILE)
                .withConditionalCancer();
    }

    public static TrecPmRetrieval jlpmcommon2OnepositiveClause(int size) {
        return new TrecPmRetrieval(TrecConfig.ELASTIC_BA_INDEX, size).withExperimentName("jlpmcommon21pos")
                .withSubTemplate(JULIE_COMMON2_ONE_POS_CLAUSE_TEMPLATE)
                .withWordRemoval().withGeneSynonym()
                .withDiseasePreferredTerm().withDiseaseSynonym().withSynonymList(SYNONYMS_FILE)
                .withConditionalCancer();
    }

    public static TrecPmRetrieval jlpmletor(int size) {
        return new TrecPmRetrieval(TrecConfig.ELASTIC_BA_INDEX, size).withExperimentName("jlpmletor")
                .withSubTemplate(JULIE_COMMON_TEMPLATE)
                .withWordRemoval().withGeneSynonym()
                .withDiseasePreferredTerm().withDiseaseSynonym().withSynonymList(SYNONYMS_FILE)
                .withStoredFields(Challenge.TREC_PM, Task.PUBMED, 2019)
                .withConditionalCancer();
    }

    public static TrecPmRetrieval jlpmltrin(int size) {
        return new TrecPmRetrieval(TrecConfig.ELASTIC_BA_INDEX, size).withExperimentName("jlpmltrin")
                .withSubTemplate(JULIE_COMMON_TEMPLATE)
                .withWordRemoval().withGeneSynonym()
                .withDiseasePreferredTerm().withDiseaseSynonym().withSynonymList(SYNONYMS_FILE)
                .withStoredFields(Challenge.TREC_PM, Task.PUBMED, 2019)
                .withConditionalCancer();
    }

    public static TrecPmRetrieval jlpmtrcommon(int size) {
        // XXX Results should be reranked via TreatmentRanker, see LiteratureArticlesExperimenter.java
        return new TrecPmRetrieval(TrecConfig.ELASTIC_BA_INDEX, size).withExperimentName("jlpmtrcommon")
                .withSubTemplate(JULIE_COMMON_TEMPLATE)
                .withStoredFields(StoredFieldsRegistry.getStoredFields(Challenge.TREC_PM, Task.PUBMED, 2019))
                .withWordRemoval().withGeneSynonym()
                .withDiseasePreferredTerm().withDiseaseSynonym().withSynonymList(SYNONYMS_FILE)
                .withConditionalCancer();
    }

    public static TrecPmRetrieval jlpmtrboost(int size) {
        // XXX Results should be reranked via TreatmentRanker, see LiteratureArticlesExperimenter.java
        return new TrecPmRetrieval(TrecConfig.ELASTIC_BA_INDEX, size).withExperimentName("jlpmtrboost")
                .withSubTemplate(JULIE_BOOST_TEMPLATE)
                .withStoredFields(StoredFieldsRegistry.getStoredFields(Challenge.TREC_PM, Task.PUBMED, 2019))
                .withWordRemoval().withGeneSynonym()
                .withDiseasePreferredTerm().withDiseaseSynonym().withSynonymList(SYNONYMS_FILE)
                .withConditionalCancer();
    }

    /**
     * Uses heavily parametrized templates for use with parameter optimization.
     *
     * @param size
     * @return
     */
    public static TrecPmRetrieval jlpmcommon2Generic(int size) {
        FeatureControlCenter fcc = FeatureControlCenter.getInstance();
        HierarchicalConfiguration<ImmutableNode> conf = fcc.getFeatureConfiguration();

        TrecPmRetrieval ret = new TrecPmRetrieval(TrecConfig.ELASTIC_BA_INDEX, size)
                .withExperimentName("jlpmcommon2gen")
                .withSubTemplate(conf.getString(slash(RETRIEVALPARAMETERS, TEMPLATE)));

        HierarchicalConfiguration<ImmutableNode> retrievalConfig = conf.configurationAt(RETRIEVALPARAMETERS);
        if (retrievalConfig.getBoolean(QUERYFILTERING))
            ret.withWordRemoval();
        if (retrievalConfig.getBoolean(slash(GENEEXPANSION, SYNONYMS)))
            ret.withGeneSynonym();
        if (retrievalConfig.getBoolean(slash(GENEEXPANSION, DESCRIPTION)))
            ret.withGeneDescription();
        if (retrievalConfig.getBoolean(SYNONYMLIST))
            ret.withSynonymList(SYNONYMS_FILE);
        if (retrievalConfig.getBoolean(slash(DISEASEEXPANSION, PREFERREDTERM)))
            ret.withUmlsDiseasePreferredTerm();
        if (retrievalConfig.getBoolean(slash(DISEASEEXPANSION, SYNONYMS)))
            ret.withUmlsDiseaseSynonym();
        if (retrievalConfig.getBoolean(slash(DISEASEEXPANSION, HYPERNYMS)))
            ret.withUmlsDiseaseHypernym();


        Map<String, String> templateProperties = new HashMap<>();
        templateProperties.put("positive_pm_boosters", FeatureControlCenter.getKeywordStringFromFeatureConfiguration(retrievalConfig, slash(KEYWORDS, POSITIVEPM)));
        templateProperties.put("negative_pm_boosters", FeatureControlCenter.getKeywordStringFromFeatureConfiguration(retrievalConfig, slash(KEYWORDS, NEGATIVEPM)));
        ret.withProperties(templateProperties);

        // The decorator is always added but it internally checks which keywords are active, if any.
        // Without active keywords, this does nothing.
        ret.withFeatureControlledConditionalCancer();

        // TODO: field weights
        // TODO query weights

        SimilarityParameters similarityParameters = new BM25Parameters(conf.getDouble(slash(INDEXPARAMETERS, BM25, K1)), conf.getDouble(slash(INDEXPARAMETERS, BM25, B)));
        ret.withSimilarityParameters(similarityParameters);

        return ret;
    }

    public static TrecPmRetrieval jlpmcommon2paramopt(int size) {
        FeatureControlCenter fcc = FeatureControlCenter.getInstance();
        HierarchicalConfiguration<ImmutableNode> conf = fcc.getFeatureConfiguration();

        String matchAllBoost = conf.getString(slash(RETRIEVALPARAMETERS, TEMPLATEPARAMETERS, MATCH_ALL_BOOST));
        String negKeywordsBoost = conf.getString(slash(RETRIEVALPARAMETERS, TEMPLATEPARAMETERS, NEG_KEYWORDS_BOOST));
        TrecPmRetrieval ret = new TrecPmRetrieval(TrecConfig.ELASTIC_BA_INDEX, size)
                .withExperimentName("jlpmcommon2");
        ret.withProperties(MATCH_ALL_BOOST, matchAllBoost, NEG_KEYWORDS_BOOST, negKeywordsBoost)
                .withSubTemplate(conf.getString(slash(RETRIEVALPARAMETERS, TEMPLATE)));
        ret.withWordRemoval();
        ret.withGeneSynonym();
        ret.withSynonymList(SYNONYMS_FILE);
        ret.withDiseasePreferredTerm();
        ret.withDiseaseSynonym();

        // The decorator is always added but it internally checks which keywords are active, if any.
        // Without active keywords, this does nothing.
        ret.withConditionalCancer();


        return ret;
    }

}
