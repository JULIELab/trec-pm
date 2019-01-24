package at.medunigraz.imi.bst.trec;

import at.medunigraz.imi.bst.trec.experiment.Experiment;

import java.util.HashMap;
import java.util.Map;

/**
 * Actually, this class is not different from {@link SigirPubmedExperimenterDefaultBoosting}, it just uses the predefined
 * parameter sets from {@link SigirParameters}.
 */
public class SigirPubmedExperimenterParameterized extends SuperSigirPubmedExperimenter {
    public static void main(String[] args) {

        final Experiment.GoldStandard goldStandard = Experiment.GoldStandard.OFFICIAL;
        final Experiment.Task target = Experiment.Task.PUBMED;
        final int year = 2017;


        // Change here the predefined parameter set you want to use
        Map<String, String> templateProperties = SigirParameters.TREC_2018_HPIPUBNONE;
        runExperiments(templateProperties, goldStandard, target, year, "", "");
    }
}
