package at.medunigraz.imi.bst.trec;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.trec.experiment.Experiment;
import at.medunigraz.imi.bst.trec.experiment.registry.LiteratureArticlesRetrievalRegistry;
import at.medunigraz.imi.bst.trec.model.Topic;
import at.medunigraz.imi.bst.trec.search.ElasticClientFactory;
import de.julielab.ir.OriginalDocumentRetrieval;
import de.julielab.ir.TrecCacheConfiguration;
import de.julielab.ir.goldstandards.TrecPMGoldStandardFactory;
import de.julielab.ir.goldstandards.TrecQrelGoldStandard;
import de.julielab.ir.ltr.features.features.FastTextEmbeddingFeatures;
import de.julielab.java.utilities.cache.CacheService;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class LiteratureArticlesExperimenter {

    private static final TrecQrelGoldStandard<Topic> GOLD_STANDARD = TrecPMGoldStandardFactory.pubmedOfficial2019();

    public static void main(String[] args) throws IOException {
        CacheService.initialize(new TrecCacheConfiguration());

//        // Judging order: ?
//        final Experiment jlpmcommon = new Experiment(GOLD_STANDARD,
//                LiteratureArticlesRetrievalRegistry.jlpmcommon(TrecConfig.SIZE));
//
        final Experiment jlpmcommon2 = new Experiment(GOLD_STANDARD,
                LiteratureArticlesRetrievalRegistry.jlpmcommon2(TrecConfig.SIZE));
        final Experiment jlpmcommon22 = new Experiment(GOLD_STANDARD,
                LiteratureArticlesRetrievalRegistry.jlpmcommon2(TrecConfig.SIZE));

//                final Experiment jlpmcommon21pos = new Experiment(GOLD_STANDARD,
//                LiteratureArticlesRetrievalRegistry.jlpmcommon2OnepositiveClause(TrecConfig.SIZE));

//
//        final Experiment jlpmletor = new Experiment(GOLD_STANDARD,
//                LiteratureArticlesRetrievalRegistry.jlpmletor(TrecConfig.SIZE));
//        jlpmletor.setReRanker(new RankerFromPm1718());
//
//        final Experiment jlpmltrin = new Experiment(GOLD_STANDARD,
//                LiteratureArticlesRetrievalRegistry.jlpmltrin(TrecConfig.SIZE));
//        jlpmltrin.setReRanker(new RankerFromInternalPm19());
//
//        final Experiment jlpmtrcommon = new Experiment(GOLD_STANDARD,
//                LiteratureArticlesRetrievalRegistry.jlpmtrcommon(TrecConfig.SIZE));
//        jlpmtrcommon.setReRanker(new TreatmentRanker());
//
//        final Experiment jlpmtrboost = new Experiment(GOLD_STANDARD,
//                LiteratureArticlesRetrievalRegistry.jlpmtrboost(TrecConfig.SIZE));
//        jlpmtrboost.setReRanker(new TreatmentRanker());

        Set<Experiment> experiments = new LinkedHashSet<>(Arrays.asList( jlpmcommon2, jlpmcommon22 ));
        for (Experiment exp : experiments) {
            exp.run();
        }
        CacheService.getInstance().commitAllCaches();
        ElasticClientFactory.getClient().close();
        OriginalDocumentRetrieval.getInstance().shutdown();
        FastTextEmbeddingFeatures.shutdown();
    }
}
