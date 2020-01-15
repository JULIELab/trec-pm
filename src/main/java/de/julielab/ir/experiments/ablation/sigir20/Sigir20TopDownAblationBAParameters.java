package de.julielab.ir.experiments.ablation.sigir20;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Sigir20TopDownAblationBAParameters extends LinkedHashMap<String, Map<String, String>> {
    public Sigir20TopDownAblationBAParameters() {
        put("-DISEXP", params(
                "retrievalparameters.diseaseexpansion.custom", "false",
                "retrievalparameters.diseaseexpansion.hypernyms", "false",
                "retrievalparameters.diseaseexpansion.preferredterm", "false",
                "retrievalparameters.diseaseexpansion.synonyms", "false"
        ));
        put("-DISEXP+DISSYN", params(
                "retrievalparameters.diseaseexpansion.custom", "false",
                "retrievalparameters.diseaseexpansion.hypernyms", "false",
                "retrievalparameters.diseaseexpansion.preferredterm", "false",
                "retrievalparameters.diseaseexpansion.synonyms", "true"
        ));
        put("-DISEXP+DISHYP", params(
                "retrievalparameters.diseaseexpansion.custom", "false",
                "retrievalparameters.diseaseexpansion.hypernyms", "true",
                "retrievalparameters.diseaseexpansion.preferredterm", "false",
                "retrievalparameters.diseaseexpansion.synonyms", "false"
        ));
        put("-GENEXP", params(
                "retrievalparameters.geneexpansion.custom", "false",
                "retrievalparameters.geneexpansion.description", "false",
                "retrievalparameters.geneexpansion.hypernyms", "false",
                "retrievalparameters.geneexpansion.synonyms", "false"
        ));
        put("-GENEXP+GENESYN", params(
                "retrievalparameters.geneexpansion.custom", "false",
                "retrievalparameters.geneexpansion.description", "false",
                "retrievalparameters.geneexpansion.hypernyms", "false",
                "retrievalparameters.geneexpansion.synonyms", "true"
        ));
        put("-GENEXP+GENEDESC", params(
                "retrievalparameters.geneexpansion.custom", "false",
                "retrievalparameters.geneexpansion.description", "true",
                "retrievalparameters.geneexpansion.hypernyms", "false",
                "retrievalparameters.geneexpansion.synonyms", "false"
        ));
        put("-GENEXP+FAMILIES", params(
                "retrievalparameters.geneexpansion.custom", "true",
                "retrievalparameters.geneexpansion.description", "false",
                "retrievalparameters.geneexpansion.hypernyms", "false",
                "retrievalparameters.geneexpansion.synonyms", "false"
        ));
        put("-BM25", params(
                "indexparameters.bm25.b", "0.75",
                "indexparameters.bm25.k1", "1.2"));
        put("-QF", params(
                "retrievalparameters.queryfiltering", "false"));
        put("-POSKEY", params(
                "retrievalparameters.keywords.positivepm@word:Gleason", "false",
                "retrievalparameters.keywords.positivepm@word:base", "false",
                "retrievalparameters.keywords.positivepm@word:clinical", "false",
                "retrievalparameters.keywords.positivepm@word:cure", "false",
                "retrievalparameters.keywords.positivepm@word:dna", "false",
                "retrievalparameters.keywords.positivepm@word:efficacy", "false",
                "retrievalparameters.keywords.positivepm@word:gefitinib", "false",
                "retrievalparameters.keywords.positivepm@word:gene", "false",
                "retrievalparameters.keywords.positivepm@word:genotype", "false",
                "retrievalparameters.keywords.positivepm@word:heal", "false",
                "retrievalparameters.keywords.positivepm@word:healing", "false",
                "retrievalparameters.keywords.positivepm@word:malignancy", "false",
                "retrievalparameters.keywords.positivepm@word:outcome", "false",
                "retrievalparameters.keywords.positivepm@word:patient", "false",
                "retrievalparameters.keywords.positivepm@word:personalized", "false",
                "retrievalparameters.keywords.positivepm@word:prevent", "false",
                "retrievalparameters.keywords.positivepm@word:prognoses", "false",
                "retrievalparameters.keywords.positivepm@word:prognosis", "false",
                "retrievalparameters.keywords.positivepm@word:prognostic", "false",
                "retrievalparameters.keywords.positivepm@word:prophylactic", "false",
                "retrievalparameters.keywords.positivepm@word:prophylaxis", "false",
                "retrievalparameters.keywords.positivepm@word:recover", "false",
                "retrievalparameters.keywords.positivepm@word:recovery", "false",
                "retrievalparameters.keywords.positivepm@word:recurrence", "false",
                "retrievalparameters.keywords.positivepm@word:resistance", "false",
                "retrievalparameters.keywords.positivepm@word:study", "false",
                "retrievalparameters.keywords.positivepm@word:surgery", "false",
                "retrievalparameters.keywords.positivepm@word:survival", "false",
                "retrievalparameters.keywords.positivepm@word:survive", "false",
                "retrievalparameters.keywords.positivepm@word:target", "false",
                "retrievalparameters.keywords.positivepm@word:targets", "false",
                "retrievalparameters.keywords.positivepm@word:therapeutic", "false",
                "retrievalparameters.keywords.positivepm@word:therapeutical", "false",
                "retrievalparameters.keywords.positivepm@word:therapy", "false",
                "retrievalparameters.keywords.positivepm@word:treatment", "false"
        ));
        put("-NEGKEY", params(
                "retrievalparameters.keywords.negativepm@word:case", "false",
                "retrievalparameters.keywords.negativepm@word:cell", "false",
                "retrievalparameters.keywords.negativepm@word:development", "false",
                "retrievalparameters.keywords.negativepm@word:dna", "false",
                "retrievalparameters.keywords.negativepm@word:model", "false",
                "retrievalparameters.keywords.negativepm@word:mouse", "false",
                "retrievalparameters.keywords.negativepm@word:pathogenesis", "false",
                "retrievalparameters.keywords.negativepm@word:specific", "false",
                "retrievalparameters.keywords.negativepm@word:staining", "false",
                "retrievalparameters.keywords.negativepm@word:tissue", "false",
                "retrievalparameters.keywords.negativepm@word:tumor", "false"
        ));
        put("-DISMAX", params(
                "retrievalparameters.template", "/templates/biomedical_articles_generic/jlpmcommon2generic_nodismax.json"));
        put("-FLDWT", params(
                "retrievalparameters.templateparameters.fieldboosts.abstract_field_disease_boost", "1.0",
                "retrievalparameters.templateparameters.fieldboosts.abstract_field_gene_boost", "1.0",
                "retrievalparameters.templateparameters.fieldboosts.abstract_field_kw_boost", "1.0",
                "retrievalparameters.templateparameters.fieldboosts.genes_field_boost", "1.0",
                "retrievalparameters.templateparameters.fieldboosts.meshTags_field_disease_boost", "1.0",
                "retrievalparameters.templateparameters.fieldboosts.meshTags_field_gene_boost", "1.0",
                "retrievalparameters.templateparameters.fieldboosts.meshTags_field_kw_boost", "1.0",
                "retrievalparameters.templateparameters.fieldboosts.title_field_disease_boost", "1.0",
                "retrievalparameters.templateparameters.fieldboosts.title_field_gene_boost", "1.0",
                "retrievalparameters.templateparameters.fieldboosts.title_field_kw_boost", "1.0"
        ));
        put("-CLSWT", params(
                "retrievalparameters.templateparameters.clauseboosts.conditional_cancer_boost", "1.0",
                "retrievalparameters.templateparameters.clauseboosts.conditional_chemo_boost", "1.0",
                "retrievalparameters.templateparameters.clauseboosts.exists_abstract_boost", "1.0",
                "retrievalparameters.templateparameters.clauseboosts.filtered_treatments_boost", "1.0",
                "retrievalparameters.templateparameters.clauseboosts.negative_kw_boost", "-1.0",
                "retrievalparameters.templateparameters.clauseboosts.positive_kw_boost", "1.0",
                "retrievalparameters.templateparameters.disease.boosts.disease_custom_boost", "1.0",
                "retrievalparameters.templateparameters.disease.boosts.disease_hypernyms_boost", "1.0",
                "retrievalparameters.templateparameters.disease.boosts.disease_prefterm_boost", "1.0",
                "retrievalparameters.templateparameters.disease.boosts.disease_query_boost", "1.0",
                "retrievalparameters.templateparameters.disease.boosts.disease_syn_boost", "1.0",
                "retrievalparameters.templateparameters.disease.boosts.disease_topic_clause_boost", "1.0",
                "retrievalparameters.templateparameters.gene.boosts.gene_custom_boost", "1.0",
                "retrievalparameters.templateparameters.gene.boosts.gene_desc_boost", "1.0",
                "retrievalparameters.templateparameters.gene.boosts.gene_hypernyms_boost", "1.0",
                "retrievalparameters.templateparameters.gene.boosts.gene_query_boost", "1.0",
                "retrievalparameters.templateparameters.gene.boosts.gene_syn_boost", "1.0",
                "retrievalparameters.templateparameters.gene.boosts.gene_topic_clause_boost", "1.0"
        ));
        put("-SLDTMR", params(
                "retrievalparameters.geneexpansion.custom", "false"));
        put("-NONMEL", params(
                "retrievalparameters.template", "/templates/biomedical_articles_generic/jlpmcommon2generic_no_non_melanoma.json"));
    }

    private Map<String, String> params(String... entries) {
        Map<String, String> params = new HashMap<>();
        for (int i = 0; i < entries.length; i++) {
            String entry = entries[i];
            if (i % 2 == 1)
                params.put(entries[i - 1], entries[i]);
        }
        return params;
    }
}
