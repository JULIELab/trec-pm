package at.medunigraz.imi.bst.retrieval;

import de.julielab.ir.model.QueryDescription;

import java.util.Map;

public abstract class MapQueryDecorator<T extends QueryDescription> extends QueryDecorator<T> {

	public MapQueryDecorator(Query decoratedQuery) {
		super(decoratedQuery);
	}

	protected void map(Map<String, String> keymap) {
		setJSONQuery(map(getJSONQuery(), keymap));
	}

	public static String map(String jsonQuery, Map<String, String> keymap) {
		String ret = jsonQuery;
		for (Map.Entry<String, String> entry : keymap.entrySet()) {
			String search = String.format("{{%s}}", entry.getKey());
			ret = ret.replace(search, entry.getValue());
		}
		return ret;
	}

}
