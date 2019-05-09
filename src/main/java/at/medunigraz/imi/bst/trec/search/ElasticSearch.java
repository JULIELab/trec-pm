package at.medunigraz.imi.bst.trec.search;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.trec.model.Result;
import at.medunigraz.imi.bst.trec.utils.JsonUtils;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ElasticSearch implements SearchEngine {

	private static final Logger LOG = LogManager.getLogger();

	private Client client = ElasticClientFactory.getClient();

	private String index = "_all";
	private String[] types = new String[0];

	private static HTreeMap<String, List<Result>> resultListCache;
    private static DB filedb;
    private boolean cacheReadOnly = true;

    public ElasticSearch() {

        if (filedb == null) {
            File cacheDir = new File("cache/elasticsearc.db");
            final DBMaker.Maker dbmaker = DBMaker
                    .fileDB(cacheDir.getAbsolutePath())
                    .fileMmapEnable()
                    .transactionEnable()
                    .closeOnJvmShutdown();
            if (TrecConfig.DOCUMENT_DB_CACHE_READ_ONLY && cacheDir.exists())
                dbmaker.readOnly();
            else
                cacheReadOnly = false;
            filedb = dbmaker
                    .make();
            resultListCache = filedb.hashMap("ElasticSearchResultListCache").
                    keySerializer(Serializer.STRING).valueSerializer(Serializer.JAVA).
                    createOrOpen();
        }
	}

	public ElasticSearch(String index) {
		this();
		this.index = index;
	}

	public ElasticSearch(String index, String... types) {
		this();
		this.index = index;
		this.types = types;
	}

	public List<Result> query(JSONObject jsonQuery) {
        final String json = jsonQuery.toString();
        List<Result> result = resultListCache.get(json);
        if (result == null) {
            QueryBuilder qb = QueryBuilders.wrapperQuery(json);
            LOG.trace(JsonUtils.prettify(jsonQuery));

            result = query(qb);
            if (!cacheReadOnly) {
                resultListCache.put(json, result);

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
