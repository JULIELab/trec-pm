package at.medunigraz.imi.bst.trec;

import at.medunigraz.imi.bst.trec.experiment.Experiment;

import java.util.HashMap;
import java.util.Map;

import static at.medunigraz.imi.bst.trec.SigirParameters.BEST_FIELDS;

public class SigirPubmedTermBoostExperimenterDefaultBoosting extends SuperSigirPubmedRecallExperimenter {
    public static void main(String[] args) {

        if (args.length != 2) {
            System.err.println("Parameters: <multi match mode> <match default operator>");
        }

        final Experiment.GoldStandard goldStandard = Experiment.GoldStandard.OFFICIAL;
        final Experiment.Task target = Experiment.Task.PUBMED;
        final int year = 2018;


        Map<String, String> templateProperties = new HashMap<>(SigirParameters.LITERATURE_ES_DEFAULTS);

        String defaultMultiMatch = args[0];
        templateProperties.put("dis_multi_match_type", defaultMultiMatch);
        templateProperties.put("dis_syn_multi_match_type", defaultMultiMatch);
        templateProperties.put("dis_hyper_multi_match_type", defaultMultiMatch);
        templateProperties.put("gene_multi_match_type", defaultMultiMatch.equals("phrase") ? BEST_FIELDS : defaultMultiMatch);
        templateProperties.put("gene_syn_multi_match_type", defaultMultiMatch);
        templateProperties.put("gene_desc_multi_match_type", defaultMultiMatch);
        templateProperties.put("gene_hyper_multi_match_type", defaultMultiMatch);
        templateProperties.put("cancer_multi_match_type", defaultMultiMatch);
        templateProperties.put("dna_multi_match_type", defaultMultiMatch);
        templateProperties.put("neg_boost_multi_match_type", defaultMultiMatch);
        templateProperties.put("pos_boost_multi_match_type", defaultMultiMatch);
        templateProperties.put("dis_prefterm_multi_match_type", defaultMultiMatch);
        templateProperties.put("dgi_multi_match_type", defaultMultiMatch);

        String defaultOperator = args[1];
        templateProperties.put("dis_operator", defaultOperator);
        templateProperties.put("dis_prefterm_operator", defaultOperator);
        templateProperties.put("dis_syn_operator", defaultOperator);
        templateProperties.put("dis_hyper_operator", defaultOperator);
        templateProperties.put("gene_operator", "OR");
        templateProperties.put("gene_syn_operator", defaultOperator);
        templateProperties.put("gene_hyper_operator", defaultOperator);
        templateProperties.put("gene_desc_operator", "OR");
        templateProperties.put("cancer_operator", "OR");
        templateProperties.put("dna_operator", "OR");

        templateProperties.put("phrase_slop", "10");


        runTermBoostExperiments(templateProperties, goldStandard, target, year, "termboost", "--mmm:" + defaultMultiMatch + "-op:" + defaultOperator);
    }
}
