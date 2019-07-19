package at.medunigraz.imi.bst.trec.search;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.trec.model.Result;
import at.medunigraz.imi.bst.trec.utils.JsonUtils;
import de.julielab.ir.cache.CacheAccess;
import de.julielab.ir.cache.CacheService;
import de.julielab.ir.es.NoParameters;
import de.julielab.ir.es.SimilarityParameters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ElasticSearch implements SearchEngine {

    private static final Logger LOG = LogManager.getLogger();

    private RestHighLevelClient client = ElasticClientFactory.getClient();

    private String index = "_all";
    private SimilarityParameters parameters;
    private String[] types = new String[0];

    private CacheAccess<String, List<Result>> cache;

    public ElasticSearch() {
        cache = CacheService.getInstance().getCacheAccess("elasticsearch.db", "ElasticSearchResultCache", "string", "java");
        this.parameters = new NoParameters();
    }

    public ElasticSearch(String index, SimilarityParameters parameters) {
        this();
        this.index = index;
        this.parameters = parameters != null ? parameters : new NoParameters();
    }

    public ElasticSearch(String index, SimilarityParameters parameters, String... types) {
        this(index, parameters);
        this.types = types;
    }

    public List<Result> query(JSONObject jsonQuery) {
        return query(jsonQuery, TrecConfig.SIZE);
    }

    public List<Result> query(JSONObject jsonQuery, int size) {
            final String json = jsonQuery.toString();
            String cacheKey = index + Arrays.toString(types) + size + parameters.printToString() + json;
            LOG.debug("Query ID for cache: {}", cacheKey);
            List<Result> result = cache.get(cacheKey);
            if (result == null) {
                if (!(parameters instanceof NoParameters)) {
    //                ElasticSearchSetup.
                }
                QueryBuilder qb = QueryBuilders.wrapperQuery(json);
                LOG.trace(JsonUtils.prettify(jsonQuery));

                result = query(qb, size);
                cache.put(cacheKey, result);
            }
            return result;
    }

    private List<Result> query(QueryBuilder qb, int size) {

        try {
            SearchResponse response = client.search(new SearchRequest(index).source(new SearchSourceBuilder().query(qb).size(size).storedField("_id")), RequestOptions.DEFAULT);
            //LOG.trace(JsonUtils.prettify(response.toString()));

            SearchHit[] results = response.getHits().getHits();

            List<Result> ret = new ArrayList<>();
            for (SearchHit hit : results) {
                Result result = new Result(hit.getId(), hit.getScore());
                ret.add(result);
            }

            return ret;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
