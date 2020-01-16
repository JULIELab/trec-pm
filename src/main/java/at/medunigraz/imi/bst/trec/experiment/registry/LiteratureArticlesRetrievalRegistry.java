package at.medunigraz.imi.bst.trec.experiment.registry;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.retrieval.StoredFieldsRegistry;
import at.medunigraz.imi.bst.trec.experiment.TrecPmRetrieval;
import at.medunigraz.imi.bst.trec.model.Challenge;
import at.medunigraz.imi.bst.trec.model.Task;
import de.julielab.ir.es.BM25Parameters;
import de.julielab.ir.es.SimilarityParameters;
import de.julielab.ir.ltr.features.FeatureControlCenter;
import de.julielab.ir.ltr.features.PMFCConstants;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

import java.util.HashMap;
import java.util.Map;

import static de.julielab.ir.ltr.features.FCConstants.TEMPLATEPARAMETERS;
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
                .withConditionalCancer();
    }

    public static TrecPmRetrieval jlpmcommon2umls(int size) {
        return new TrecPmRetrieval(TrecConfig.ELASTIC_BA_INDEX, size).withExperimentName("jlpmcommon2umls")
                .withSubTemplate(JULIE_COMMON2_TEMPLATE)
                .withWordRemoval().withGeneSynonym()
                .withUmlsDiseasePreferredTerm().withUmlsDiseaseSynonym().withSynonymList(SYNONYMS_FILE)
                .withConditionalCancer();
    }

    public static TrecPmRetrieval jlpmcommon2withumlshypernym(int size) {
        return new TrecPmRetrieval(TrecConfig.ELASTIC_BA_INDEX, size).withExperimentName("jlpmcommon2umlsdishyp")
                .withSubTemplate(JULIE_COMMON2_TEMPLATE)
                .withWordRemoval().withGeneSynonym()
                .withUmlsDiseasePreferredTerm().withUmlsDiseaseSynonym().withUmlsDiseaseHypernym().withSynonymList(SYNONYMS_FILE)
                .withConditionalCancer();
    }

    public static TrecPmRetrieval jlpmcommon2withlexigramhypernym(int size) {
        return new TrecPmRetrieval(TrecConfig.ELASTIC_BA_INDEX, size).withExperimentName("jlpmcommon2lexigramdishyp")
                .withSubTemplate(JULIE_COMMON2_TEMPLATE)
                .withWordRemoval().withGeneSynonym()
                .withUmlsDiseasePreferredTerm().withUmlsDiseaseSynonym().withDiseaseHypernym().withSynonymList(SYNONYMS_FILE)
                .withConditionalCancer();
    }

    public static TrecPmRetrieval jlpmcommon2withoutgenesynonyms(int size) {
        return new TrecPmRetrieval(TrecConfig.ELASTIC_BA_INDEX, size).withExperimentName("jlpmcommon2lwithoutgenesyn")
                .withSubTemplate(JULIE_COMMON2_TEMPLATE)
                .withWordRemoval()
                .withUmlsDiseasePreferredTerm().withUmlsDiseaseSynonym().withSynonymList(SYNONYMS_FILE)
                .withConditionalCancer();
    }

    public static TrecPmRetrieval jlpmcommon2withgenesdesc(int size) {
        return new TrecPmRetrieval(TrecConfig.ELASTIC_BA_INDEX, size).withExperimentName("jlpmcommon2lwithgenedesc")
                .withSubTemplate(JULIE_COMMON2_TEMPLATE)
                .withWordRemoval().withGeneSynonym().withGeneDescription()
                .withUmlsDiseasePreferredTerm().withUmlsDiseaseSynonym().withSynonymList(SYNONYMS_FILE)
                .withConditionalCancer();
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
                .withUmlsDiseasePreferredTerm().withUmlsDiseaseSynonym().withSynonymList(SYNONYMS_FILE)
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
    public static TrecPmRetrieval jlpmgeneric(int size, String instanceName, String indexSuffix) {
        FeatureControlCenter fcc = FeatureControlCenter.getInstance();
        HierarchicalConfiguration<ImmutableNode> conf = fcc.getFeatureConfiguration();

        TrecPmRetrieval ret = new TrecPmRetrieval(TrecConfig.ELASTIC_BA_INDEX, size)
                .withExperimentName("jlpmgeneric-"+instanceName).withIndexSuffix(indexSuffix);

        HierarchicalConfiguration<ImmutableNode> retrievalConfig = conf.configurationAt(RETRIEVALPARAMETERS);
        Map<String, String> templateProperties = new HashMap<>();
        // Default values for parameters that are not always activated but are still required to create a valid query
        templateProperties.put(DISEASE_SLOP, "1");
        templateProperties.put(DISEASE_PREFTERM_BOOST, "1");
        templateProperties.put(DISEASE_SYN_BOOST, "1");
        templateProperties.put(DISEASE_HYPERNYMS_BOOST, "1");
        templateProperties.put(GENE_TOPIC_SLOP, "1");
        templateProperties.put(GENE_SYN_SLOP, "1");
        templateProperties.put(GENE_DESC_SLOP, "1");
        templateProperties.put(GENE_SYN_BOOST, "1");
        templateProperties.put(GENE_DESC_BOOST, "1");
        templateProperties.put(DISEASE_CUSTOM_BOOST, "1");
        templateProperties.put(GENE_CUSTOM_BOOST, "1");

        // Reading parameters from the configuration
        templateProperties.put("positive_pm_boosters", FeatureControlCenter.getKeywordStringFromFeatureConfiguration(retrievalConfig, slash(KEYWORDS, POSITIVEPM)));
        templateProperties.put("negative_pm_boosters", FeatureControlCenter.getKeywordStringFromFeatureConfiguration(retrievalConfig, slash(KEYWORDS, NEGATIVEPM)));
        // Field boosts
        templateProperties.putAll(FeatureControlCenter.getValuesFromFeatureConfiguration(retrievalConfig, slash(TEMPLATEPARAMETERS, FIELDBOOSTS)));
        // Keyword clause boosts
        templateProperties.putAll(FeatureControlCenter.getValuesFromFeatureConfiguration(retrievalConfig, slash(TEMPLATEPARAMETERS, CLAUSEBOOSTS)));
        // Disease (sub)query boosts
        templateProperties.putAll(FeatureControlCenter.getValuesFromFeatureConfiguration(retrievalConfig, slash(TEMPLATEPARAMETERS, DISEASE, BOOSTS)));
        // Disease (sub)query match types
        templateProperties.putAll(FeatureControlCenter.getValuesFromFeatureConfiguration(retrievalConfig, slash(TEMPLATEPARAMETERS, DISEASE, MATCHTYPES)));
        // Disease phrase slop
        templateProperties.putAll(FeatureControlCenter.getValuesFromFeatureConfiguration(retrievalConfig, slash(TEMPLATEPARAMETERS, DISEASE, PHRASESLOPS)));

        // Gene (sub)query boosts
        templateProperties.putAll(FeatureControlCenter.getValuesFromFeatureConfiguration(retrievalConfig, slash(TEMPLATEPARAMETERS, GENE, BOOSTS)));
        // Gene (sub)query match types
        templateProperties.putAll(FeatureControlCenter.getValuesFromFeatureConfiguration(retrievalConfig, slash(TEMPLATEPARAMETERS, GENE, MATCHTYPES)));
        // Gene query clauses phrase slopts
        templateProperties.putAll(FeatureControlCenter.getValuesFromFeatureConfiguration(retrievalConfig, slash(TEMPLATEPARAMETERS, GENE, PHRASESLOPS)));
        ret.withProperties(templateProperties);

        ret.withSubTemplate(conf.getString(slash(RETRIEVALPARAMETERS, TEMPLATE)));

        if (retrievalConfig.getBoolean(QUERYFILTERING))
            ret.withWordRemoval();
        if (retrievalConfig.getBoolean(slash(GENEEXPANSION, SYNONYMS)))
            ret.withGeneSynonym();
//        if (retrievalConfig.getBoolean(slash(GENEEXPANSION, DESCRIPTION)))
            ret.withGeneDescription();
        if (retrievalConfig.getBoolean(slash(GENEEXPANSION, HYPERNYMS)))
            ret.withGeneFamily();
        if (retrievalConfig.getBoolean(slash(DISEASEEXPANSION, PREFERREDTERM)))
            ret.withUmlsDiseasePreferredTerm();
        if (retrievalConfig.getBoolean(slash(DISEASEEXPANSION, SYNONYMS))) {
            ret.withUmlsDiseaseSynonym();
        }
        if (retrievalConfig.getBoolean(slash(DISEASEEXPANSION, HYPERNYMS)))
            ret.withUmlsDiseaseHypernym();
        if (retrievalConfig.getBoolean(slash(DISEASEEXPANSION, CUSTOM)))
            ret.withSolidTumor();

        System.err.println("WARN: Similarity changing is currently disabled!!!");
//        SimilarityParameters similarityParameters = new BM25Parameters(conf.getDouble(slash(INDEXPARAMETERS, BM25, K1)), conf.getDouble(slash(INDEXPARAMETERS, BM25, B)));
//        ret.withSimilarityParameters(similarityParameters);

        return ret;
    }

    public static TrecPmRetrieval jlpmcommon2paramopt(int size) {
        FeatureControlCenter fcc = FeatureControlCenter.getInstance();
        HierarchicalConfiguration<ImmutableNode> conf = fcc.getFeatureConfiguration();

        String matchAllBoost = conf.getString(slash(RETRIEVALPARAMETERS, PMFCConstants.TEMPLATEPARAMETERS, MATCH_ALL_BOOST));
        String negKeywordsBoost = conf.getString(slash(RETRIEVALPARAMETERS, PMFCConstants.TEMPLATEPARAMETERS, NEG_KEYWORDS_BOOST));
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
