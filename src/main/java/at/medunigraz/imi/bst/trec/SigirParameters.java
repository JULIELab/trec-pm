package at.medunigraz.imi.bst.trec;

import java.util.HashMap;
import java.util.Map;

public class SigirParameters {

    public final static String BEST_FIELDS = "best_fields";
    public final static String MOST_FIELDS = "most_fields";
    public final static String PHRASE = "phrase";
    
    public static final Map<String, String> TREC_2018_HPIPUBNONE = new HashMap<>();
    static {
        TREC_2018_HPIPUBNONE.put("dis_boost", "1.5");
        TREC_2018_HPIPUBNONE.put("dis_topic_boost", "1");
        TREC_2018_HPIPUBNONE.put("dis_prefterm_boost", "0.1");
        TREC_2018_HPIPUBNONE.put("dis_syn_boost", "0.1");
        TREC_2018_HPIPUBNONE.put("dis_hyper_boost", "0");
        TREC_2018_HPIPUBNONE.put("gene_boost", "1.5");
        TREC_2018_HPIPUBNONE.put("gene_topic_boost", "1");
        TREC_2018_HPIPUBNONE.put("gene_syn_boost", "0.7");
        TREC_2018_HPIPUBNONE.put("gene_desc_boost", "0.1");
        TREC_2018_HPIPUBNONE.put("gene_hyper_boost", "0");
        TREC_2018_HPIPUBNONE.put("title_boost", "^2");
        TREC_2018_HPIPUBNONE.put("abstract_boost", "");
        TREC_2018_HPIPUBNONE.put("keyword_boost", "");
        TREC_2018_HPIPUBNONE.put("meshTags_boost", "");
        TREC_2018_HPIPUBNONE.put("genes_field_boost", "");
        TREC_2018_HPIPUBNONE.put("pos_words_boost", "1");
        TREC_2018_HPIPUBNONE.put("neg_words_boost", "-1");
        TREC_2018_HPIPUBNONE.put("cancer_boost", "1");
        TREC_2018_HPIPUBNONE.put("chemo_boost", "1");
        TREC_2018_HPIPUBNONE.put("dna_boost", "1");
        TREC_2018_HPIPUBNONE.put("extra_boost", "1");
        TREC_2018_HPIPUBNONE.put("pm_boost", "2");
        TREC_2018_HPIPUBNONE.put("non_mel_boost", "1");
        TREC_2018_HPIPUBNONE.put("pm_gs_boost", "1");
        TREC_2018_HPIPUBNONE.put("dgi_boost", "0");
        TREC_2018_HPIPUBNONE.put("mut_boost", "1");

        TREC_2018_HPIPUBNONE.put("dis_multi_match_type", BEST_FIELDS);
        TREC_2018_HPIPUBNONE.put("dis_prefterm_multi_match_type", BEST_FIELDS);
        TREC_2018_HPIPUBNONE.put("dis_syn_multi_match_type", PHRASE);
        TREC_2018_HPIPUBNONE.put("dis_hyper_multi_match_type", BEST_FIELDS);
        TREC_2018_HPIPUBNONE.put("gene_multi_match_type", BEST_FIELDS);
        TREC_2018_HPIPUBNONE.put("gene_syn_multi_match_type", PHRASE);
        TREC_2018_HPIPUBNONE.put("gene_desc_multi_match_type", PHRASE);
        TREC_2018_HPIPUBNONE.put("gene_hyper_multi_match_type", BEST_FIELDS);
        TREC_2018_HPIPUBNONE.put("cancer_multi_match_type", BEST_FIELDS);
        TREC_2018_HPIPUBNONE.put("dna_multi_match_type", BEST_FIELDS);
        TREC_2018_HPIPUBNONE.put("neg_boost_multi_match_type", BEST_FIELDS);
        TREC_2018_HPIPUBNONE.put("pos_boost_multi_match_type", BEST_FIELDS);
        TREC_2018_HPIPUBNONE.put("dgi_multi_match_type", BEST_FIELDS);

        TREC_2018_HPIPUBNONE.put("dis_operator", "or");
        TREC_2018_HPIPUBNONE.put("dis_prefterm_operator", "or");
        TREC_2018_HPIPUBNONE.put("dis_syn_operator", "or");
        TREC_2018_HPIPUBNONE.put("dis_hyper_operator", "or");
        TREC_2018_HPIPUBNONE.put("gene_operator", "or");
        TREC_2018_HPIPUBNONE.put("gene_syn_operator", "or");
        TREC_2018_HPIPUBNONE.put("gene_hyper_operator", "or");
        TREC_2018_HPIPUBNONE.put("gene_desc_operator", "or");
        TREC_2018_HPIPUBNONE.put("cancer_operator", "or");
        TREC_2018_HPIPUBNONE.put("dna_operator", "or");

        TREC_2018_HPIPUBNONE.put("phrase_slop", "10");
    }

