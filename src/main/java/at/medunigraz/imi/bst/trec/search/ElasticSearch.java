package at.medunigraz.imi.bst.trec.search;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.trec.model.Result;
import at.medunigraz.imi.bst.trec.utils.JsonUtils;
import de.julielab.ir.cache.CacheService;
import de.julielab.ir.es.ElasticSearchSetup;
import de.julielab.ir.es.NoParameters;
import de.julielab.ir.es.SimilarityParameters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.json.JSONObject;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.springframework.cache.Cache;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ElasticSearch implements SearchEngine {

	private static final Logger LOG = LogManager.getLogger();

	private Client client = ElasticClientFactory.getClient();

	private String index = "_all";
    private SimilarityParameters parameters;
    private String[] types = new String[0];

	private static HTreeMap<String, List<Result>> resultListCache;
    private boolean cacheReadOnly = true;

    public ElasticSearch() {
        final CacheService cacheService = CacheService.getInstance();
        final File cacheDbFile = Path.of("cache", "elasticsearch.db").toFile();
        resultListCache = cacheService.getCache(cacheDbFile, "ElasticSearchResultListCache", Serializer.STRING, Serializer.JAVA);
        cacheReadOnly = cacheService.isDbReadOnly(cacheDbFile);
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
        final String json = jsonQuery.toString();
		String cacheKey = index + Arrays.toString(types) + parameters.printToString() + json;
		LOG.debug("Query ID for cache: {}", cacheKey);
        List<Result> result = resultListCache.get(cacheKey);
        if (result == null) {
            if (!(parameters instanceof NoParameters)) {
//                ElasticSearchSetup.
            }
            QueryBuilder qb = QueryBuilders.wrapperQuery(json);
            LOG.trace(JsonUtils.prettify(jsonQuery));

            result = query(qb);
            if (!cacheReadOnly) {
                resultListCache.put(cacheKey, result);

                filedb.commit();
            }
        }
        return result;
	}

	private List<Result> query(QueryBuilder qb) {
		SearchRequestBuilder searchRequestBuilder = client.prepareSearch(index).setTypes(types).setQuery(qb)
				.setSize(1000).addStoredField("_id");

		SearchResponse response = searchRequestBuilder.get();
		//LOG.trace(JsonUtils.prettify(response.toString()));

		SearchHit[] results = response.getHits().getHits();

		List<Result> ret = new ArrayList<>();
		for (SearchHit hit : results) {
			Result result = new Result(hit.getId(), hit.getScore());
			ret.add(result);
		}

		return ret;
	}

}
