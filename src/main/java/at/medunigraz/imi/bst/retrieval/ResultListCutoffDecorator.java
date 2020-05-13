package at.medunigraz.imi.bst.retrieval;

import at.medunigraz.imi.bst.trec.model.Result;
import de.julielab.ir.model.QueryDescription;

import java.util.List;

public class ResultListCutoffDecorator<Q extends QueryDescription> extends QueryDecorator<Q> {
    private final int cutoff;

    public ResultListCutoffDecorator(Query<Q> decoratedQuery, int cutoff) {
        super(decoratedQuery);
        this.cutoff = cutoff;
    }

    @Override
    public List<Result> query(Q topic) {
        List<Result> result = decoratedQuery.query(topic);
        return cutoff > 0 ? result.subList(0, Math.min(cutoff, result.size())) : result;
    }
}
