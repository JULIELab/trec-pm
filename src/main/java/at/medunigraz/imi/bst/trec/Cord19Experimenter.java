package at.medunigraz.imi.bst.trec;

import at.medunigraz.imi.bst.trec.experiment.Experiment;
import at.medunigraz.imi.bst.trec.experiment.registry.Cord19RetrievalRegistry;
import at.medunigraz.imi.bst.trec.model.TrecCovidTopicSetFactory;
import at.medunigraz.imi.bst.trec.search.ElasticClientFactory;
import de.julielab.ir.TrecCacheConfiguration;
import de.julielab.ir.goldstandards.TrecCovidGoldStandardFactory;
import de.julielab.ir.model.CovidTopic;
import de.julielab.ir.umls.UmlsSynsetProvider;
import de.julielab.java.utilities.cache.CacheService;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.IOException;

public final class Cord19Experimenter {

    public static void main(String[] args) throws IOException, ConfigurationException {
        CacheService.initialize(new TrecCacheConfiguration());
        UmlsSynsetProvider.setDefaultSynsetFile("resources/umlsCovidSynsets.txt.gz");

        Experiment<CovidTopic> exp = new Experiment<>(TrecCovidGoldStandardFactory.round1(), Cord19RetrievalRegistry.jlbaseRound1(), TrecCovidTopicSetFactory.topicsRound1());
        exp.setRequestedMetrics(new String[]{"NDCG", "P_5", "P_10", "Bpref", "MAP"});
//        exp.setK(5);
        exp.run();

        Experiment<CovidTopic> exp2 = new Experiment<>(TrecCovidGoldStandardFactory.round1(), Cord19RetrievalRegistry.jlprecRound1(), TrecCovidTopicSetFactory.topicsRound1());
        exp2.setRequestedMetrics(new String[]{"NDCG", "P_5", "P_10", "Bpref", "MAP"});
        exp2.run();

        Experiment<CovidTopic> exp3 = new Experiment<>(TrecCovidGoldStandardFactory.round1(), Cord19RetrievalRegistry.jlrecallRound1(), TrecCovidTopicSetFactory.topicsRound1());
        exp3.setRequestedMetrics(new String[]{"NDCG", "P_5", "P_10", "Bpref", "MAP"});
        exp3.run();

        CacheService.getInstance().commitAllCaches();
        ElasticClientFactory.getClient().close();
    }

}
