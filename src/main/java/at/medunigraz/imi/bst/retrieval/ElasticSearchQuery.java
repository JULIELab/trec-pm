package at.medunigraz.imi.bst.retrieval;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.trec.model.Result;
import at.medunigraz.imi.bst.trec.search.ElasticSearch;
import de.julielab.ir.es.SimilarityParameters;
import de.julielab.ir.model.QueryDescription;
import org.json.JSONObject;

import java.util.List;

public class ElasticSearchQuery<T extends QueryDescription> implements Query<T> {

    private String jsonQuery;

    private String[] types = null;

    private String index;
    private SimilarityParameters parameters;

    private int size = TrecConfig.SIZE;

    public ElasticSearchQuery(int size, String index, String... types) {
        this.size = size;
        this.index = index;
        this.types = types;
    }

    public ElasticSearchQuery(String index, String... types) {
        this.index = index;
        this.types = types;
    }

    @Override
    public List<Result> query(T topic) {
        ElasticSearch es;
        if (this.types != null) {
            es = new ElasticSearch(this.index, parameters, this.types);
        } else {
            es = new ElasticSearch(this.index, parameters);
        }
        return es.query(new JSONObject(jsonQuery), size);
    }

    @Override
    public String getJSONQuery() {
        return jsonQuery;
    }

    @Override
    public void setJSONQuery(String jsonQuery) {
        this.jsonQuery = jsonQuery;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    public SimilarityParameters getParameters() {
        return parameters;
    }

    public void setSimilarityParameters(SimilarityParameters parameters) {
        this.parameters = parameters;
    }
}
