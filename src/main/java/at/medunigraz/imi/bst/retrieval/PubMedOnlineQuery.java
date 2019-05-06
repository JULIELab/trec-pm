package at.medunigraz.imi.bst.retrieval;

import at.medunigraz.imi.bst.trec.model.Result;
import at.medunigraz.imi.bst.trec.model.Topic;
import at.medunigraz.imi.bst.trec.search.PubMedOnline;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PubMedOnlineQuery implements Query<Topic> {
    private PubMedOnline pmo = new PubMedOnline();
    @Override
    public List<Result> query(Topic topic) {
        List<String> l = new ArrayList<>();
        // disease
        l.add(topic.getDisease());
        l.add(topic.diseasePreferredTerm);
        l.addAll(topic.diseaseSynonyms);
        l.addAll(topic.diseaseHypernyms);
        // gene
        l.add(topic.getGene());
        l.addAll(topic.geneSynonyms);
        l.addAll(topic.geneHypernyms);
        l.addAll(topic.geneDescriptions);


        final JSONObject jsonQuery = new JSONObject();
        jsonQuery.put("query", l.stream().collect(Collectors.joining(" OR ")));
        return pmo.query(jsonQuery);
    }

    @Override
    public String getJSONQuery() {
        return null;
    }

    @Override
    public void setJSONQuery(String pubmedJsonQuery) {
        // nothing
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }
}
