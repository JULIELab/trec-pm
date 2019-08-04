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
        // Judging order: 2
        final Experiment jlctphrase = new Experiment(GOLD_STANDARD,
                ClinicalTrialsRetrievalRegistry.jlctphrase(TrecConfig.SIZE));

        Set<Experiment> experiments = new LinkedHashSet<>(Arrays.asList(jlctphrase));
        for (Experiment exp : experiments) {
            exp.run();
        }
    }

}
