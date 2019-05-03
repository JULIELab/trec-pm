package at.medunigraz.imi.bst.retrieval;

import java.util.List;

import at.medunigraz.imi.bst.trec.model.Result;
import at.medunigraz.imi.bst.trec.model.Topic;

public interface Query {
	public List<Result> query(Topic topic);
	
	public void setJSONQuery(String jsonQuery);
	
	public String getJSONQuery();
	
	public String getName();
}