    public static final Map<String, String> LITERATURE_ES_DEFAULTS = new HashMap<>();
    static {
        LITERATURE_ES_DEFAULTS.put("dis_boost", "1");
        LITERATURE_ES_DEFAULTS.put("dis_topic_boost", "1");
        LITERATURE_ES_DEFAULTS.put("dis_prefterm_boost", "1");
        LITERATURE_ES_DEFAULTS.put("dis_syn_boost", "1");
        LITERATURE_ES_DEFAULTS.put("dis_hyper_boost", "1");
        LITERATURE_ES_DEFAULTS.put("gene_boost", "1");
        LITERATURE_ES_DEFAULTS.put("gene_topic_boost", "1");
        LITERATURE_ES_DEFAULTS.put("gene_syn_boost", "1");
        LITERATURE_ES_DEFAULTS.put("gene_desc_boost", "1");
        LITERATURE_ES_DEFAULTS.put("gene_hyper_boost", "1");
        LITERATURE_ES_DEFAULTS.put("title_boost", "");
        LITERATURE_ES_DEFAULTS.put("abstract_boost", "");
        LITERATURE_ES_DEFAULTS.put("keyword_boost", "");
        LITERATURE_ES_DEFAULTS.put("meshTags_boost", "");
        LITERATURE_ES_DEFAULTS.put("genes_field_boost", "");
        LITERATURE_ES_DEFAULTS.put("pos_words_boost", "1");
        LITERATURE_ES_DEFAULTS.put("neg_words_boost", "1");
        LITERATURE_ES_DEFAULTS.put("cancer_boost", "1");
        LITERATURE_ES_DEFAULTS.put("chemo_boost", "1");
        LITERATURE_ES_DEFAULTS.put("dna_boost", "1");
        LITERATURE_ES_DEFAULTS.put("extra_boost", "1");
        LITERATURE_ES_DEFAULTS.put("pm_boost", "1");
        LITERATURE_ES_DEFAULTS.put("non_mel_boost", "1");
        LITERATURE_ES_DEFAULTS.put("pm_gs_boost", "1");
        LITERATURE_ES_DEFAULTS.put("dgi_boost", "1");
        LITERATURE_ES_DEFAULTS.put("mut_boost", "1");

        LITERATURE_ES_DEFAULTS.put("dis_multi_match_type", BEST_FIELDS);
        LITERATURE_ES_DEFAULTS.put("dis_prefterm_multi_match_type", BEST_FIELDS);
        LITERATURE_ES_DEFAULTS.put("dis_syn_multi_match_type", PHRASE);
        LITERATURE_ES_DEFAULTS.put("dis_hyper_multi_match_type", BEST_FIELDS);
        LITERATURE_ES_DEFAULTS.put("gene_multi_match_type", BEST_FIELDS);
        LITERATURE_ES_DEFAULTS.put("gene_syn_multi_match_type", PHRASE);
        LITERATURE_ES_DEFAULTS.put("gene_desc_multi_match_type", PHRASE);
        LITERATURE_ES_DEFAULTS.put("gene_hyper_multi_match_type", BEST_FIELDS);
        LITERATURE_ES_DEFAULTS.put("cancer_multi_match_type", BEST_FIELDS);
        LITERATURE_ES_DEFAULTS.put("dna_multi_match_type", BEST_FIELDS);
        LITERATURE_ES_DEFAULTS.put("neg_boost_multi_match_type", BEST_FIELDS);
        LITERATURE_ES_DEFAULTS.put("pos_boost_multi_match_type", BEST_FIELDS);
        LITERATURE_ES_DEFAULTS.put("dgi_multi_match_type", BEST_FIELDS);

        LITERATURE_ES_DEFAULTS.put("dis_operator", "or");
        LITERATURE_ES_DEFAULTS.put("dis_prefterm_operator", "or");
        LITERATURE_ES_DEFAULTS.put("dis_syn_operator", "or");
        LITERATURE_ES_DEFAULTS.put("dis_hyper_operator", "or");
        LITERATURE_ES_DEFAULTS.put("gene_operator", "or");
        LITERATURE_ES_DEFAULTS.put("gene_syn_operator", "or");
        LITERATURE_ES_DEFAULTS.put("gene_hyper_operator", "or");
        LITERATURE_ES_DEFAULTS.put("gene_desc_operator", "or");
        LITERATURE_ES_DEFAULTS.put("cancer_operator", "or");
        LITERATURE_ES_DEFAULTS.put("dna_operator", "or");

        LITERATURE_ES_DEFAULTS.put("phrase_slop", "10");
    }

    private SigirParameters() {
    }
}
