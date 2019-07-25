package at.medunigraz.imi.bst.trec;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.trec.experiment.Experiment;
import at.medunigraz.imi.bst.trec.experiment.registry.ClinicalTrialsRetrievalRegistry;
import at.medunigraz.imi.bst.trec.model.GoldStandardType;
import at.medunigraz.imi.bst.trec.model.Topic;
import at.medunigraz.imi.bst.trec.model.TopicSet;
import at.medunigraz.imi.bst.trec.model.TrecPMTopicSetFactory;
import de.julielab.ir.goldstandards.TrecPMGoldStandardFactory;
import de.julielab.ir.goldstandards.TrecQrelGoldStandard;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public final class ClinicalTrialsExperimenter {

    private static final int YEAR = 2019;
    /**
     * @todo Unify GoldStandard (#16)
     */
    private static final GoldStandardType GOLD_STANDARD_TYPE = GoldStandardType.INTERNAL;

    private static final TrecQrelGoldStandard<Topic> GOLD_STANDARD = TrecPMGoldStandardFactory.trialsInternal2019();

    private static final TopicSet TOPICS = TrecPMTopicSetFactory.topics2019();

    public static void main(String[] args) {
        // Judging order: 1
        final Experiment hpictall = prototype();
        hpictall.setRetrieval(ClinicalTrialsRetrievalRegistry.hpictall(YEAR, TrecConfig.SIZE));

        // Judging order: 2
        final Experiment hpictphrase = prototype();
        hpictphrase.setRetrieval(ClinicalTrialsRetrievalRegistry.hpictphrase(YEAR, TrecConfig.SIZE));

        // Judging order: 3
        final Experiment hpictboost = prototype();
        hpictphrase.setRetrieval(ClinicalTrialsRetrievalRegistry.hpictboost(YEAR, TrecConfig.SIZE));

        // Judging order: 4
        final Experiment hpictcommon = prototype();
        hpictphrase.setRetrieval(ClinicalTrialsRetrievalRegistry.hpictcommon(YEAR, TrecConfig.SIZE));

        // Judging order: 5
        final Experiment hpictbase = prototype();
        hpictphrase.setRetrieval(ClinicalTrialsRetrievalRegistry.hpictbase(YEAR, TrecConfig.SIZE));

        Set<Experiment> experiments = new LinkedHashSet<>(Arrays.asList(hpictall, hpictphrase, hpictboost, hpictcommon, hpictbase));
        for (Experiment exp : experiments) {
            exp.run();
        }
    }

    private static Experiment prototype() {
        final Experiment prototype = new Experiment();
        prototype.setGoldDataset(GOLD_STANDARD);
        prototype.setTopicSet(TOPICS);
        prototype.setGoldStandardType(GOLD_STANDARD_TYPE);
        return prototype;
    }

}
