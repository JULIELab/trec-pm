package at.medunigraz.imi.bst.trec.search;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.retrieval.SearchHitReranker;
import at.medunigraz.imi.bst.trec.model.Result;
import de.julielab.ir.es.ElasticSearchSetup;
import de.julielab.ir.es.NoParameters;
import de.julielab.ir.es.SimilarityParameters;
import de.julielab.java.utilities.cache.CacheAccess;
import de.julielab.java.utilities.cache.CacheService;
import org.apache.commons.codec.digest.DigestUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ElasticSearch implements SearchEngine {

    private static final Logger LOG = LoggerFactory.getLogger(ElasticSearch.class);
    private static Map<Thread, CacheAccess<String, List<Result>>> cacheMap = new ConcurrentHashMap<>();
    private CacheAccess<String, List<Result>> cache;
    private RestHighLevelClient client;
    private String[] indices = new String[]{"_all"};
    private SimilarityParameters parameters;
    // This was introduced for LtR. There, it is required to obtain IR scores for the exact documents of
    // the gold dataset. The filter query is meant to restrict the elastic search result set to the given
    // document IDs.
    private BoolQueryBuilder filterQuery;
    private String[] storedFields;
    /**
     * When not null, for each value in the given field only the first document in a result list will be returned.
     */
    private String unifyingField;
    private SearchHitReranker reranker;

    public ElasticSearch() {
        cache = cacheMap.compute(Thread.currentThread(), (k, v) ->
                v != null ? v : CacheService.getInstance().getCacheAccess("elasticsearch.db", "ElasticSearchResultCache", "string", "java", 0)
        );
        // This disables the caching. I only do this for parameter optimization because there won't be many - if any - cache hits.
        // For experiments where the queries might often be the same, use the cache assignment above.
//        cache = new NoOpCacheAccess<>("dummy", "dummy");
        this.parameters = new NoParameters();
    }

    public ElasticSearch(String[] indices, SimilarityParameters parameters) {
        this();
        this.indices = indices;
        this.parameters = parameters != null ? parameters : new NoParameters();
    }

    public void setUnifyingField(String unifyingField) {
        this.unifyingField = unifyingField;
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
        System.out.println(json);
        LOG.trace("Sending query: {}", Thread.currentThread().getName() + ", " + indices + ": " + json);
        QueryBuilder qb = QueryBuilders.wrapperQuery(json);
        // Mostly used for LtR: Restrict the result to a set of documents specified with
        // #setFilterOnFieldValues(String, Collection)
        if (filterQuery != null) {
            qb = filterQuery.must(qb);
        }
        String idString = Arrays.toString(indices) + Arrays.toString(storedFields) + size + parameters.printToString() + qb.toString().replaceAll("\n", "") + unifyingField;
        idString = idString.replaceAll("\\s+", " ");
        final String cacheKey = DigestUtils.md5Hex(idString);
        LOG.trace("Query ID for cache: {}", cacheKey);
        List<Result> result = cache.get(cacheKey);
        if (result == null) {

            try {
                LOG.trace("Query is not cached, getting result from ES");
                if (!(parameters instanceof NoParameters)) {
                    if (client == null)
                        client = ElasticClientFactory.getClient();
                    if (indices.length > 1)
                        throw new IllegalArgumentException("Index reconfiguration currently only works when a single index is given. Given indices: " + Arrays.asList(indices));
                    ElasticSearchSetup.configureSimilarity(indices[0], true, parameters, TrecConfig.ELASTIC_BA_MEDLINE_TYPE);
                }

                result = query(qb, size);
                cache.put(cacheKey, result);
            } catch (IOException e) {
                LOG.error("Error when reconfiguring index similarity for index {} to {}", indices, parameters, e);
            }
        } else {
            LOG.debug("Got query result of size {} from cache", result.size());
        }
        return result;
    }

    public void setReranker(SearchHitReranker reranker) {
        this.reranker = reranker;
    }

    private List<Result> query(QueryBuilder qb, int size) {
        if (client == null)
            client = ElasticClientFactory.getClient();
        try {
            final SearchSourceBuilder sb = new SearchSourceBuilder().query(qb).size(size).storedField("_id");
            if (storedFields != null)
                sb.fetchSource(storedFields, null);
//            System.out.println(sb);
            SearchResponse response = null;
            int retries = 0;
            ExecutionException lastException = null;
            while (retries < 3 && response == null) {
                try {
                    response = client.search(new SearchRequest(indices).source(sb), RequestOptions.DEFAULT);
                } catch (NoNodeAvailableException e) {
                    LOG.error("Could not connect to ElasticSearch cluster. Connecting will be retried every 30 seconds. Error message: {}", e.getMessage());
                    boolean connected = false;
                    int reconnections = 1;
                    while (!connected) {
                        Thread.sleep(30000);
                        try {
                            response = client.search(new SearchRequest(indices).source(sb), RequestOptions.DEFAULT);
                            connected = true;
                        } catch (NoNodeAvailableException e1) {
                            LOG.debug("Could still not connect to ElasticSearch. Tried {} times.", reconnections);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ++retries;
            }
            if (response == null) {
                LOG.error("Could not execute the query after 3 tries, giving up.", lastException);
            } else {
                //LOG.trace(JsonUtils.prettify(response.toString()));
                SearchHit[] results = response.getHits().getHits();
                List<Result> resultObjects;
                Stream<Result> resultStream = Arrays.stream(results).map(hit -> {
                    Result result = new Result(hit.getId(), hit.getIndex(), hit.getScore());
                    result.setSourceFields(hit.getSourceAsMap());
                    return result;
                });
                if (reranker != null) {
                    resultObjects = reranker.rerank(resultStream);
                } else {
                    resultObjects = resultStream.collect(Collectors.toList());
                }
                List<Result> ret = new ArrayList<>();
                Set<String> uniqueFieldValues = unifyingField != null ? new HashSet<>() : null;
                for (Result r : resultObjects) {
                    if (unifyingField != null && r.getSourceFields().get(unifyingField) != null && uniqueFieldValues.add((String) r.getSourceFields().get(unifyingField)))
                        ret.add(r);
                    else if (unifyingField == null)
                        ret.add(r);
                }
                LOG.debug("Got {} results", ret.size());
                List<String> text = ret.stream().map(r -> r.getSourceFields().get("cord19_uid") + " " + r.getSourceFields().get("text") ).map(String.class::cast).collect(Collectors.toList());
                return ret;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
