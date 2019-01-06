package at.medunigraz.imi.bst.trec.query;

import java.util.List;

import org.json.JSONObject;

import at.medunigraz.imi.bst.trec.model.Result;
import at.medunigraz.imi.bst.trec.model.Topic;
import at.medunigraz.imi.bst.trec.search.ElasticSearch;

public class ElasticSearchQuery implements Query {
	
	private String jsonQuery;
	
	private String index;
	private String[] types = null;
	
	public ElasticSearchQuery(String index) {
		this.index = index;
	}
	
	public ElasticSearchQuery(String index, String... types) {
		this.index = index;
		this.types = types;
	}

	@Override
	public List<Result> query(Topic topic) {
		ElasticSearch es = null;
		if (types != null) {
			es = new ElasticSearch(index, types);
		} else {
			es = new ElasticSearch(index);
		}
		return es.query(new JSONObject(jsonQuery));
	}
	
	@Override
	public void setJSONQuery(String jsonQuery) {
		this.jsonQuery = jsonQuery;
	}

	@Override
	public String getJSONQuery() {
		return jsonQuery;
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

}
