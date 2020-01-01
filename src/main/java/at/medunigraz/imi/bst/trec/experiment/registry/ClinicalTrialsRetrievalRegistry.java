package at.medunigraz.imi.bst.trec.experiment.registry;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.trec.experiment.TrecPmRetrieval;
import de.julielab.ir.es.BM25Parameters;
import de.julielab.ir.es.SimilarityParameters;
import de.julielab.ir.ltr.features.FeatureControlCenter;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

import java.util.HashMap;
import java.util.Map;

import static de.julielab.ir.ltr.features.FCConstants.*;
import static de.julielab.ir.ltr.features.PMFCConstants.TEMPLATEPARAMETERS;
import static de.julielab.ir.ltr.features.PMFCConstants.*;
import static de.julielab.java.utilities.ConfigurationUtilities.slash;

public final class ClinicalTrialsRetrievalRegistry {

    private static final String IMPROVED_TEMPLATE ="/templates/clinical_trials/hpictboost.json";
    private static final String PHRASE_TEMPLATE = "/templates/clinical_trials/hpictphrase.json";
    private static final String JULIE_PHRASE_TEMPLATE = "/templates/clinical_trials/jlctphrase.json";
    private static final String JULIE_PREC_TEMPLATE = "/templates/clinical_trials/jlctprec.json";
    private static final String JULIE_GENES_TEMPLATE = "/templates/clinical_trials/jlctgenes.json";
    private static final String SYNONYMS_FILE ="/synonyms/trec-synonyms.txt";

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
                .withDiseaseSynonym().withGeneSynonym().withGeneFamily().withSynonymList(SYNONYMS_FILE)
                .withConditionalCancer();
    }

    public static TrecPmRetrieval jlctletor(int size) {
        return new TrecPmRetrieval(TrecConfig.ELASTIC_CT_INDEX, size).withExperimentName("jlctletor")
                .withSubTemplate(JULIE_PHRASE_TEMPLATE).withWordRemoval().withSolidTumor().withDiseasePreferredTerm()
                .withDiseaseSynonym().withGeneSynonym().withGeneFamily().withSynonymList(SYNONYMS_FILE)
                .withConditionalCancer();
    }

    public static TrecPmRetrieval jlctltrin(int size) {
        return new TrecPmRetrieval(TrecConfig.ELASTIC_CT_INDEX, size).withExperimentName("jlctltrin")
                .withSubTemplate(JULIE_PHRASE_TEMPLATE).withWordRemoval().withSolidTumor().withDiseasePreferredTerm()
                .withDiseaseSynonym().withGeneSynonym().withGeneFamily().withSynonymList(SYNONYMS_FILE);
    }

    public static TrecPmRetrieval jlctprec(int size) {
        return new TrecPmRetrieval(TrecConfig.ELASTIC_CT_INDEX, size).withExperimentName("jlctprec")
                .withSubTemplate(JULIE_PREC_TEMPLATE).withWordRemoval().withSolidTumor().withDiseasePreferredTerm()
                .withDiseaseSynonym().withGeneSynonym().withGeneFamily().withSynonymList(SYNONYMS_FILE)
                .withConditionalCancer();
    }

    public static TrecPmRetrieval jlctgenes(int size) {
        return new TrecPmRetrieval(TrecConfig.ELASTIC_CT_INDEX, size).withExperimentName("jlctgenes")
                .withSubTemplate(JULIE_GENES_TEMPLATE).withWordRemoval().withSolidTumor().withUmlsDiseasePreferredTerm()
                .withUmlsDiseaseSynonym().withGeneSynonym().withGeneFamily().withSynonymList(SYNONYMS_FILE)
                .withConditionalCancer();
    }

    /**
     * Uses heavily parametrized templates for use with parameter optimization.
     *
     * @param size
     * @return
     */
    public static TrecPmRetrieval jlctgeneric(int size, String instance) {
        FeatureControlCenter fcc = FeatureControlCenter.getInstance();
        HierarchicalConfiguration<ImmutableNode> conf = fcc.getFeatureConfiguration();

        TrecPmRetrieval ret = new TrecPmRetrieval(TrecConfig.ELASTIC_CT_INDEX, size)
                .withExperimentName("jlctgeneric-"+instance);

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
        if (retrievalConfig.getBoolean(slash(GENEEXPANSION, DESCRIPTION)))
            ret.withGeneDescription();
        if (retrievalConfig.getBoolean(slash(GENEEXPANSION, CUSTOM)))
            ret.withSolidTumor();
        if (retrievalConfig.getBoolean(slash(GENEEXPANSION, HYPERNYMS)))
            ret.withGeneFamily();
        if (retrievalConfig.getBoolean(SYNONYMLIST))
            ret.withSynonymList(SYNONYMS_FILE);
        if (retrievalConfig.getBoolean(slash(DISEASEEXPANSION, PREFERREDTERM)))
            ret.withUmlsDiseasePreferredTerm();
        if (retrievalConfig.getBoolean(slash(DISEASEEXPANSION, SYNONYMS)))
            ret.withUmlsDiseaseSynonym();
        if (retrievalConfig.getBoolean(slash(DISEASEEXPANSION, HYPERNYMS)))
            ret.withUmlsDiseaseHypernym();
        // The decorator is always added but it internally checks which keywords are active, if any.
        // Without active keywords, this does nothing.
        ret.withFeatureControlledConditionalCancer();


        SimilarityParameters similarityParameters = new BM25Parameters(conf.getDouble(slash(INDEXPARAMETERS, BM25, K1)), conf.getDouble(slash(INDEXPARAMETERS, BM25, B)));
        ret.withSimilarityParameters(similarityParameters);

        return ret;
    }
}
