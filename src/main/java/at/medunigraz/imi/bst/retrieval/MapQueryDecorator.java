package at.medunigraz.imi.bst.retrieval;

import de.julielab.ir.model.QueryDescription;

import java.util.Map;

public abstract class MapQueryDecorator<T extends QueryDescription> extends QueryDecorator<T> {

    public MapQueryDecorator(Query decoratedQuery) {
        super(decoratedQuery);
    }

    public static String map(String jsonQuery, Map<String, String> keymap) {
        String ret = jsonQuery;
        if (jsonQuery == null)
            throw new IllegalStateException("Cannot find a template. When using the decorator chaining API, make sure to first set the parameters and then the template. The template loading decorator must be a delegate of this decorater that accesses the template.");
        for (Map.Entry<String, String> entry : keymap.entrySet()) {
            String search = String.format("{{%s}}", entry.getKey());
            ret = ret.replace(search, entry.getValue());
        }
        return ret;
    }

    protected void map(Map<String, String> keymap) {
        setJSONQuery(map(getJSONQuery(), keymap));
    }

}
