package at.medunigraz.imi.bst.trec.search;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.json.JSONObject;

import at.medunigraz.imi.bst.trec.model.Result;
import at.medunigraz.imi.bst.trec.utils.JsonUtils;

public class ElasticSearch implements SearchEngine {

	private static final Logger LOG = LogManager.getLogger();

	private Client client = ElasticClientFactory.getClient();
	
	private String index = "_all";
	private String[] types = new String[0];
	
	public ElasticSearch() {
	}
	
	public ElasticSearch(String index) {
		this.index = index;
	}
	
	public ElasticSearch(String index, String... types) {
		this.index = index;
		this.types = types;
	}
	
	public List<Result> query(JSONObject jsonQuery) {
		QueryBuilder qb = QueryBuilders.wrapperQuery(jsonQuery.toString());
		LOG.trace(JsonUtils.prettify(jsonQuery));
		
		return query(qb);
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
