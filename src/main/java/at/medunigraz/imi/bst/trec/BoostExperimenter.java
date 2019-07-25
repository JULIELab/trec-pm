package at.medunigraz.imi.bst.trec;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.retrieval.Retrieval;
import at.medunigraz.imi.bst.trec.experiment.Experiment;
import at.medunigraz.imi.bst.trec.experiment.registry.LiteratureArticlesRetrievalRegistry;
import at.medunigraz.imi.bst.trec.model.Task;
import at.medunigraz.imi.bst.trec.model.Topic;
import at.medunigraz.imi.bst.trec.model.TopicSet;
import at.medunigraz.imi.bst.trec.model.TrecPMTopicSetFactory;
import de.julielab.ir.goldstandards.TrecPMGoldStandardFactory;
import de.julielab.ir.goldstandards.TrecQrelGoldStandard;

import java.util.LinkedHashSet;
import java.util.Set;

public class BoostExperimenter {
    private static final int YEAR = 2018;

    private static final TrecQrelGoldStandard<Topic> GOLD_STANDARD = TrecPMGoldStandardFactory.pubmedOfficial2018();

    private static final TopicSet TOPICS = TrecPMTopicSetFactory.topics2018();

    private static final Task TASK = Task.PUBMED;

    public static void main(String[] args) {


        Set<Experiment> experiments = new LinkedHashSet<>();

        for (float i = 1; i <= 5; i += 0.5) {
            Retrieval retrieval = LiteratureArticlesRetrievalRegistry.boost(YEAR, TrecConfig.SIZE, String.valueOf(i));
            Experiment exp = prototype();
            exp.setRetrieval(retrieval);
            experiments.add(exp);
        }

        for (Experiment exp : experiments) {
            // TODO collect metrics and find optimal boost
            exp.run();
        }
    }

    private static Experiment prototype() {
        final Experiment prototype = new Experiment();
        prototype.setGoldDataset(GOLD_STANDARD);
        prototype.setTopicSet(TOPICS);
        return prototype;
    }

}
