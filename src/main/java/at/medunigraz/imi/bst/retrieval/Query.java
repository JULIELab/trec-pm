package at.medunigraz.imi.bst.retrieval;

import java.util.List;

import at.medunigraz.imi.bst.trec.model.Result;
import at.medunigraz.imi.bst.trec.model.Topic;

public interface Query {
    List<Result> query(Topic topic);

    String getJSONQuery();

    void setJSONQuery(String jsonQuery);

    String getName();
}
