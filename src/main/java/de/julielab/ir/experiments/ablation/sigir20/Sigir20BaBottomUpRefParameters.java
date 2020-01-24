package de.julielab.ir.experiments.ablation.sigir20;

import java.util.HashMap;

public class Sigir20BaBottomUpRefParameters extends HashMap<String, String> {
    public Sigir20BaBottomUpRefParameters() {
        put("retrievalparameters.template", "/templates/biomedical_articles_generic/jlpmcommon2generic_no_non_melanoma.json");
        put("retrievalparameters.queryfiltering","false");
        put("retrievalparameters.synonymlist","false");
        put("retrievalparameters.diseaseexpansion.preferredterm", "false");
        put("retrievalparameters.diseaseexpansion.synonyms", "false");
        put("retrievalparameters.diseaseexpansion.hypernyms", "false");
        put("retrievalparameters.diseaseexpansion.custom", "false");
        put("retrievalparameters.templateparameters.disease.boosts.disease_query_boost","1");
        put("retrievalparameters.templateparameters.disease.boosts.disease_topic_clause_boost","1");
        put("retrievalparameters.templateparameters.disease.boosts.disease_prefterm_boost","1");
        put("retrievalparameters.templateparameters.disease.boosts.disease_syn_boost","1");
        put("retrievalparameters.templateparameters.disease.boosts.disease_hypernyms_boost","1");
        put("retrievalparameters.templateparameters.disease.boosts.disease_custom_boost","1");
        put("retrievalparameters.templateparameters.disease.matchtypes.disease_match_type","best_fields");
        put("retrievalparameters.templateparameters.disease.matchtypes.disease_syn_match_type","best_fields");
        put("retrievalparameters.templateparameters.disease.matchtypes.disease_hypernyms_match_type","best_fields");
        put("retrievalparameters.templateparameters.disease.phraseslops.disease_slop","0");
        put("retrievalparameters.geneexpansion.synonyms",   "false");
        put("retrievalparameters.geneexpansion.description",   "false");
        put("retrievalparameters.geneexpansion.custom",   "false");
        put("retrievalparameters.geneexpansion.hypernyms",   "false");
        put("retrievalparameters.templateparameters.gene.boosts.gene_query_boost","1");
        put("retrievalparameters.templateparameters.gene.boosts.gene_topic_clause_boost","1");
        put("retrievalparameters.templateparameters.gene.boosts.gene_syn_boost","1");
        put("retrievalparameters.templateparameters.gene.boosts.gene_hypernyms_boost","1");
        put("retrievalparameters.templateparameters.gene.boosts.gene_desc_boost","1");
        put("retrievalparameters.templateparameters.gene.boosts.gene_custom_boost","1");
        put("retrievalparameters.templateparameters.gene.matchtypes.gene_topic_match_type","best_fields");
        put("retrievalparameters.templateparameters.gene.matchtypes.gene_syn_match_type","best_fields");
        put("retrievalparameters.templateparameters.gene.matchtypes.gene_hypernyms_match_type","best_fields");
        put("retrievalparameters.templateparameters.gene.matchtypes.gene_desc_match_type","best_fields");
        put("retrievalparameters.templateparameters.gene.matchtypes.custom_gene_match_type","best_fields");
        put("retrievalparameters.templateparameters.gene.phraseslops.gene_topic_slop","0");
        put("retrievalparameters.templateparameters.gene.phraseslops.gene_syn_slop","0");
        put("retrievalparameters.templateparameters.gene.phraseslops.gene_desc_slop","0");
        put("indexparameters.bm25.k1","1.2");
        put("indexparameters.bm25.b","0.75");
        put("retrievalparameters.templateparameters.fieldboosts.title_field_disease_boost","1");
        put("retrievalparameters.templateparameters.fieldboosts.abstract_field_disease_boost","1");
        put("retrievalparameters.templateparameters.fieldboosts.meshTags_field_disease_boost","1");
        put("retrievalparameters.templateparameters.fieldboosts.title_field_gene_boost","1");
        put("retrievalparameters.templateparameters.fieldboosts.abstract_field_gene_boost","1");
        put("retrievalparameters.templateparameters.fieldboosts.meshTags_field_gene_boost","1");
        put("retrievalparameters.templateparameters.fieldboosts.title_field_kw_boost","1");
        put("retrievalparameters.templateparameters.fieldboosts.abstract_field_kw_boost","1");
        put("retrievalparameters.templateparameters.fieldboosts.meshTags_field_kw_boost","1");
        put("retrievalparameters.templateparameters.fieldboosts.genes_field_boost","0");
        put("retrievalparameters.templateparameters.clauseboosts.conditional_chemo_boost","0");
        put("retrievalparameters.templateparameters.clauseboosts.conditional_cancer_boost","0");
        put("retrievalparameters.templateparameters.clauseboosts.positive_kw_boost","0");
        put("retrievalparameters.templateparameters.clauseboosts.negative_kw_boost","0");
        put("retrievalparameters.templateparameters.clauseboosts.exists_abstract_boost","0");
        put("retrievalparameters.templateparameters.clauseboosts.filtered_treatments_boost","0");
        put("retrievalparameters.keywords.chemotherapy@word:*mab","false");
        put("retrievalparameters.keywords.chemotherapy@word:*nib","false");
        put("retrievalparameters.keywords.chemotherapy@word:*cin","false");
        put("retrievalparameters.keywords.chemotherapy@word:*one","false");
        put("retrievalparameters.keywords.chemotherapy@word:*ate","false");
        put("retrievalparameters.keywords.chemotherapy@word:*mus","false");
        put("retrievalparameters.keywords.chemotherapy@word:*lin","false");
        put("retrievalparameters.keywords.cancer@word:cancer","false");
        put("retrievalparameters.keywords.cancer@word:carcinoma","false");
        put("retrievalparameters.keywords.cancer@word:tumor","false");
        put("retrievalparameters.keywords.positivepm@word:surgery","false");
        put("retrievalparameters.keywords.positivepm@word:recurrence","false");
        put("retrievalparameters.keywords.positivepm@word:malignancy","false");
        put("retrievalparameters.keywords.positivepm@word:study","false");
        put("retrievalparameters.keywords.positivepm@word:gefitinib","false");
        put("retrievalparameters.keywords.positivepm@word:treatment","false");
        put("retrievalparameters.keywords.positivepm@word:survival","false");
        put("retrievalparameters.keywords.positivepm@word:survive","false");
        put("retrievalparameters.keywords.positivepm@word:prognostic","false");
        put("retrievalparameters.keywords.positivepm@word:prognosis","false");
        put("retrievalparameters.keywords.positivepm@word:prognoses","false");
        put("retrievalparameters.keywords.positivepm@word:clinical","false");
        put("retrievalparameters.keywords.positivepm@word:therapy","false");
        put("retrievalparameters.keywords.positivepm@word:therapeutic","false");
        put("retrievalparameters.keywords.positivepm@word:therapeutical","false");
        put("retrievalparameters.keywords.positivepm@word:outcome","false");
        put("retrievalparameters.keywords.positivepm@word:resistance","false");
        put("retrievalparameters.keywords.positivepm@word:Gleason","false");
        put("retrievalparameters.keywords.positivepm@word:target","false");
        put("retrievalparameters.keywords.positivepm@word:targets","false");
        put("retrievalparameters.keywords.positivepm@word:gene","false");
        put("retrievalparameters.keywords.positivepm@word:genotype","false");
        put("retrievalparameters.keywords.positivepm@word:base","false");
        put("retrievalparameters.keywords.positivepm@word:prevent","false");
        put("retrievalparameters.keywords.positivepm@word:prophylaxis","false");
        put("retrievalparameters.keywords.positivepm@word:prophylactic","false");
        put("retrievalparameters.keywords.positivepm@word:personalized","false");
        put("retrievalparameters.keywords.positivepm@word:efficacy","false");
        put("retrievalparameters.keywords.positivepm@word:cure","false");
        put("retrievalparameters.keywords.positivepm@word:heal","false");
        put("retrievalparameters.keywords.positivepm@word:healing","false");
        put("retrievalparameters.keywords.positivepm@word:recover","false");
        put("retrievalparameters.keywords.positivepm@word:recovery","false");
        put("retrievalparameters.keywords.positivepm@word:patient","false");
        put("retrievalparameters.keywords.positivepm@word:dna","false");
        put("retrievalparameters.keywords.negativepm@word:tumor","false");
        put("retrievalparameters.keywords.negativepm@word:cell","false");
        put("retrievalparameters.keywords.negativepm@word:mouse","false");
        put("retrievalparameters.keywords.negativepm@word:model","false");
        put("retrievalparameters.keywords.negativepm@word:tissue","false");
        put("retrievalparameters.keywords.negativepm@word:development","false");
        put("retrievalparameters.keywords.negativepm@word:specific","false");
        put("retrievalparameters.keywords.negativepm@word:staining","false");
        put("retrievalparameters.keywords.negativepm@word:pathogenesis","false");
        put("retrievalparameters.keywords.negativepm@word:case","false");
        put("retrievalparameters.keywords.negativepm@word:dna","false");

    }
}
