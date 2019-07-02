package at.medunigraz.imi.bst.retrieval;

import java.util.List;

import at.medunigraz.imi.bst.trec.model.Result;
import at.medunigraz.imi.bst.trec.model.Topic;
import de.julielab.ir.model.QueryDescription;

public interface Query<T extends QueryDescription> {
    List<Result> query(T topic);

    String getJSONQuery();

    void setJSONQuery(String jsonQuery);

    String getName();
}
