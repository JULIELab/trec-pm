package at.medunigraz.imi.bst.trec;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.trec.experiment.Experiment;
import at.medunigraz.imi.bst.trec.experiment.registry.LiteratureArticlesRetrievalRegistry;
import at.medunigraz.imi.bst.trec.model.Topic;
import at.medunigraz.imi.bst.trec.search.ElasticClientFactory;
import de.julielab.ir.OriginalDocumentRetrieval;
import de.julielab.ir.goldstandards.TrecPMGoldStandardFactory;
import de.julielab.ir.goldstandards.TrecQrelGoldStandard;
import de.julielab.ir.ltr.RankLibRanker;
import de.julielab.ir.ltr.RankerFromPm1718;
import de.julielab.ir.ltr.TreatmentRanker;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class LiteratureArticlesExperimenter {

    private static final TrecQrelGoldStandard<Topic> GOLD_STANDARD = TrecPMGoldStandardFactory.pubmedInternal2019();

    public static void main(String[] args) throws IOException {
        // Judging order: ?
//        final Experiment jlpmcommon = new Experiment(GOLD_STANDARD,
//                LiteratureArticlesRetrievalRegistry.jlpmcommon(TrecConfig.SIZE));

        final Experiment jlpmletor = new Experiment(GOLD_STANDARD,
                LiteratureArticlesRetrievalRegistry.jlpmletor(TrecConfig.SIZE));
        jlpmletor.setReRanker(new RankerFromPm1718());

//        final Experiment jlpmtrcommon = new Experiment(GOLD_STANDARD,
//                LiteratureArticlesRetrievalRegistry.jlpmtrcommon(TrecConfig.SIZE));
//        jlpmtrcommon.setReRanker(new TreatmentRanker());

        //Set<Experiment> experiments = new LinkedHashSet<>(Arrays.asList(jlpmcommon, jlpmletor, jlpmtrcommon));
        Set<Experiment> experiments = new LinkedHashSet<>(Arrays.asList( jlpmletor));
        for (Experiment exp : experiments) {
            exp.run();
        }
        ElasticClientFactory.getClient().close();
        OriginalDocumentRetrieval.getInstance().shutdown();
    }
}
