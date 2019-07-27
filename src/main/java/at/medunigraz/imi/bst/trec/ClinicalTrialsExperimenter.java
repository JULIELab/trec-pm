package at.medunigraz.imi.bst.trec;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.trec.experiment.Experiment;
import at.medunigraz.imi.bst.trec.experiment.registry.ClinicalTrialsRetrievalRegistry;
import at.medunigraz.imi.bst.trec.model.Topic;
import de.julielab.ir.goldstandards.TrecPMGoldStandardFactory;
import de.julielab.ir.goldstandards.TrecQrelGoldStandard;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public final class ClinicalTrialsExperimenter {

    private static final TrecQrelGoldStandard<Topic> GOLD_STANDARD = TrecPMGoldStandardFactory.trialsInternal2019();

    public static void main(String[] args) {
        // Judging order: 1
        final Experiment hpictall = new Experiment(GOLD_STANDARD,
                ClinicalTrialsRetrievalRegistry.hpictall(TrecConfig.SIZE));

        // Judging order: 2
        final Experiment hpictphrase = new Experiment(GOLD_STANDARD,
                ClinicalTrialsRetrievalRegistry.hpictphrase(TrecConfig.SIZE));

        // Judging order: 3
        final Experiment hpictboost = new Experiment(GOLD_STANDARD,
                ClinicalTrialsRetrievalRegistry.hpictboost(TrecConfig.SIZE));

        // Judging order: 4
        final Experiment hpictcommon = new Experiment(GOLD_STANDARD,
                ClinicalTrialsRetrievalRegistry.hpictcommon(TrecConfig.SIZE));

        // Judging order: 5
        final Experiment hpictbase = new Experiment(GOLD_STANDARD,
                ClinicalTrialsRetrievalRegistry.hpictbase(TrecConfig.SIZE));

        Set<Experiment> experiments = new LinkedHashSet<>(Arrays.asList(hpictall, hpictphrase, hpictboost, hpictcommon, hpictbase));
        for (Experiment exp : experiments) {
            exp.run();
        }
    }

}
