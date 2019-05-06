package at.medunigraz.imi.bst.retrieval;

import java.util.List;

import at.medunigraz.imi.bst.trec.experiment.Experiment;
import at.medunigraz.imi.bst.trec.model.GoldStandard;
import de.julielab.ir.model.QueryDescription;
import org.json.JSONObject;

import at.medunigraz.imi.bst.trec.model.Result;
import at.medunigraz.imi.bst.trec.model.Topic;
import at.medunigraz.imi.bst.trec.search.ElasticSearch;

public class ElasticSearchQuery<T extends QueryDescription> implements Query<T> {

    private  GoldStandard goldStandard;
    private String jsonQuery;
	
	private String[] types = null;

	private String index;
	
    public ElasticSearchQuery(GoldStandard goldStandard) {
        this.goldStandard = goldStandard;
    }

    public ElasticSearchQuery(String index, String... types) {
        this.index = index;
        this.types = types;
    }

    @Override
	public List<Result> query(T topic) {
		ElasticSearch es;
		final String index = this.index != null ? this.index : Experiment.getIndexName(topic.getTask());
        final String[] types = this.types != null ? this.types : Experiment.getTypes(topic.getTask(), goldStandard);
        if (this.types != null) {
			es = new ElasticSearch(index, this.types);
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
