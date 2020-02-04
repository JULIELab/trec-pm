package de.julielab.ir.ltr;

import at.medunigraz.imi.bst.trec.model.Task;
import at.medunigraz.imi.bst.trec.search.ElasticClientFactory;
import ciir.umass.edu.learning.RANKER_TYPE;
import ciir.umass.edu.utilities.MyThreadPool;
import de.julielab.ir.OriginalDocumentRetrieval;
import de.julielab.ir.TrecCacheConfiguration;
import de.julielab.ir.goldstandards.TrecPMGoldStandardFactory;
import de.julielab.ir.ltr.features.features.FastTextEmbeddingFeatures;
import de.julielab.java.utilities.ConfigurationUtilities;
import de.julielab.java.utilities.cache.CacheService;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class RankerFromInternalPm19 extends PrecisionMedicineReranker {

    public RankerFromInternalPm19() throws ConfigurationException, IOException {
        super(Task.PUBMED,
                Arrays.asList(TrecPMGoldStandardFactory.pubmedInternal2019()),
                ConfigurationUtilities.loadXmlConfiguration(new File("config", "featureConfiguration.xml")),
                Arrays.asList(new File("config", "costosys-pm19.xml").getCanonicalFile(), new File("config", "costosys-pm1718.xml").getCanonicalFile()),
                "_data_xmi.documents",
                "pubmedId.keyword",
                new File("rankLibModels/pmInternal9-val20pct-" + RANKER_TYPE.LAMBDAMART + ".mod"));
    }

    /**
     * For training.
     *
     * @param args
     */
    public static void main(String args[]) throws ConfigurationException, IOException {
        CacheService.initialize(new TrecCacheConfiguration());
        final RankerFromInternalPm19 rankerFromPm1718 = new RankerFromInternalPm19();
        rankerFromPm1718.trainModel();
        CacheService.getInstance().commitAllCaches();
        ElasticClientFactory.getClient().close();
        OriginalDocumentRetrieval.getInstance().shutdown();
        FastTextEmbeddingFeatures.shutdown();
        MyThreadPool.getInstance().shutdown();
    }

}
