package at.medunigraz.imi.bst.trec.search;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.trec.model.Result;
import at.medunigraz.imi.bst.trec.utils.JsonUtils;
import de.julielab.ir.es.ElasticSearchSetup;
import de.julielab.ir.es.NoParameters;
import de.julielab.ir.es.SimilarityParameters;
import de.julielab.java.utilities.cache.CacheAccess;
import de.julielab.java.utilities.cache.CacheService;
import org.apache.commons.codec.digest.DigestUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

public class ElasticSearch implements SearchEngine {

    private static final Logger LOG = LoggerFactory.getLogger(ElasticSearch.class);
    private static Map<Thread, CacheAccess<String, List<Result>>> cacheMap = new ConcurrentHashMap<>();
    private CacheAccess<String, List<Result>> cache;
    private Client client;
    private String index = "_all";
    private SimilarityParameters parameters;
    private String[] types = new String[0];
    // This was introduced for LtR. There, it is required to obtain IR scores for the exact documents of
    // the gold dataset. The filter query is meant to restrict the elastic search result set to the given
    // document IDs.
    private BoolQueryBuilder filterQuery;
    private String[] storedFields;

    public ElasticSearch() {
        cache = cacheMap.compute(Thread.currentThread(), (k,v) ->
            v != null ? v : CacheService.getInstance().getCacheAccess("elasticsearch.db", "ElasticSearchResultCache", "string", "java")
        );
        // This disables the caching. I only do this for parameter optimization because there won't be many - if any - cache hits.
        // For experiments where the queries might often be the same, use the cache assignment above.
//        cache = new NoOpCacheAccess<>("dummy", "dummy");
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

    public void setStoredFields(String[] storedFields) {
        this.storedFields = storedFields;
    }

    /**
     * <p>Filters the results for the given collection of values on the given field.</p>
     * <p>This is realized by a boolean query wrapping the original query in a must clause and the requested
     * values into a filter clause. Calling this method multiple times will override the old filter.</p>
     *
     * @param field  The field to filter on.
     * @param values The values of which the field must have at least one to be eligible for returning.
     */
    public void setFilterOnFieldValues(String field, Collection<String> values) {
        filterQuery = QueryBuilders.boolQuery().filter(QueryBuilders.termsQuery(field, values));
    }

    public List<Result> query(JSONObject jsonQuery) {
        return query(jsonQuery, TrecConfig.SIZE);
    }

    public List<Result> query(JSONObject jsonQuery, int size) {
        final String json = jsonQuery.toString();
//        System.out.println(Thread.currentThread().getName() + ", " + index + ": " + json);
        QueryBuilder qb = QueryBuilders.wrapperQuery(json);
        // Mostly used for LtR: Restrict the result to a set of documents specified with
        // #setFilterOnFieldValues(String, Collection)
        if (filterQuery != null) {
            qb = filterQuery.must(qb);
        }
        LOG.trace(JsonUtils.prettify(jsonQuery));
        String idString = index + Arrays.toString(storedFields) + Arrays.toString(types) + size + parameters.printToString() + qb.toString().replaceAll("\n", "");
        idString = idString.replaceAll("\\s+", " ");
        final String cacheKey = DigestUtils.md5Hex(idString);
        LOG.trace("Query ID for cache: {}", cacheKey);
        List<Result> result = cache.get(cacheKey);
        if (result == null) {
            LOG.trace("Query is not cached, getting result from ES");
            if (!(parameters instanceof NoParameters)) {
                if (client == null)
                    client = ElasticClientFactory.getClient();
                ElasticSearchSetup.configureSimilarity(index, true, parameters, TrecConfig.ELASTIC_BA_MEDLINE_TYPE);
            }

            result = query(qb, size);
            cache.put(cacheKey, result);
        } else {
            LOG.debug("Got query result of size {} from cache", result.size());
        }
        return result;
    }

    private List<Result> query(QueryBuilder qb, int size) {
        if (client == null)
            client = ElasticClientFactory.getClient();
        try {
            final SearchSourceBuilder sb = new SearchSourceBuilder().query(qb).size(size).storedField("_id");
            if (storedFields != null)
                sb.fetchSource(storedFields, null);
            SearchResponse response = null;
            int retries = 0;
            ExecutionException lastException = null;
            while (retries < 3 && response == null) {
                try {
                    response = client.search(new SearchRequest(index).source(sb)).get();
                } catch (InterruptedException e) {
                    LOG.error("Search was interrupted", e);
                } catch (ExecutionException e) {
                    if (e.getMessage().contains("no such index")) {
                        LOG.error("No such index exception. Searched index was {}", index, e);
                        retries = Integer.MAX_VALUE;
                    } else {
                        lastException = e;
                        int waitingtime = 1000 * (retries + 1);
                        LOG.debug("ExecutionException happened when searching. This happens sometimes after the settings of the searched index were updated directly before. Trying again after waiting for {}ms. Number of tries: {}. Error message: {}", waitingtime, retries, e.getMessage());
                        Thread.sleep(waitingtime);
                    }
                }
                ++retries;
            }
            if (response == null) {
                LOG.error("Could not execute the query after 3 tries, giving up.", lastException);
            } else {
                //LOG.trace(JsonUtils.prettify(response.toString()));
                SearchHit[] results = response.getHits().getHits();

                List<Result> ret = new ArrayList<>();
                for (SearchHit hit : results) {
                    Result result = new Result(hit.getId(), hit.getScore());
                    result.setSourceFields(hit.getSourceAsMap());
                    ret.add(result);
                }
                return ret;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

}
