package at.medunigraz.imi.bst.trec.search;

import at.medunigraz.imi.bst.trec.model.Result;
import org.json.JSONObject;

import java.util.List;

public interface SearchEngine {
    List<Result> query(JSONObject jsonQuery);
}
