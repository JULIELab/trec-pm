package at.medunigraz.imi.bst.trec.query;

import at.medunigraz.imi.bst.retrieval.Query;
import at.medunigraz.imi.bst.retrieval.QueryDecorator;
import at.medunigraz.imi.bst.trec.model.Result;
import at.medunigraz.imi.bst.trec.model.Topic;
import de.julielab.ir.model.QueryDescription;

import java.util.List;

public abstract class DynamicQueryDecorator extends QueryDecorator<Topic> {

    public DynamicQueryDecorator(Query decoratedQuery) {
        super(decoratedQuery);
    }

    @Override
    public List<Result> query(Topic topic) {
        expandTopic(topic);
        return decoratedQuery.query(topic);
    }

    public abstract Topic expandTopic(Topic topic);
}
