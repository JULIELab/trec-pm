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
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ElasticSearch implements SearchEngine {

    private static final Logger LOG = LogManager.getLogger();

    private RestHighLevelClient client = ElasticClientFactory.getClient();

    private String index = "_all";
    private SimilarityParameters parameters;
    private String[] types = new String[0];
    // This was introduced for LtR. There, it is required to obtain IR scores for the exact documents of
    // the gold dataset. The filter query is meant to restrict the elastic search result set to the given
    // document IDs.
    private BoolQueryBuilder filterQuery;
    private String[] storedFields;
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
        QueryBuilder qb = QueryBuilders.wrapperQuery(json);
        // Mostly used for LtR: Restrict the result to a set of documents specified with
        // #setFilterOnFieldValues(String, Collection)
        if (filterQuery != null) {
            qb = filterQuery.must(qb);
        }
        LOG.trace(JsonUtils.prettify(jsonQuery));
        String cacheKey = index + Arrays.toString(storedFields) + Arrays.toString(types) + size + parameters.printToString() + qb.toString().replaceAll("\n", "");
        LOG.debug("Query ID for cache: {}", cacheKey);
        List<Result> result = cache.get(cacheKey);
        if (result == null) {
            if (!(parameters instanceof NoParameters)) {
                //                ElasticSearchSetup.
            }

            result = query(qb, size);
            cache.put(cacheKey, result);
        }
        return result;
    }

    private List<Result> query(QueryBuilder qb, int size) {

        try {
            final SearchSourceBuilder sb = new SearchSourceBuilder().query(qb).size(size).storedField("_id");
            if (storedFields != null)
                sb.fetchSource(storedFields, null);
            SearchResponse response = client.search(new SearchRequest(index).source(sb), RequestOptions.DEFAULT);
            //LOG.trace(JsonUtils.prettify(response.toString()));

            SearchHit[] results = response.getHits().getHits();

            List<Result> ret = new ArrayList<>();
            for (SearchHit hit : results) {
                Result result = new Result(hit.getId(), hit.getScore());
                result.setSourceFields(hit.getSourceAsMap());
                ret.add(result);
            }

            return ret;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
