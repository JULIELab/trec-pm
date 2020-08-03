package at.medunigraz.imi.bst.trec;

import at.medunigraz.imi.bst.retrieval.RRFResultListFusion;
import at.medunigraz.imi.bst.trec.experiment.Experiment;
import at.medunigraz.imi.bst.trec.experiment.registry.Cord19RetrievalRegistry;
import at.medunigraz.imi.bst.trec.model.TrecCovidTopicSetFactory;
import at.medunigraz.imi.bst.trec.search.ElasticClientFactory;
import de.julielab.ir.TrecCacheConfiguration;
import de.julielab.ir.model.CovidTopic;
import de.julielab.ir.umls.UmlsSynsetProvider;
import de.julielab.java.utilities.cache.CacheService;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;

public final class Cord19Experimenter {

    public static void main(String[] args) throws IOException, ConfigurationException {
        CacheService.initialize(new TrecCacheConfiguration());
        UmlsSynsetProvider.setDefaultSynsetFile("resources/umlsCovidSynsets.txt.gz");

        Experiment<CovidTopic> exp;

//        exp = new Experiment<>(TrecCovidGoldStandardFactory.round4Cumulative(), Cord19RetrievalRegistry.jlbasernd5(), TrecCovidTopicSetFactory.topicsRound4());
//        exp.setRequestedMetrics(new String[]{"ndcg_cut_10", "P_5", "P_10", "Bpref", "MAP", "set_recall"});
//        exp.run();


//
//        exp = new Experiment<>(TrecCovidGoldStandardFactory.round4Cumulative(), Cord19RetrievalRegistry.jlQERound5(), TrecCovidTopicSetFactory.topicsRound4());
//        exp.setRequestedMetrics(new String[]{"ndcg_cut_10", "P_5", "P_10", "Bpref", "MAP", "set_recall"});
//        exp.run();
//
        exp = new Experiment<>(null, new RRFResultListFusion(r -> (String) r.getSourceFields().get("cord19_uid")), TrecCovidTopicSetFactory.topicsRound5(), Cord19RetrievalRegistry.jlbasernd5(), Cord19RetrievalRegistry.jlQERound5());
        exp.setRequestedMetrics(new String[]{"ndcg_cut_10", "P_5", "P_10", "Bpref", "MAP", "set_recall"});
        exp.setWriteInspectionFile(false);
        exp.setInspectionOutputPerTopic(1000);
        exp.setInspectionResultColumnGenerator(r -> String.join("\t", (String)r.getSourceFields().get("cord19_uid"), norm((String)r.getSourceFields().get("text")), norm((String)r.getSourceFields().get("title")), norm((String)r.getSourceFields().get("abstract"))));
        exp.run();

        exp = new Experiment<>(null, Cord19RetrievalRegistry.jlbasernd5UdelTopics(), TrecCovidTopicSetFactory.topicsRound5Udel());
        exp.setRequestedMetrics(new String[]{"ndcg_cut_10", "P_5", "P_10", "Bpref", "MAP", "set_recall"});
        exp.setWriteInspectionFile(true);
        exp.setInspectionOutputPerTopic(1000);
        exp.setInspectionResultColumnGenerator(r -> String.join("\t", (String)r.getSourceFields().get("cord19_uid"), norm((String)r.getSourceFields().get("text")), norm((String)r.getSourceFields().get("title")), norm((String)r.getSourceFields().get("abstract"))));
        exp.run();

        CacheService.getInstance().commitAllCaches();
        ElasticClientFactory.getClient().close();
    }

    private static String norm(String str) {
        if (str == null)
            return "";
        return StringUtils.normalizeSpace(str).replaceAll("\t", " ").replace("\n", " ");
    }

}
