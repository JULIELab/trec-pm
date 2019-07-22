package at.medunigraz.imi.bst.trec;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.trec.experiment.Experiment;
import at.medunigraz.imi.bst.trec.experiment.registry.ScientificArticlesRetrievalRegistry;
import at.medunigraz.imi.bst.trec.model.GoldStandard;
import at.medunigraz.imi.bst.trec.model.Topic;
import at.medunigraz.imi.bst.trec.model.TopicSet;
import at.medunigraz.imi.bst.trec.model.TrecPMTopicSetFactory;
import de.julielab.ir.goldstandards.TrecPMGoldStandardFactory;
import de.julielab.ir.goldstandards.TrecQrelGoldStandard;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class PubmedExperimenter {

    private static final int YEAR = 2019;
    /**
     * @todo Unify GoldStandard (#16)
     */
    private static final GoldStandard GOLD_STANDARD_TYPE = GoldStandard.INTERNAL;

    private static final TrecQrelGoldStandard<Topic> GOLD_STANDARD = TrecPMGoldStandardFactory.pubmedInternal2019();

    private static final TopicSet TOPICS = TrecPMTopicSetFactory.topics2019();

    public static void main(String[] args) {
        // Judging order: 1
        Experiment hpipubclass = prototype();
        hpipubclass.setRetrieval(ScientificArticlesRetrievalRegistry.hpipubclass(YEAR, TrecConfig.SIZE));

        // Judging order: 2
        Experiment hpipubnone = prototype();
        hpipubclass.setRetrieval(ScientificArticlesRetrievalRegistry.hpipubnone(YEAR, TrecConfig.SIZE));

        // Judging order: 3
        Experiment hpipubboost = prototype();
        hpipubclass.setRetrieval(ScientificArticlesRetrievalRegistry.hpipubboost(YEAR, TrecConfig.SIZE));

        // Judging order: 4
        Experiment hpipubcommon = prototype();
        hpipubclass.setRetrieval(ScientificArticlesRetrievalRegistry.hpipubcommon(YEAR, TrecConfig.SIZE));

        // Judging order: 5
        Experiment hpipubbase = prototype();
        hpipubclass.setRetrieval(ScientificArticlesRetrievalRegistry.hpipubbase(YEAR, TrecConfig.SIZE));

        Set<Experiment> experiments = new LinkedHashSet<>(Arrays.asList(hpipubclass, hpipubnone, hpipubboost, hpipubcommon, hpipubbase));
        for (Experiment exp : experiments) {
            exp.run();
        }
    }

    private static Experiment prototype() {
        final Experiment prototype = new Experiment();
        prototype.setGoldDataset(GOLD_STANDARD);
        prototype.setTopicSet(TOPICS);
        prototype.setGoldStandard(GOLD_STANDARD_TYPE);
        return prototype;
    }

}
