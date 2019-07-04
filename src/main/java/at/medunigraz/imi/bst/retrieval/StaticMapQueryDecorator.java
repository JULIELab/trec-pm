package at.medunigraz.imi.bst.retrieval;

import at.medunigraz.imi.bst.trec.model.Result;
import de.julielab.ir.model.QueryDescription;

import java.util.List;
import java.util.Map;

public class StaticMapQueryDecorator<T extends QueryDescription> extends MapQueryDecorator<T> {
	
	private Map<String, String> keymap;

	public StaticMapQueryDecorator(Map<String, String> keymap, Query decoratedQuery) {
		super(decoratedQuery);
		this.keymap = keymap;
	}
	
	@Override
	public List<Result> query(T topic) {
		map(keymap);
		return decoratedQuery.query(topic);
	}
	
	@Override
	protected String getMyName() {
		return getSimpleClassName() + "(" + keymap.values() + ")";
	}
}
