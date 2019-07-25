package at.medunigraz.imi.bst.trec;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.retrieval.Retrieval;
import at.medunigraz.imi.bst.trec.experiment.Experiment;
import at.medunigraz.imi.bst.trec.experiment.registry.LiteratureArticlesRetrievalRegistry;
import at.medunigraz.imi.bst.trec.model.Topic;
import de.julielab.ir.goldstandards.TrecPMGoldStandardFactory;
import de.julielab.ir.goldstandards.TrecQrelGoldStandard;

import java.util.LinkedHashSet;
import java.util.Set;

public class BoostExperimenter {

    private static final TrecQrelGoldStandard<Topic> GOLD_STANDARD = TrecPMGoldStandardFactory.pubmedOfficial2018();

    public static void main(String[] args) {


        Set<Experiment> experiments = new LinkedHashSet<>();

        for (float i = 1; i <= 5; i += 0.5) {
            Retrieval retrieval = LiteratureArticlesRetrievalRegistry.boost(TrecConfig.SIZE, String.valueOf(i));
            experiments.add(new Experiment(GOLD_STANDARD, retrieval));
        }

        for (Experiment exp : experiments) {
            // TODO collect metrics and find optimal boost
            exp.run();
        }
    }
}
