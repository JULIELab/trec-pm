package de.julielab.ir.experiments.ablation.sigir20;

import java.util.HashMap;
import java.util.Map;

public class Sigir20BottomUpAblationCTParameters extends HashMap<String, Map<String, String>> {
    public Sigir20BottomUpAblationCTParameters(Map<String, String> optimizedParameters) {
        put("+DISEXP+QF", params(
                "retrievalparameters.diseaseexpansion.custom", "true",
                "retrievalparameters.diseaseexpansion.hypernyms", "true",
                "retrievalparameters.diseaseexpansion.preferredterm", "true",
                "retrievalparameters.diseaseexpansion.synonyms", "true",
                "retrievalparameters.templateparameters.disease.matchtypes.disease_match_type", optimizedParameters.get("retrievalparameters.templateparameters.disease.matchtypes.disease_match_type"),
                "retrievalparameters.templateparameters.disease.matchtypes.disease_syn_match_type", optimizedParameters.get("retrievalparameters.templateparameters.disease.matchtypes.disease_syn_match_type"),
                "retrievalparameters.templateparameters.disease.matchtypes.disease_hypernyms_match_type", optimizedParameters.get("retrievalparameters.templateparameters.disease.matchtypes.disease_hypernyms_match_type"),
                "retrievalparameters.templateparameters.disease.phraseslops.disease_slop", optimizedParameters.get("retrievalparameters.templateparameters.disease.phraseslops.disease_slop"),
                "retrievalparameters.queryfiltering", "true"
        ));
        put("+DISEXP+DF-DISSYN", params(
                "retrievalparameters.diseaseexpansion.custom", "true",
                "retrievalparameters.diseaseexpansion.hypernyms", "true",
                "retrievalparameters.diseaseexpansion.preferredterm", "true",
                "retrievalparameters.diseaseexpansion.synonyms", "false",
                "retrievalparameters.templateparameters.disease.matchtypes.disease_match_type", optimizedParameters.get("retrievalparameters.templateparameters.disease.matchtypes.disease_match_type"),
                "retrievalparameters.templateparameters.disease.matchtypes.disease_syn_match_type", optimizedParameters.get("retrievalparameters.templateparameters.disease.matchtypes.disease_syn_match_type"),
                "retrievalparameters.templateparameters.disease.matchtypes.disease_hypernyms_match_type", optimizedParameters.get("retrievalparameters.templateparameters.disease.matchtypes.disease_hypernyms_match_type"),
                "retrievalparameters.templateparameters.disease.phraseslops.disease_slop", optimizedParameters.get("retrievalparameters.templateparameters.disease.phraseslops.disease_slop"),
                "retrievalparameters.queryfiltering", "true"
        ));
        put("+DISEXP+QF-DISHYP", params(
                "retrievalparameters.diseaseexpansion.custom", "true",
                "retrievalparameters.diseaseexpansion.hypernyms", "false",
                "retrievalparameters.diseaseexpansion.preferredterm", "true",
                "retrievalparameters.diseaseexpansion.synonyms", "true",
                "retrievalparameters.templateparameters.disease.matchtypes.disease_match_type", optimizedParameters.get("retrievalparameters.templateparameters.disease.matchtypes.disease_match_type"),
                "retrievalparameters.templateparameters.disease.matchtypes.disease_syn_match_type", optimizedParameters.get("retrievalparameters.templateparameters.disease.matchtypes.disease_syn_match_type"),
                "retrievalparameters.templateparameters.disease.matchtypes.disease_hypernyms_match_type", optimizedParameters.get("retrievalparameters.templateparameters.disease.matchtypes.disease_hypernyms_match_type"),
                "retrievalparameters.templateparameters.disease.phraseslops.disease_slop", optimizedParameters.get("retrievalparameters.templateparameters.disease.phraseslops.disease_slop"),
                "retrievalparameters.queryfiltering", "true"
        ));
        put("+GENEXP+QF", params(
                "retrievalparameters.geneexpansion.custom", "true",
                "retrievalparameters.geneexpansion.description", "true",
                "retrievalparameters.geneexpansion.hypernyms", "true",
                "retrievalparameters.geneexpansion.synonyms", "true",
                "retrievalparameters.templateparameters.gene.matchtypes.gene_topic_match_type", optimizedParameters.get("retrievalparameters.templateparameters.gene.matchtypes.gene_topic_match_type"),
                "retrievalparameters.templateparameters.gene.matchtypes.gene_syn_match_type", optimizedParameters.get("retrievalparameters.templateparameters.gene.matchtypes.gene_syn_match_type"),
                "retrievalparameters.templateparameters.gene.matchtypes.gene_hypernyms_match_type", optimizedParameters.get("retrievalparameters.templateparameters.gene.matchtypes.gene_hypernyms_match_type"),
                "retrievalparameters.templateparameters.gene.matchtypes.gene_desc_match_type", optimizedParameters.get("retrievalparameters.templateparameters.gene.matchtypes.gene_desc_match_type"),
                "retrievalparameters.templateparameters.gene.matchtypes.custom_gene_match_type", optimizedParameters.get("retrievalparameters.templateparameters.gene.matchtypes.custom_gene_match_type"),
                "retrievalparameters.templateparameters.gene.phraseslops.gene_topic_slop", optimizedParameters.get("retrievalparameters.templateparameters.gene.phraseslops.gene_topic_slop"),
                "retrievalparameters.templateparameters.gene.phraseslops.gene_syn_slop", optimizedParameters.get("retrievalparameters.templateparameters.gene.phraseslops.gene_syn_slop"),
                "retrievalparameters.templateparameters.gene.phraseslops.gene_desc_slop", optimizedParameters.get("retrievalparameters.templateparameters.gene.phraseslops.gene_desc_slop"),
                "retrievalparameters.queryfiltering", "true"
        ));
        put("+GENEXP+QF-GENESYN", params(
                "retrievalparameters.geneexpansion.custom", "true",
                "retrievalparameters.geneexpansion.description", "true",
                "retrievalparameters.geneexpansion.hypernyms", "true",
                "retrievalparameters.geneexpansion.synonyms", "false",
                "retrievalparameters.templateparameters.gene.matchtypes.gene_topic_match_type", optimizedParameters.get("retrievalparameters.templateparameters.gene.matchtypes.gene_topic_match_type"),
                "retrievalparameters.templateparameters.gene.matchtypes.gene_syn_match_type", optimizedParameters.get("retrievalparameters.templateparameters.gene.matchtypes.gene_syn_match_type"),
                "retrievalparameters.templateparameters.gene.matchtypes.gene_hypernyms_match_type", optimizedParameters.get("retrievalparameters.templateparameters.gene.matchtypes.gene_hypernyms_match_type"),
                "retrievalparameters.templateparameters.gene.matchtypes.gene_desc_match_type", optimizedParameters.get("retrievalparameters.templateparameters.gene.matchtypes.gene_desc_match_type"),
                "retrievalparameters.templateparameters.gene.matchtypes.custom_gene_match_type", optimizedParameters.get("retrievalparameters.templateparameters.gene.matchtypes.custom_gene_match_type"),
                "retrievalparameters.templateparameters.gene.phraseslops.gene_topic_slop", optimizedParameters.get("retrievalparameters.templateparameters.gene.phraseslops.gene_topic_slop"),
                "retrievalparameters.templateparameters.gene.phraseslops.gene_syn_slop", optimizedParameters.get("retrievalparameters.templateparameters.gene.phraseslops.gene_syn_slop"),
                "retrievalparameters.templateparameters.gene.phraseslops.gene_desc_slop", optimizedParameters.get("retrievalparameters.templateparameters.gene.phraseslops.gene_desc_slop"),
                "retrievalparameters.queryfiltering", "true"
        ));
        put("+GENEXP+QF-GENEDESC", params(
                "retrievalparameters.geneexpansion.custom", "true",
                "retrievalparameters.geneexpansion.description", "false",
                "retrievalparameters.geneexpansion.hypernyms", "true",
                "retrievalparameters.geneexpansion.synonyms", "true",
                "retrievalparameters.templateparameters.gene.matchtypes.gene_topic_match_type", optimizedParameters.get("retrievalparameters.templateparameters.gene.matchtypes.gene_topic_match_type"),
                "retrievalparameters.templateparameters.gene.matchtypes.gene_syn_match_type", optimizedParameters.get("retrievalparameters.templateparameters.gene.matchtypes.gene_syn_match_type"),
                "retrievalparameters.templateparameters.gene.matchtypes.gene_hypernyms_match_type", optimizedParameters.get("retrievalparameters.templateparameters.gene.matchtypes.gene_hypernyms_match_type"),
                "retrievalparameters.templateparameters.gene.matchtypes.gene_desc_match_type", optimizedParameters.get("retrievalparameters.templateparameters.gene.matchtypes.gene_desc_match_type"),
                "retrievalparameters.templateparameters.gene.matchtypes.custom_gene_match_type", optimizedParameters.get("retrievalparameters.templateparameters.gene.matchtypes.custom_gene_match_type"),
                "retrievalparameters.templateparameters.gene.phraseslops.gene_topic_slop", optimizedParameters.get("retrievalparameters.templateparameters.gene.phraseslops.gene_topic_slop"),
                "retrievalparameters.templateparameters.gene.phraseslops.gene_syn_slop", optimizedParameters.get("retrievalparameters.templateparameters.gene.phraseslops.gene_syn_slop"),
                "retrievalparameters.templateparameters.gene.phraseslops.gene_desc_slop", optimizedParameters.get("retrievalparameters.templateparameters.gene.phraseslops.gene_desc_slop"),
                "retrievalparameters.queryfiltering", "true"
        ));
        put("+GENEXP+QF-FAMILIES", params(
                "retrievalparameters.geneexpansion.custom", "false",
                "retrievalparameters.geneexpansion.description", "true",
                "retrievalparameters.geneexpansion.hypernyms", "true",
                "retrievalparameters.geneexpansion.synonyms", "true",
                "retrievalparameters.templateparameters.gene.matchtypes.gene_topic_match_type", optimizedParameters.get("retrievalparameters.templateparameters.gene.matchtypes.gene_topic_match_type"),
                "retrievalparameters.templateparameters.gene.matchtypes.gene_syn_match_type", optimizedParameters.get("retrievalparameters.templateparameters.gene.matchtypes.gene_syn_match_type"),
                "retrievalparameters.templateparameters.gene.matchtypes.gene_hypernyms_match_type", optimizedParameters.get("retrievalparameters.templateparameters.gene.matchtypes.gene_hypernyms_match_type"),
                "retrievalparameters.templateparameters.gene.matchtypes.gene_desc_match_type", optimizedParameters.get("retrievalparameters.templateparameters.gene.matchtypes.gene_desc_match_type"),
                "retrievalparameters.templateparameters.gene.matchtypes.custom_gene_match_type", optimizedParameters.get("retrievalparameters.templateparameters.gene.matchtypes.custom_gene_match_type"),
                "retrievalparameters.templateparameters.gene.phraseslops.gene_topic_slop", optimizedParameters.get("retrievalparameters.templateparameters.gene.phraseslops.gene_topic_slop"),
                "retrievalparameters.templateparameters.gene.phraseslops.gene_syn_slop", optimizedParameters.get("retrievalparameters.templateparameters.gene.phraseslops.gene_syn_slop"),
                "retrievalparameters.templateparameters.gene.phraseslops.gene_desc_slop", optimizedParameters.get("retrievalparameters.templateparameters.gene.phraseslops.gene_desc_slop"),
                "retrievalparameters.queryfiltering", "true"
        ));
        put("+BM25", params(
                "indexparameters.bm25.b", optimizedParameters.get("indexparameters.bm25.b"),
                "indexparameters.bm25.k1", optimizedParameters.get("indexparameters.bm25.k1")));
        put("+DISEXP", params(
                "retrievalparameters.diseaseexpansion.custom", "true",
                "retrievalparameters.diseaseexpansion.hypernyms", "true",
                "retrievalparameters.diseaseexpansion.preferredterm", "true",
                "retrievalparameters.diseaseexpansion.synonyms", "true",
                "retrievalparameters.templateparameters.disease.matchtypes.disease_match_type", optimizedParameters.get("retrievalparameters.templateparameters.disease.matchtypes.disease_match_type"),
                "retrievalparameters.templateparameters.disease.matchtypes.disease_syn_match_type", optimizedParameters.get("retrievalparameters.templateparameters.disease.matchtypes.disease_syn_match_type"),
                "retrievalparameters.templateparameters.disease.matchtypes.disease_hypernyms_match_type", optimizedParameters.get("retrievalparameters.templateparameters.disease.matchtypes.disease_hypernyms_match_type"),
                "retrievalparameters.templateparameters.disease.phraseslops.disease_slop", optimizedParameters.get("retrievalparameters.templateparameters.disease.phraseslops.disease_slop")
        ));
        put("+GENEXP", params(
                "retrievalparameters.geneexpansion.custom", "true",
                "retrievalparameters.geneexpansion.description", "true",
                "retrievalparameters.geneexpansion.hypernyms", "true",
                "retrievalparameters.geneexpansion.synonyms", "true",
                "retrievalparameters.templateparameters.gene.matchtypes.gene_topic_match_type", optimizedParameters.get("retrievalparameters.templateparameters.gene.matchtypes.gene_topic_match_type"),
                "retrievalparameters.templateparameters.gene.matchtypes.gene_syn_match_type", optimizedParameters.get("retrievalparameters.templateparameters.gene.matchtypes.gene_syn_match_type"),
                "retrievalparameters.templateparameters.gene.matchtypes.gene_hypernyms_match_type", optimizedParameters.get("retrievalparameters.templateparameters.gene.matchtypes.gene_hypernyms_match_type"),
                "retrievalparameters.templateparameters.gene.matchtypes.gene_desc_match_type", optimizedParameters.get("retrievalparameters.templateparameters.gene.matchtypes.gene_desc_match_type"),
                "retrievalparameters.templateparameters.gene.matchtypes.custom_gene_match_type", optimizedParameters.get("retrievalparameters.templateparameters.gene.matchtypes.custom_gene_match_type"),
                "retrievalparameters.templateparameters.gene.phraseslops.gene_topic_slop", optimizedParameters.get("retrievalparameters.templateparameters.gene.phraseslops.gene_topic_slop"),
                "retrievalparameters.templateparameters.gene.phraseslops.gene_syn_slop", optimizedParameters.get("retrievalparameters.templateparameters.gene.phraseslops.gene_syn_slop"),
                "retrievalparameters.templateparameters.gene.phraseslops.gene_desc_slop", optimizedParameters.get("retrievalparameters.templateparameters.gene.phraseslops.gene_desc_slop")
        ));
        put("+QF", params(
                "retrievalparameters.queryfiltering", "true"));
        put("+POSKEY", params(
                "retrievalparameters.keywords.positivepm@word:Gleason", optimizedParameters.get("retrievalparameters.keywords.positivepm@word:Gleason"),
                "retrievalparameters.keywords.positivepm@word:base", optimizedParameters.get("retrievalparameters.keywords.positivepm@word:base"),
                "retrievalparameters.keywords.positivepm@word:clinical", optimizedParameters.get("retrievalparameters.keywords.positivepm@word:clinical"),
                "retrievalparameters.keywords.positivepm@word:cure", optimizedParameters.get("retrievalparameters.keywords.positivepm@word:cure"),
                "retrievalparameters.keywords.positivepm@word:dna", optimizedParameters.get("retrievalparameters.keywords.positivepm@word:dna"),
                "retrievalparameters.keywords.positivepm@word:efficacy", optimizedParameters.get("retrievalparameters.keywords.positivepm@word:efficacy"),
                "retrievalparameters.keywords.positivepm@word:gefitinib", optimizedParameters.get("retrievalparameters.keywords.positivepm@word:gefitinib"),
                "retrievalparameters.keywords.positivepm@word:gene", optimizedParameters.get("retrievalparameters.keywords.positivepm@word:gene"),
                "retrievalparameters.keywords.positivepm@word:genotype", optimizedParameters.get("retrievalparameters.keywords.positivepm@word:genotype"),
                "retrievalparameters.keywords.positivepm@word:heal", optimizedParameters.get("retrievalparameters.keywords.positivepm@word:heal"),
                "retrievalparameters.keywords.positivepm@word:healing", optimizedParameters.get("retrievalparameters.keywords.positivepm@word:healing"),
                "retrievalparameters.keywords.positivepm@word:malignancy", optimizedParameters.get("retrievalparameters.keywords.positivepm@word:malignancy"),
                "retrievalparameters.keywords.positivepm@word:outcome", optimizedParameters.get("retrievalparameters.keywords.positivepm@word:outcome"),
                "retrievalparameters.keywords.positivepm@word:patient", optimizedParameters.get("retrievalparameters.keywords.positivepm@word:patient"),
                "retrievalparameters.keywords.positivepm@word:personalized", optimizedParameters.get("retrievalparameters.keywords.positivepm@word:personalized"),
                "retrievalparameters.keywords.positivepm@word:prevent", optimizedParameters.get("retrievalparameters.keywords.positivepm@word:prevent"),
                "retrievalparameters.keywords.positivepm@word:prognoses", optimizedParameters.get("retrievalparameters.keywords.positivepm@word:prognoses"),
                "retrievalparameters.keywords.positivepm@word:prognosis", optimizedParameters.get("retrievalparameters.keywords.positivepm@word:prognosis"),
                "retrievalparameters.keywords.positivepm@word:prognostic", optimizedParameters.get("retrievalparameters.keywords.positivepm@word:prognostic"),
                "retrievalparameters.keywords.positivepm@word:prophylactic", optimizedParameters.get("retrievalparameters.keywords.positivepm@word:prophylactic"),
                "retrievalparameters.keywords.positivepm@word:prophylaxis", optimizedParameters.get("retrievalparameters.keywords.positivepm@word:prophylaxis"),
                "retrievalparameters.keywords.positivepm@word:recover", optimizedParameters.get("retrievalparameters.keywords.positivepm@word:recover"),
                "retrievalparameters.keywords.positivepm@word:recovery", optimizedParameters.get("retrievalparameters.keywords.positivepm@word:recovery"),
                "retrievalparameters.keywords.positivepm@word:recurrence", optimizedParameters.get("retrievalparameters.keywords.positivepm@word:recurrence"),
                "retrievalparameters.keywords.positivepm@word:resistance", optimizedParameters.get("retrievalparameters.keywords.positivepm@word:resistance"),
                "retrievalparameters.keywords.positivepm@word:study", optimizedParameters.get("retrievalparameters.keywords.positivepm@word:study"),
                "retrievalparameters.keywords.positivepm@word:surgery", optimizedParameters.get("retrievalparameters.keywords.positivepm@word:surgery"),
                "retrievalparameters.keywords.positivepm@word:survival", optimizedParameters.get("retrievalparameters.keywords.positivepm@word:survival"),
                "retrievalparameters.keywords.positivepm@word:survive", optimizedParameters.get("retrievalparameters.keywords.positivepm@word:survive"),
                "retrievalparameters.keywords.positivepm@word:target", optimizedParameters.get("retrievalparameters.keywords.positivepm@word:target"),
                "retrievalparameters.keywords.positivepm@word:targets", optimizedParameters.get("retrievalparameters.keywords.positivepm@word:targets"),
                "retrievalparameters.keywords.positivepm@word:therapeutic", optimizedParameters.get("retrievalparameters.keywords.positivepm@word:therapeutic"),
                "retrievalparameters.keywords.positivepm@word:therapeutical", optimizedParameters.get("retrievalparameters.keywords.positivepm@word:therapeutical"),
                "retrievalparameters.keywords.positivepm@word:therapy", optimizedParameters.get("retrievalparameters.keywords.positivepm@word:therapy"),
                "retrievalparameters.keywords.positivepm@word:treatment", optimizedParameters.get("retrievalparameters.keywords.positivepm@word:treatment")
        ));
        put("+NEGKEY", params(
                "retrievalparameters.keywords.negativepm@word:case", optimizedParameters.get("retrievalparameters.keywords.negativepm@word:case"),
                "retrievalparameters.keywords.negativepm@word:cell", optimizedParameters.get("retrievalparameters.keywords.negativepm@word:cell"),
                "retrievalparameters.keywords.negativepm@word:development", optimizedParameters.get("retrievalparameters.keywords.negativepm@word:development"),
                "retrievalparameters.keywords.negativepm@word:dna", optimizedParameters.get("retrievalparameters.keywords.negativepm@word:dna"),
                "retrievalparameters.keywords.negativepm@word:model", optimizedParameters.get("retrievalparameters.keywords.negativepm@word:model"),
                "retrievalparameters.keywords.negativepm@word:mouse", optimizedParameters.get("retrievalparameters.keywords.negativepm@word:mouse"),
                "retrievalparameters.keywords.negativepm@word:pathogenesis", optimizedParameters.get("retrievalparameters.keywords.negativepm@word:pathogenesis"),
                "retrievalparameters.keywords.negativepm@word:specific", optimizedParameters.get("retrievalparameters.keywords.negativepm@word:specific"),
                "retrievalparameters.keywords.negativepm@word:staining", optimizedParameters.get("retrievalparameters.keywords.negativepm@word:staining"),
                "retrievalparameters.keywords.negativepm@word:tissue", optimizedParameters.get("retrievalparameters.keywords.negativepm@word:tissue"),
                "retrievalparameters.keywords.negativepm@word:tumor", optimizedParameters.get("retrievalparameters.keywords.negativepm@word:tumor")
        ));
        put("+FLDWT", params(
                "retrievalparameters.templateparameters.fieldboosts.brief_title_field_disease_boost", optimizedParameters.get("retrievalparameters.templateparameters.fieldboosts.brief_title_field_disease_boost"),
                "retrievalparameters.templateparameters.fieldboosts.brief_title_field_gene_boost", optimizedParameters.get("retrievalparameters.templateparameters.fieldboosts.brief_title_field_gene_boost"),
                "retrievalparameters.templateparameters.fieldboosts.brief_title_field_kw_boost", optimizedParameters.get("retrievalparameters.templateparameters.fieldboosts.brief_title_field_kw_boost"),
                "retrievalparameters.templateparameters.fieldboosts.conditions_field_disease_boost", optimizedParameters.get("retrievalparameters.templateparameters.fieldboosts.conditions_field_disease_boost"),
                "retrievalparameters.templateparameters.fieldboosts.conditions_field_gene_boost", optimizedParameters.get("retrievalparameters.templateparameters.fieldboosts.conditions_field_gene_boost"),
                "retrievalparameters.templateparameters.fieldboosts.conditions_field_kw_boost", optimizedParameters.get("retrievalparameters.templateparameters.fieldboosts.conditions_field_kw_boost"),
                "retrievalparameters.templateparameters.fieldboosts.description_field_disease_boost", optimizedParameters.get("retrievalparameters.templateparameters.fieldboosts.description_field_disease_boost"),
                "retrievalparameters.templateparameters.fieldboosts.description_field_gene_boost", optimizedParameters.get("retrievalparameters.templateparameters.fieldboosts.description_field_gene_boost"),
                "retrievalparameters.templateparameters.fieldboosts.description_field_kw_boost", optimizedParameters.get("retrievalparameters.templateparameters.fieldboosts.description_field_kw_boost"),
                "retrievalparameters.templateparameters.fieldboosts.genes_field_boost", optimizedParameters.get("retrievalparameters.templateparameters.fieldboosts.genes_field_boost"),
                "retrievalparameters.templateparameters.fieldboosts.inclusion_field_disease_boost", optimizedParameters.get("retrievalparameters.templateparameters.fieldboosts.inclusion_field_disease_boost"),
                "retrievalparameters.templateparameters.fieldboosts.inclusion_field_gene_boost", optimizedParameters.get("retrievalparameters.templateparameters.fieldboosts.inclusion_field_gene_boost"),
                "retrievalparameters.templateparameters.fieldboosts.inclusion_field_kw_boost", optimizedParameters.get("retrievalparameters.templateparameters.fieldboosts.inclusion_field_kw_boost"),
                "retrievalparameters.templateparameters.fieldboosts.keywords_field_disease_boost", optimizedParameters.get("retrievalparameters.templateparameters.fieldboosts.keywords_field_disease_boost"),
                "retrievalparameters.templateparameters.fieldboosts.keywords_field_gene_boost", optimizedParameters.get("retrievalparameters.templateparameters.fieldboosts.keywords_field_gene_boost"),
                "retrievalparameters.templateparameters.fieldboosts.keywords_field_kw_boost", optimizedParameters.get("retrievalparameters.templateparameters.fieldboosts.keywords_field_kw_boost"),
                "retrievalparameters.templateparameters.fieldboosts.meshTags_field_disease_boost", optimizedParameters.get("retrievalparameters.templateparameters.fieldboosts.meshTags_field_disease_boost"),
                "retrievalparameters.templateparameters.fieldboosts.meshTags_field_gene_boost", optimizedParameters.get("retrievalparameters.templateparameters.fieldboosts.meshTags_field_gene_boost"),
                "retrievalparameters.templateparameters.fieldboosts.meshTags_field_kw_boost", optimizedParameters.get("retrievalparameters.templateparameters.fieldboosts.meshTags_field_kw_boost"),
                "retrievalparameters.templateparameters.fieldboosts.official_title_field_disease_boost", optimizedParameters.get("retrievalparameters.templateparameters.fieldboosts.official_title_field_disease_boost"),
                "retrievalparameters.templateparameters.fieldboosts.official_title_field_gene_boost", optimizedParameters.get("retrievalparameters.templateparameters.fieldboosts.official_title_field_gene_boost"),
                "retrievalparameters.templateparameters.fieldboosts.official_title_field_kw_boost", optimizedParameters.get("retrievalparameters.templateparameters.fieldboosts.official_title_field_kw_boost"),
                "retrievalparameters.templateparameters.fieldboosts.outcomeDescriptions_field_disease_boost", optimizedParameters.get("retrievalparameters.templateparameters.fieldboosts.outcomeDescriptions_field_disease_boost"),
                "retrievalparameters.templateparameters.fieldboosts.outcomeDescriptions_field_gene_boost", optimizedParameters.get("retrievalparameters.templateparameters.fieldboosts.outcomeDescriptions_field_gene_boost"),
                "retrievalparameters.templateparameters.fieldboosts.outcomeDescriptions_field_kw_boost", optimizedParameters.get("retrievalparameters.templateparameters.fieldboosts.outcomeDescriptions_field_kw_boost"),
                "retrievalparameters.templateparameters.fieldboosts.outcomeMeasures_field_disease_boost", optimizedParameters.get("retrievalparameters.templateparameters.fieldboosts.outcomeMeasures_field_disease_boost"),
                "retrievalparameters.templateparameters.fieldboosts.outcomeMeasures_field_gene_boost", optimizedParameters.get("retrievalparameters.templateparameters.fieldboosts.outcomeMeasures_field_gene_boost"),
                "retrievalparameters.templateparameters.fieldboosts.outcomeMeasures_field_kw_boost", optimizedParameters.get("retrievalparameters.templateparameters.fieldboosts.outcomeMeasures_field_kw_boost"),
                "retrievalparameters.templateparameters.fieldboosts.summary_field_disease_boost", optimizedParameters.get("retrievalparameters.templateparameters.fieldboosts.summary_field_disease_boost"),
                "retrievalparameters.templateparameters.fieldboosts.summary_field_gene_boost", optimizedParameters.get("retrievalparameters.templateparameters.fieldboosts.summary_field_gene_boost"),
                "retrievalparameters.templateparameters.fieldboosts.summary_field_kw_boost", optimizedParameters.get("retrievalparameters.templateparameters.fieldboosts.summary_field_kw_boost")
        ));
        put("+CLSWT", params(
                "retrievalparameters.templateparameters.clauseboosts.conditional_cancer_boost", optimizedParameters.get("retrievalparameters.templateparameters.clauseboosts.conditional_cancer_boost"),
                "retrievalparameters.templateparameters.clauseboosts.conditional_chemo_boost", optimizedParameters.get("retrievalparameters.templateparameters.clauseboosts.conditional_chemo_boost"),
                "retrievalparameters.templateparameters.clauseboosts.negative_kw_boost", optimizedParameters.get("retrievalparameters.templateparameters.clauseboosts.negative_kw_boost"),
                "retrievalparameters.templateparameters.clauseboosts.positive_kw_boost", optimizedParameters.get("retrievalparameters.templateparameters.clauseboosts.positive_kw_boost"),
                "retrievalparameters.templateparameters.clauseboosts.sex_boost", optimizedParameters.get("retrievalparameters.templateparameters.clauseboosts.sex_boost"),
                "retrievalparameters.templateparameters.clauseboosts.structured_boost", optimizedParameters.get("retrievalparameters.templateparameters.clauseboosts.structured_boost"),
                "retrievalparameters.templateparameters.disease.boosts.disease_custom_boost", optimizedParameters.get("retrievalparameters.templateparameters.disease.boosts.disease_custom_boost"),
                "retrievalparameters.templateparameters.disease.boosts.disease_hypernyms_boost", optimizedParameters.get("retrievalparameters.templateparameters.disease.boosts.disease_hypernyms_boost"),
                "retrievalparameters.templateparameters.disease.boosts.disease_prefterm_boost", optimizedParameters.get("retrievalparameters.templateparameters.disease.boosts.disease_prefterm_boost"),
                "retrievalparameters.templateparameters.disease.boosts.disease_query_boost", optimizedParameters.get("retrievalparameters.templateparameters.disease.boosts.disease_query_boost"),
                "retrievalparameters.templateparameters.disease.boosts.disease_syn_boost", optimizedParameters.get("retrievalparameters.templateparameters.disease.boosts.disease_syn_boost"),
                "retrievalparameters.templateparameters.disease.boosts.disease_topic_clause_boost", optimizedParameters.get("retrievalparameters.templateparameters.disease.boosts.disease_topic_clause_boost"),
                "retrievalparameters.templateparameters.gene.boosts.gene_custom_boost", optimizedParameters.get("retrievalparameters.templateparameters.gene.boosts.gene_custom_boost"),
                "retrievalparameters.templateparameters.gene.boosts.gene_desc_boost", optimizedParameters.get("retrievalparameters.templateparameters.gene.boosts.gene_desc_boost"),
                "retrievalparameters.templateparameters.gene.boosts.gene_hypernyms_boost", optimizedParameters.get("retrievalparameters.templateparameters.gene.boosts.gene_hypernyms_boost"),
                "retrievalparameters.templateparameters.gene.boosts.gene_query_boost", optimizedParameters.get("retrievalparameters.templateparameters.gene.boosts.gene_query_boost"),
                "retrievalparameters.templateparameters.gene.boosts.gene_syn_boost", optimizedParameters.get("retrievalparameters.templateparameters.gene.boosts.gene_syn_boost"),
                "retrievalparameters.templateparameters.gene.boosts.gene_topic_clause_boost", optimizedParameters.get("retrievalparameters.templateparameters.gene.boosts.gene_topic_clause_boost")
        ));
        put("+SLDTMR", params(
                "retrievalparameters.geneexpansion.custom", "true"));
        put("+NONMEL", params(
                "retrievalparameters.template", "/templates/clinical_trials_generic/jlctgeneric.json"));
    }

    private Map<String, String> params(String... entries) {
        Map<String, String> params = new HashMap<>();
        for (int i = 0; i < entries.length; i++) {
            if (i % 2 == 1)
                params.put(entries[i - 1], entries[i]);
        }
        return params;
    }
}
