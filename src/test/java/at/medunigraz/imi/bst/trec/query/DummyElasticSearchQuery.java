package at.medunigraz.imi.bst.trec.query;

import java.util.List;

import at.medunigraz.imi.bst.retrieval.ElasticSearchQuery;
import at.medunigraz.imi.bst.trec.model.Result;
import at.medunigraz.imi.bst.trec.model.Topic;

public class DummyElasticSearchQuery extends ElasticSearchQuery {
	
	public DummyElasticSearchQuery() {
		super("NOOP");
	}

	private Topic topic;

	@Override
	public List<Result> query(Topic topic) {
		this.topic = topic;
		
		// NOOP
		return null;
	}
	
	public Topic getTopic() {
		return this.topic;
	}

}
