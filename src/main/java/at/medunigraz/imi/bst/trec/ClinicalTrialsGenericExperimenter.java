package at.medunigraz.imi.bst.trec;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.trec.experiment.Experiment;
import at.medunigraz.imi.bst.trec.experiment.registry.ClinicalTrialsRetrievalRegistry;
import at.medunigraz.imi.bst.trec.experiment.registry.LiteratureArticlesRetrievalRegistry;
import at.medunigraz.imi.bst.trec.model.Topic;
import at.medunigraz.imi.bst.trec.search.ElasticClientFactory;
import de.julielab.ir.OriginalDocumentRetrieval;
import de.julielab.ir.TrecCacheConfiguration;
import de.julielab.ir.goldstandards.TrecPMGoldStandardFactory;
import de.julielab.ir.goldstandards.TrecQrelGoldStandard;
import de.julielab.ir.ltr.features.FeatureControlCenter;
import de.julielab.ir.ltr.features.features.FastTextEmbeddingFeatures;
import de.julielab.java.utilities.ConfigurationUtilities;
import de.julielab.java.utilities.cache.CacheService;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public final class ClinicalTrialsGenericExperimenter {

    private static final TrecQrelGoldStandard<Topic> GOLD_STANDARD = TrecPMGoldStandardFactory.trialsOfficial2019();

    public static void main(String[] args) throws Exception {
        CacheService.initialize(new TrecCacheConfiguration());
        FeatureControlCenter.initialize(ConfigurationUtilities.loadXmlConfiguration(new File("config.xml")));
        Experiment jlpmgeneric = new Experiment(GOLD_STANDARD, ClinicalTrialsRetrievalRegistry.jlctgeneric(TrecConfig.SIZE, GOLD_STANDARD.getDatasetId(), ""));

        Set<Experiment> experiments = new LinkedHashSet<>(Arrays.asList(jlpmgeneric));
        for (Experiment exp : experiments) {
            exp.run();
        }
        CacheService.getInstance().commitAllCaches();
        ElasticClientFactory.getClient().close();
        OriginalDocumentRetrieval.getInstance().shutdown();
        FastTextEmbeddingFeatures.shutdown();
    }

}
