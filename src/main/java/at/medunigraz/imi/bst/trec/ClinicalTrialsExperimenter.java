package at.medunigraz.imi.bst.trec;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.trec.experiment.Experiment;
import at.medunigraz.imi.bst.trec.experiment.registry.ClinicalTrialsRetrievalRegistry;
import at.medunigraz.imi.bst.trec.model.Topic;
import de.julielab.ir.OriginalDocumentRetrieval;
import de.julielab.ir.TrecCacheConfiguration;
import de.julielab.ir.goldstandards.TrecPMGoldStandardFactory;
import de.julielab.ir.goldstandards.TrecQrelGoldStandard;
import de.julielab.ir.ltr.RankerFromCt1718;
import de.julielab.ir.ltr.RankerFromInternalCt19New;
import de.julielab.java.utilities.cache.CacheService;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public final class ClinicalTrialsExperimenter {

    private static final TrecQrelGoldStandard<Topic> GOLD_STANDARD = TrecPMGoldStandardFactory.trialsOfficial2019();

    public static void main(String[] args) throws IOException, ConfigurationException {
        CacheService.initialize(new TrecCacheConfiguration());

        // Judging order: ?
//        final Experiment jlctphrase = new Experiment(GOLD_STANDARD,
//               ClinicalTrialsRetrievalRegistry.jlctphrase(TrecConfig.SIZE));

        final Experiment jlctletor = new Experiment(GOLD_STANDARD,
                ClinicalTrialsRetrievalRegistry.jlctletor(TrecConfig.SIZE));
        jlctletor.setReRanker(new RankerFromCt1718());

        final Experiment jlctltrin = new Experiment(GOLD_STANDARD,
                ClinicalTrialsRetrievalRegistry.jlctltrin(TrecConfig.SIZE));
        jlctltrin.setReRanker(new RankerFromInternalCt19New());

//        final Experiment jlctprec = new Experiment(GOLD_STANDARD,
//                ClinicalTrialsRetrievalRegistry.jlctprec(TrecConfig.SIZE));

        final Experiment jlctgenes = new Experiment(GOLD_STANDARD,
                ClinicalTrialsRetrievalRegistry.jlctgenes(TrecConfig.SIZE));

        Set<Experiment> experiments = new LinkedHashSet<>(Arrays.asList(jlctletor, jlctltrin));
        for (Experiment exp : experiments) {
            exp.run();
        }

        OriginalDocumentRetrieval.getInstance().shutdown();
        CacheService.getInstance().commitAllCaches();

    }

}
