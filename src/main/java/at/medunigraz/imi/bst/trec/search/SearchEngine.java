package at.medunigraz.imi.bst.trec.search;

import java.util.List;

import org.json.JSONObject;

import at.medunigraz.imi.bst.trec.model.Result;

public interface SearchEngine {
    List<Result> query(JSONObject jsonQuery);
}
