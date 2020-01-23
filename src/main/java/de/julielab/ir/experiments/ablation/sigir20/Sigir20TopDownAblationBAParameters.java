package de.julielab.ir.experiments.ablation.sigir20;

import de.julielab.ir.experiments.ablation.AblationLatexTableInfo;

import java.util.*;

import static de.julielab.ir.experiments.ablation.sigir20.AblationNames.*;

public class Sigir20TopDownAblationBAParameters extends LinkedHashMap<String, Map<String, String>> implements AblationLatexTableInfo {
    private static final Set<String> INDENT = Collections.emptySet();
    private static final Set<String> MIDRULE_AFTER = Collections.emptySet();

    public Sigir20TopDownAblationBAParameters() {
        put(String.format("-%s", DISEXP), params(
                "retrievalparameters.diseaseexpansion.custom", "false",
                "retrievalparameters.diseaseexpansion.hypernyms", "false",
                "retrievalparameters.diseaseexpansion.preferredterm", "false",
                "retrievalparameters.diseaseexpansion.synonyms", "false"
        ));
        put(String.format("-%s", PREF), params(
                "retrievalparameters.diseaseexpansion.custom", "true",
                "retrievalparameters.diseaseexpansion.hypernyms", "false",
                "retrievalparameters.diseaseexpansion.preferredterm", "false",
                "retrievalparameters.diseaseexpansion.synonyms", "true"
        ));
        put(String.format("-%s", DISSYN), params(
                "retrievalparameters.diseaseexpansion.custom", "true",
                "retrievalparameters.diseaseexpansion.hypernyms", "false",
                "retrievalparameters.diseaseexpansion.preferredterm", "true",
                "retrievalparameters.diseaseexpansion.synonyms", "false"
        ));
        put(String.format("-%s", SLDTMR), params(
                "retrievalparameters.diseaseexpansion.custom", "false",
                "retrievalparameters.diseaseexpansion.hypernyms", "false",
                "retrievalparameters.diseaseexpansion.preferredterm", "true",
                "retrievalparameters.diseaseexpansion.synonyms", "true"
        ));
        put(String.format("+%s", HYP), params(
                "retrievalparameters.diseaseexpansion.custom", "true",
                "retrievalparameters.diseaseexpansion.hypernyms", "true",
                "retrievalparameters.diseaseexpansion.preferredterm", "true",
                "retrievalparameters.diseaseexpansion.synonyms", "true"
        ));
        put(String.format("-%s", GENEXP), params(
                "retrievalparameters.geneexpansion.custom", "false",
                "retrievalparameters.geneexpansion.description", "false",
                "retrievalparameters.geneexpansion.hypernyms", "false",
                "retrievalparameters.geneexpansion.synonyms", "false"
        ));
        put(String.format("-%s", GENSYN), params(
                "retrievalparameters.geneexpansion.custom", "true",
                "retrievalparameters.geneexpansion.description", "true",
                "retrievalparameters.geneexpansion.hypernyms", "true",
                "retrievalparameters.geneexpansion.synonyms", "false"
        ));
        put(String.format("-%s", DESC), params(
                "retrievalparameters.geneexpansion.custom", "true",
                "retrievalparameters.geneexpansion.description", "false",
                "retrievalparameters.geneexpansion.hypernyms", "true",
                "retrievalparameters.geneexpansion.synonyms", "true"
        ));
        put(String.format("-%s", FAM), params(
                "retrievalparameters.geneexpansion.custom", "true",
                "retrievalparameters.geneexpansion.description", "true",
                "retrievalparameters.geneexpansion.hypernyms", "false",
                "retrievalparameters.geneexpansion.synonyms", "true"
        ));
        put(String.format("-%s", BM25), params(
                "indexparameters.bm25.b", "0.75",
                "indexparameters.bm25.k1", "1.2"));
        put(String.format("-%s", BM25+"_bdefault"), params(
                "indexparameters.bm25.b", "0.75"));
        put(String.format("-%s", BM25+"_k1default"), params(
                "indexparameters.bm25.k1", "1.2"));
        put(String.format("-%s", CLSWT), params(
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
        put(String.format("-%s", DISMAX), params(
                "retrievalparameters.template", "/templates/biomedical_articles_generic/jlpmcommon2generic_nodismax.json"));
        put(String.format("-%s", GENFLD), params(
                "retrievalparameters.templateparameters.fieldboosts.title_field_gene_boost", "0.0"
        ));
        put(String.format("-%s", HASAB), params("retrievalparameters.templateparameters.clauseboosts.exists_abstract_boost", "0.0"));
        put(String.format("-%s", HASTR), params("retrievalparameters.templateparameters.clauseboosts.filtered_treatments_boost", "0.0" ));
        put(String.format("-%s", FLDWT), params(
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
        put(String.format("-%s", MULTWRDS), params("retrievalparameters.templateparameters.disease.matchtypes.disease_match_type", "best_fields",
                "retrievalparameters.templateparameters.disease.matchtypes.disease_syn_match_type", "best_fields",
                "retrievalparameters.templateparameters.disease.matchtypes.disease_hypernyms_match_type", "best_fields",
                "retrievalparameters.templateparameters.gene.matchtypes.gene_topic_match_type", "best_fields",
                "retrievalparameters.templateparameters.gene.matchtypes.gene_syn_match_type", "best_fields",
                "retrievalparameters.templateparameters.gene.matchtypes.gene_hypernyms_match_type", "best_fields",
                "retrievalparameters.templateparameters.gene.matchtypes.gene_desc_match_type", "phrase",
                "retrievalparameters.templateparameters.gene.matchtypes.custom_gene_match_type", "best_fields"
                ));
        put(String.format("-%s", NEG), params(
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
        put(String.format("-%s", NONMEL), params(
                "retrievalparameters.template", "/templates/biomedical_articles_generic/jlpmcommon2generic_no_non_melanoma.json"));
        put(String.format("-%s", POS), params(
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
        put(String.format("-%s", STOP), params(
                "retrievalparameters.queryfiltering", "false"));
    }

    private Map<String, String> params(String... entries) {
        Map<String, String> params = new HashMap<>();
        for (int i = 0; i < entries.length; i++) {
            if (i % 2 == 1)
                params.put(entries[i - 1], entries[i]);
        }
        return params;
    }

    @Override
    public boolean indent(String ablationName) {
        return INDENT.contains(ablationName);
    }

    @Override
    public boolean addMidruleAfter(String ablationName) {
        return MIDRULE_AFTER.contains(ablationName);
    }
}
