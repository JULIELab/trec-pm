package at.medunigraz.imi.bst.trec;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.trec.experiment.Experiment;
import at.medunigraz.imi.bst.trec.experiment.registry.ClinicalTrialsRetrievalRegistry;
import at.medunigraz.imi.bst.trec.model.Topic;
import de.julielab.ir.OriginalDocumentRetrieval;
import de.julielab.ir.goldstandards.TrecPMGoldStandardFactory;
import de.julielab.ir.goldstandards.TrecQrelGoldStandard;
import de.julielab.ir.ltr.RankLibRanker;
import de.julielab.ir.ltr.RankerFromCt1718;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public final class ClinicalTrialsExperimenter {

    private static final TrecQrelGoldStandard<Topic> GOLD_STANDARD = TrecPMGoldStandardFactory.trialsInternal2019();

    public static void main(String[] args) {
        // Judging order: ?
//        final Experiment jlctphrase = new Experiment(GOLD_STANDARD,
//                ClinicalTrialsRetrievalRegistry.jlctphrase(TrecConfig.SIZE));

        final Experiment jlctletor = new Experiment(GOLD_STANDARD,
                ClinicalTrialsRetrievalRegistry.jlctletor(TrecConfig.SIZE));
        jlctletor.setReRanker(new RankerFromCt1718());

        Set<Experiment> experiments = new LinkedHashSet<>(Arrays.asList( jlctletor));
        for (Experiment exp : experiments) {
            exp.run();
        }

        OriginalDocumentRetrieval.getInstance().shutdown();

    }

}
