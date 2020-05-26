package de.julielab.ir.experiments.ablation.sigir20;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class represents the model reduced to those parameter settings that where found to significantly worsen the
 * reference configuration when being removed for biomedical abstracts.
 */
public class Sigir20ReducedBaModel extends LinkedHashMap<String, Map<String, String>> {
    public Sigir20ReducedBaModel() {
        Map<String, String> nonSignificatParameterResets = new HashMap<>();
        put("reduced", nonSignificatParameterResets);

        // neutralize/deactivate the non-significant features
        nonSignificatParameterResets.put("retrievalparameters.template", "/templates/biomedical_articles_generic/jlpmcommon2generic_no_non_melanoma.json");
        nonSignificatParameterResets.put("retrievalparameters.synonymlist","false");
        nonSignificatParameterResets.put("retrievalparameters.diseaseexpansion.hypernyms", "false");
        nonSignificatParameterResets.put("retrievalparameters.diseaseexpansion.custom", "false");
        nonSignificatParameterResets.put("retrievalparameters.geneexpansion.description",   "false");
        nonSignificatParameterResets.put("retrievalparameters.geneexpansion.hypernyms",   "false");
        nonSignificatParameterResets.put("indexparameters.bm25.k1","1.2");
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
