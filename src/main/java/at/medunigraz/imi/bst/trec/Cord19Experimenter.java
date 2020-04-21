package at.medunigraz.imi.bst.trec;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.trec.experiment.Cord19Retrieval;
import at.medunigraz.imi.bst.trec.experiment.Experiment;
import at.medunigraz.imi.bst.trec.experiment.registry.ClinicalTrialsRetrievalRegistry;
import at.medunigraz.imi.bst.trec.experiment.registry.Cord19RetrievalRegistry;
import at.medunigraz.imi.bst.trec.model.Challenge;
import at.medunigraz.imi.bst.trec.model.ResultList;
import at.medunigraz.imi.bst.trec.model.Topic;
import at.medunigraz.imi.bst.trec.model.TrecCovidTopicSetFactory;
import de.julielab.ir.OriginalDocumentRetrieval;
import de.julielab.ir.TrecCacheConfiguration;
import de.julielab.ir.goldstandards.TrecPMGoldStandardFactory;
import de.julielab.ir.goldstandards.TrecQrelGoldStandard;
import de.julielab.ir.ltr.RankerFromCt1718;
import de.julielab.ir.ltr.RankerFromInternalCt19New;
import de.julielab.ir.model.CovidTopic;
import de.julielab.ir.model.CovidTopicSet;
import de.julielab.java.utilities.cache.CacheService;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class Cord19Experimenter {


    public static void main(String[] args) throws IOException, ConfigurationException {
        CacheService.initialize(new TrecCacheConfiguration());

        Experiment<CovidTopic> exp = new Experiment<>(null, Cord19RetrievalRegistry.defaultRun(), TrecCovidTopicSetFactory.topicsRound1());
        exp.run();

        OriginalDocumentRetrieval.getInstance().shutdown();
        CacheService.getInstance().commitAllCaches();

    }

}
