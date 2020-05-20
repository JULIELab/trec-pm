package at.medunigraz.imi.bst.trec.model;

import de.julielab.ir.model.QueryDescription;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ResultList<T extends QueryDescription> {
	private T topic;
	
	private List<Result> results;
	
	public ResultList(T topic) {
		this(topic, 10);
	}

	public ResultList(T topic, int initialSize) {
		results = new ArrayList<>(initialSize);
	}
	
	public boolean add(Result result) {
		return results.add(result);
	}

	public boolean addAll(Collection<Result> results) {
		return this.results.addAll(results);
	}

	public T getTopic() {
		return topic;
	}
	
	public List<Result> getResults() {
		return results;
	}
}
