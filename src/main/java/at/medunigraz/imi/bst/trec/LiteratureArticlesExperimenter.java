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

    private static final int YEAR = 2019;

    private static final TrecQrelGoldStandard<Topic> GOLD_STANDARD = TrecPMGoldStandardFactory.pubmedInternal2019();

    public static void main(String[] args) {
        // Judging order: 1
        Experiment hpipubclass = new Experiment(GOLD_STANDARD,
                LiteratureArticlesRetrievalRegistry.hpipubclass(YEAR, TrecConfig.SIZE));

        // Judging order: 2
        Experiment hpipubnone = new Experiment(GOLD_STANDARD,
                LiteratureArticlesRetrievalRegistry.hpipubnone(YEAR, TrecConfig.SIZE));

        // Judging order: 3
        Experiment hpipubboost = new Experiment(GOLD_STANDARD,
                LiteratureArticlesRetrievalRegistry.hpipubboost(YEAR, TrecConfig.SIZE));

        // Judging order: 4
        Experiment hpipubcommon = new Experiment(GOLD_STANDARD,
                LiteratureArticlesRetrievalRegistry.hpipubcommon(YEAR, TrecConfig.SIZE));

        // Judging order: 5
        Experiment hpipubbase = new Experiment(GOLD_STANDARD,
                LiteratureArticlesRetrievalRegistry.hpipubbase(YEAR, TrecConfig.SIZE));

        Set<Experiment> experiments = new LinkedHashSet<>(Arrays.asList(hpipubclass, hpipubnone, hpipubboost, hpipubcommon, hpipubbase));
        for (Experiment exp : experiments) {
            exp.run();
        }
    }
}
