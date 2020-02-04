package de.julielab.ir.paramopt;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.trec.model.Challenge;
import at.medunigraz.imi.bst.trec.model.GoldStandardType;
import at.medunigraz.imi.bst.trec.model.Task;
import de.julielab.ir.TrecCacheConfiguration;
import de.julielab.ir.goldstandards.AggregatedTrecQrelGoldStandard;
import de.julielab.ir.goldstandards.TrecPMGoldStandardFactory;
import de.julielab.java.utilities.cache.CacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static spark.Spark.port;
import static spark.Spark.post;

public class HttpParamOptServer {
    public static final String GET_CONFIG_SCORE_PM = "get_configuration_score_pm";
    public static final String GET_CONFIG_SCORE_CT = "get_configuration_score_ct";
    public static final String INSTANCE = "instance";
    public static final String INSTANCE_INFO = "instance_info";
    public static final String CUTOFF_TIME = "cutoff_time";
    public static final String CUTOFF_LENGTH = "cutoff_length";
    public static final String SEED = "seed";
    public static final String METRICS = "metrics";
    public static final String INFNDCG = "infndcg";
    public static final String RPREC = "rprec";
    public static final String P10 = "p10";
    public static final String SET_RECALL = "set_recall";
    public static final String METRICS_PER_TOPIC = "metrics_per_topic";
    // The index_copy parameter value is used as a suffix to the otherwise normally obtained index name.
    // Used when multiple concurrent runs change index settings, then we need those copies.
    public static final String INDEX_SUFFIX = "index_suffix";
    private static final Logger log = LoggerFactory.getLogger(HttpParamOptServer.class);


    public HttpParamOptServer(int port) {
        port(port);
    }

    public static void main(String[] args) {
        int port = TrecConfig.EVALSERVER_PORT;

        HttpParamOptServer server = new HttpParamOptServer(port);
        server.startServer();
    }

    public void startServer() {
        CacheService.initialize(new TrecCacheConfiguration());

        post("/" + GET_CONFIG_SCORE_PM, new EvaluateConfigurationRoute( new AggregatedTrecQrelGoldStandard(TrecPMGoldStandardFactory.pubmedOfficial2017(), TrecPMGoldStandardFactory.pubmedOfficial2018(), TrecPMGoldStandardFactory.pubmedOfficial2019()), Challenge.TREC_PM, Task.PUBMED, GoldStandardType.OFFICIAL));
        post("/" + GET_CONFIG_SCORE_CT, new EvaluateConfigurationRoute( new AggregatedTrecQrelGoldStandard(TrecPMGoldStandardFactory.trialsOfficial2018(), TrecPMGoldStandardFactory.trialsOfficial2019()), Challenge.TREC_PM, Task.CLINICAL_TRIALS, GoldStandardType.OFFICIAL));

        log.info("Server is ready for requests.");
    }

}
