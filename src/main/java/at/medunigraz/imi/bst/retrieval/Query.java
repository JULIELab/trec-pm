package at.medunigraz.imi.bst.retrieval;

import at.medunigraz.imi.bst.trec.model.Result;
import de.julielab.ir.model.QueryDescription;

import java.io.Serializable;
import java.util.List;

public interface Query<T extends QueryDescription> extends Serializable {
    List<Result> query(T topic);

    String getJSONQuery();

    void setJSONQuery(String jsonQuery);

    String getName();
}
