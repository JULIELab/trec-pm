package at.medunigraz.imi.bst.retrieval;

import java.util.Map;

public abstract class MapQueryDecorator extends QueryDecorator {

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
