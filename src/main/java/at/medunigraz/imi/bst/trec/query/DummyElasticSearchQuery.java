package at.medunigraz.imi.bst.trec.query;

import java.util.List;

import at.medunigraz.imi.bst.retrieval.ElasticSearchQuery;
import at.medunigraz.imi.bst.trec.model.Result;
import at.medunigraz.imi.bst.trec.model.Topic;
import de.julielab.ir.model.QueryDescription;

public class DummyElasticSearchQuery<T extends QueryDescription> extends ElasticSearchQuery<T> {
	
	public DummyElasticSearchQuery() {
		super("NOOP");
	}

	private T topic;

	@Override
	public List<Result> query(T topic) {
		this.topic = topic;
		
		// NOOP
		return null;
	}
	
	public T getTopic() {
		return this.topic;
	}

}
