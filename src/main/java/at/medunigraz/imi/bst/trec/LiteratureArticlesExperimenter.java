package at.medunigraz.imi.bst.trec;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.trec.experiment.Experiment;
import at.medunigraz.imi.bst.trec.experiment.registry.LiteratureArticlesRetrievalRegistry;
import at.medunigraz.imi.bst.trec.model.Topic;
import de.julielab.ir.goldstandards.TrecPMGoldStandardFactory;
import de.julielab.ir.goldstandards.TrecQrelGoldStandard;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class LiteratureArticlesExperimenter {

    private static final TrecQrelGoldStandard<Topic> GOLD_STANDARD = TrecPMGoldStandardFactory.pubmedInternal2019();

    public static void main(String[] args) {
        // Judging order: 3
        Experiment juliepmcommon = new Experiment(GOLD_STANDARD,
                LiteratureArticlesRetrievalRegistry.juliepmcommon(TrecConfig.SIZE));

        Set<Experiment> experiments = new LinkedHashSet<>(Arrays.asList(juliepmcommon));
        for (Experiment exp : experiments) {
            exp.run();
        }
    }
}
