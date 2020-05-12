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

public final class Cord19ExperimenterLM {

    public static void main(String[] args) throws IOException, ConfigurationException {
        CacheService.initialize(new TrecCacheConfiguration());
        //UmlsSynsetProvider.setDefaultSynsetFile("resources/umlsCovidSynsets.txt.gz");

        Experiment<CovidTopic> exp;
        exp = new Experiment<>(TrecCovidGoldStandardFactory.round1(), Cord19RetrievalRegistry.jlQERound1(), TrecCovidTopicSetFactory.topicsRound1());
        exp.setRequestedMetrics(new String[]{"ndcg_cut_10", "P_5", "P_10", "Bpref", "MAP", "set_recall"});
        exp.setWriteInspectionFile(true);
        exp.setInspectionResultColumnGenerator(r -> r.getSourceFields().get("cord19_uid") + "\t" + r.getSourceFields().get("abstract"));
        exp.run();


        CacheService.getInstance().commitAllCaches();
        ElasticClientFactory.getClient().close();
    }

}
