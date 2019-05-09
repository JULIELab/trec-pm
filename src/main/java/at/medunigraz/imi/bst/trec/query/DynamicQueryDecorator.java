package at.medunigraz.imi.bst.trec.query;

import at.medunigraz.imi.bst.retrieval.Query;
import at.medunigraz.imi.bst.retrieval.QueryDecorator;
import at.medunigraz.imi.bst.trec.model.Result;
import at.medunigraz.imi.bst.trec.model.Topic;
import de.julielab.ir.model.QueryDescription;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class DynamicQueryDecorator extends QueryDecorator<Topic> {

    private Set<Topic> expandedTopics = new HashSet<>();

    public DynamicQueryDecorator(Query decoratedQuery) {
        super(decoratedQuery);
    }

    @Override
    public List<Result> query(Topic topic) {
        if (!expandedTopics.contains(topic)) {
            expandTopic(topic);
            expandedTopics.add(topic);
        }
        return decoratedQuery.query(topic);
    }

    public abstract Topic expandTopic(Topic topic);
}
