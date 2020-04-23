package at.medunigraz.imi.bst.trec.query;

import at.medunigraz.imi.bst.retrieval.Query;
import at.medunigraz.imi.bst.retrieval.QueryDecorator;
import at.medunigraz.imi.bst.trec.model.Result;
import at.medunigraz.imi.bst.trec.model.Topic;
import de.julielab.ir.model.QueryDescription;

import java.util.List;

public abstract class DynamicQueryDecorator<Q extends QueryDescription> extends QueryDecorator<Q> {

    public DynamicQueryDecorator(Query decoratedQuery) {
        super(decoratedQuery);
    }

    @Override
    public List<Result> query(Q topic) {
        expandTopic(topic);
        return decoratedQuery.query(topic);
    }

    public abstract Q expandTopic(Q topic);
}
