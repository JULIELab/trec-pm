package de.julielab.ir.experiments.ablation.sigir20;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class represents the model reduced to those parameter settings that where found to significantly worsen the
 * reference configuration when being removed for clinical trials.
 * Also activated here is query filtering and gene synonyms. While those two features did not worsen
 * the results statistically significant (p = 0.1130 and 0.1547, respectively), those are very important features
 * in our experience and should be included in an PM search engine.
 */
public class Sigir20ReducedCtModel extends LinkedHashMap<String, Map<String, String>> {
    public Sigir20ReducedCtModel() {
        Map<String, String> nonSignificatParameterResets = new HashMap<>();
        put("reduced", nonSignificatParameterResets);

        // neutralize/deactivate the non-significant features
        nonSignificatParameterResets.put("retrievalparameters.template", "/templates/clinical_trials_generic/jlctgeneric_no_non_melanoma.json");
        nonSignificatParameterResets.put("indexparameters.bm25.b","0.75");
        nonSignificatParameterResets.put("retrievalparameters.queryfiltering","true");
        nonSignificatParameterResets.put("retrievalparameters.diseaseexpansion.hypernyms", "false");
        nonSignificatParameterResets.put("retrievalparameters.geneexpansion.synonyms",   "true");
        nonSignificatParameterResets.put("retrievalparameters.geneexpansion.description",   "false");
        nonSignificatParameterResets.put("retrievalparameters.geneexpansion.custom",   "false");
        nonSignificatParameterResets.put("retrievalparameters.geneexpansion.hypernyms",   "false");
        nonSignificatParameterResets.put("retrievalparameters.templateparameters.fieldboosts.genes_field_boost", "0.0");
        nonSignificatParameterResets.put("retrievalparameters.templateparameters.fieldboosts.brief_title_field_disease_boost", "1.0");
        nonSignificatParameterResets.put("retrievalparameters.templateparameters.fieldboosts.brief_title_field_gene_boost", "1.0");
        nonSignificatParameterResets.put("retrievalparameters.templateparameters.fieldboosts.brief_title_field_kw_boost", "1.0");
        nonSignificatParameterResets.put("retrievalparameters.templateparameters.fieldboosts.conditions_field_disease_boost", "1.0");
        nonSignificatParameterResets.put("retrievalparameters.templateparameters.fieldboosts.conditions_field_gene_boost", "1.0");
        nonSignificatParameterResets.put("retrievalparameters.templateparameters.fieldboosts.conditions_field_kw_boost", "1.0");
        nonSignificatParameterResets.put("retrievalparameters.templateparameters.fieldboosts.description_field_disease_boost", "1.0");
        nonSignificatParameterResets.put("retrievalparameters.templateparameters.fieldboosts.description_field_gene_boost", "1.0");
        nonSignificatParameterResets.put("retrievalparameters.templateparameters.fieldboosts.description_field_kw_boost", "1.0");
        nonSignificatParameterResets.put("retrievalparameters.templateparameters.fieldboosts.inclusion_field_disease_boost", "1.0");
        nonSignificatParameterResets.put("retrievalparameters.templateparameters.fieldboosts.inclusion_field_gene_boost", "1.0");
        nonSignificatParameterResets.put("retrievalparameters.templateparameters.fieldboosts.inclusion_field_kw_boost", "1.0");
        nonSignificatParameterResets.put("retrievalparameters.templateparameters.fieldboosts.keywords_field_disease_boost", "1.0");
        nonSignificatParameterResets.put("retrievalparameters.templateparameters.fieldboosts.keywords_field_gene_boost", "1.0");
        nonSignificatParameterResets.put("retrievalparameters.templateparameters.fieldboosts.keywords_field_kw_boost", "1.0");
        nonSignificatParameterResets.put("retrievalparameters.templateparameters.fieldboosts.meshTags_field_disease_boost", "1.0");
        nonSignificatParameterResets.put("retrievalparameters.templateparameters.fieldboosts.meshTags_field_gene_boost", "1.0");
        nonSignificatParameterResets.put("retrievalparameters.templateparameters.fieldboosts.meshTags_field_kw_boost", "1.0");
        nonSignificatParameterResets.put("retrievalparameters.templateparameters.fieldboosts.official_title_field_disease_boost", "1.0");
        nonSignificatParameterResets.put("retrievalparameters.templateparameters.fieldboosts.official_title_field_gene_boost", "1.0");
        nonSignificatParameterResets.put("retrievalparameters.templateparameters.fieldboosts.official_title_field_kw_boost", "1.0");
        nonSignificatParameterResets.put("retrievalparameters.templateparameters.fieldboosts.outcomeDescriptions_field_disease_boost", "1.0");
        nonSignificatParameterResets.put("retrievalparameters.templateparameters.fieldboosts.outcomeDescriptions_field_gene_boost", "1.0");
        nonSignificatParameterResets.put("retrievalparameters.templateparameters.fieldboosts.outcomeDescriptions_field_kw_boost", "1.0");
        nonSignificatParameterResets.put("retrievalparameters.templateparameters.fieldboosts.outcomeMeasures_field_disease_boost", "1.0");
        nonSignificatParameterResets.put("retrievalparameters.templateparameters.fieldboosts.outcomeMeasures_field_gene_boost", "1.0");
        nonSignificatParameterResets.put("retrievalparameters.templateparameters.fieldboosts.outcomeMeasures_field_kw_boost", "1.0");
        nonSignificatParameterResets.put("retrievalparameters.templateparameters.fieldboosts.summary_field_disease_boost", "1.0");
        nonSignificatParameterResets.put("retrievalparameters.templateparameters.fieldboosts.summary_field_gene_boost", "1.0");
        nonSignificatParameterResets.put("retrievalparameters.templateparameters.fieldboosts.summary_field_kw_boost", "1.0");
        nonSignificatParameterResets.put("retrievalparameters.keywords.chemotherapy@word:*mab","false");
        nonSignificatParameterResets.put("retrievalparameters.keywords.chemotherapy@word:*nib","false");
        nonSignificatParameterResets.put("retrievalparameters.keywords.chemotherapy@word:*cin","false");
        nonSignificatParameterResets.put("retrievalparameters.keywords.chemotherapy@word:*one","false");
        nonSignificatParameterResets.put("retrievalparameters.keywords.chemotherapy@word:*ate","false");
        nonSignificatParameterResets.put("retrievalparameters.keywords.chemotherapy@word:*mus","false");
        nonSignificatParameterResets.put("retrievalparameters.keywords.chemotherapy@word:*lin","false");
        nonSignificatParameterResets.put("retrievalparameters.keywords.cancer@word:cancer","false");
        nonSignificatParameterResets.put("retrievalparameters.keywords.cancer@word:carcinoma","false");
        nonSignificatParameterResets.put("retrievalparameters.keywords.cancer@word:tumor","false");
        nonSignificatParameterResets.put("retrievalparameters.keywords.positivepm@word:surgery","false");
        nonSignificatParameterResets.put("retrievalparameters.keywords.positivepm@word:recurrence","false");
        nonSignificatParameterResets.put("retrievalparameters.keywords.positivepm@word:malignancy","false");
        nonSignificatParameterResets.put("retrievalparameters.keywords.positivepm@word:study","false");
        nonSignificatParameterResets.put("retrievalparameters.keywords.positivepm@word:gefitinib","false");
        nonSignificatParameterResets.put("retrievalparameters.keywords.positivepm@word:treatment","false");
        nonSignificatParameterResets.put("retrievalparameters.keywords.positivepm@word:survival","false");
        nonSignificatParameterResets.put("retrievalparameters.keywords.positivepm@word:survive","false");
        nonSignificatParameterResets.put("retrievalparameters.keywords.positivepm@word:prognostic","false");
        nonSignificatParameterResets.put("retrievalparameters.keywords.positivepm@word:prognosis","false");
        nonSignificatParameterResets.put("retrievalparameters.keywords.positivepm@word:prognoses","false");
        nonSignificatParameterResets.put("retrievalparameters.keywords.positivepm@word:clinical","false");
        nonSignificatParameterResets.put("retrievalparameters.keywords.positivepm@word:therapy","false");
        nonSignificatParameterResets.put("retrievalparameters.keywords.positivepm@word:therapeutic","false");
        nonSignificatParameterResets.put("retrievalparameters.keywords.positivepm@word:therapeutical","false");
        nonSignificatParameterResets.put("retrievalparameters.keywords.positivepm@word:outcome","false");
        nonSignificatParameterResets.put("retrievalparameters.keywords.positivepm@word:resistance","false");
        nonSignificatParameterResets.put("retrievalparameters.keywords.positivepm@word:Gleason","false");
        nonSignificatParameterResets.put("retrievalparameters.keywords.positivepm@word:target","false");
        nonSignificatParameterResets.put("retrievalparameters.keywords.positivepm@word:targets","false");
        nonSignificatParameterResets.put("retrievalparameters.keywords.positivepm@word:gene","false");
        nonSignificatParameterResets.put("retrievalparameters.keywords.positivepm@word:genotype","false");
        nonSignificatParameterResets.put("retrievalparameters.keywords.positivepm@word:base","false");
        nonSignificatParameterResets.put("retrievalparameters.keywords.positivepm@word:prevent","false");
        nonSignificatParameterResets.put("retrievalparameters.keywords.positivepm@word:prophylaxis","false");
        nonSignificatParameterResets.put("retrievalparameters.keywords.positivepm@word:prophylactic","false");
        nonSignificatParameterResets.put("retrievalparameters.keywords.positivepm@word:personalized","false");
        nonSignificatParameterResets.put("retrievalparameters.keywords.positivepm@word:efficacy","false");
        nonSignificatParameterResets.put("retrievalparameters.keywords.positivepm@word:cure","false");
        nonSignificatParameterResets.put("retrievalparameters.keywords.positivepm@word:heal","false");
        nonSignificatParameterResets.put("retrievalparameters.keywords.positivepm@word:healing","false");
        nonSignificatParameterResets.put("retrievalparameters.keywords.positivepm@word:recover","false");
        nonSignificatParameterResets.put("retrievalparameters.keywords.positivepm@word:recovery","false");
        nonSignificatParameterResets.put("retrievalparameters.keywords.positivepm@word:patient","false");
        nonSignificatParameterResets.put("retrievalparameters.keywords.positivepm@word:dna","false");
        nonSignificatParameterResets.put("retrievalparameters.keywords.negativepm@word:tumor","false");
        nonSignificatParameterResets.put("retrievalparameters.keywords.negativepm@word:cell","false");
        nonSignificatParameterResets.put("retrievalparameters.keywords.negativepm@word:mouse","false");
        nonSignificatParameterResets.put("retrievalparameters.keywords.negativepm@word:model","false");
        nonSignificatParameterResets.put("retrievalparameters.keywords.negativepm@word:tissue","false");
        nonSignificatParameterResets.put("retrievalparameters.keywords.negativepm@word:development","false");
        nonSignificatParameterResets.put("retrievalparameters.keywords.negativepm@word:specific","false");
        nonSignificatParameterResets.put("retrievalparameters.keywords.negativepm@word:staining","false");
        nonSignificatParameterResets.put("retrievalparameters.keywords.negativepm@word:pathogenesis","false");
        nonSignificatParameterResets.put("retrievalparameters.keywords.negativepm@word:case","false");
        nonSignificatParameterResets.put("retrievalparameters.keywords.negativepm@word:dna","false");
    }


}
