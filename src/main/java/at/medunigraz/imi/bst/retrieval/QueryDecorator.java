package at.medunigraz.imi.bst.retrieval;

import at.medunigraz.imi.bst.trec.model.Result;
import de.julielab.ir.model.QueryDescription;

import java.util.List;

public class QueryDecorator<T extends QueryDescription> implements Query<T> {

	protected Query decoratedQuery;

	public QueryDecorator(Query decoratedQuery) {
		this.decoratedQuery = decoratedQuery;
	}

	@Override
	public List<Result> query(T topic) {
		return decoratedQuery.query(topic);
	}

	@Override
	public void setJSONQuery(String jsonQuery) {
		decoratedQuery.setJSONQuery(jsonQuery);
	}

	@Override
	public String getJSONQuery() {
		return decoratedQuery.getJSONQuery();
	}

	@Override
	public String getName() {
		return getMyName() + "_" + decoratedQuery.getName();
	}
	
	protected String getSimpleClassName() {
		return this.getClass().getSimpleName().replace("QueryDecorator", "");
	}
	
	protected String getMyName() {
		return getSimpleClassName();
	}

}
