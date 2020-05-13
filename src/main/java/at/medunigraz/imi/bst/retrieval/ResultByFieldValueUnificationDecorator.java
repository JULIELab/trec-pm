package at.medunigraz.imi.bst.retrieval;

import at.medunigraz.imi.bst.trec.model.Result;
import de.julielab.ir.model.QueryDescription;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Filters the result list with respect to a given index field. Only retains the document with the first occurrence of each field value.
 * @param <Q>
 */
public class ResultByFieldValueUnificationDecorator<Q extends QueryDescription> extends QueryDecorator<Q> {
    private final String field;

    public ResultByFieldValueUnificationDecorator(Query<Q> decoratedQuery, String field) {
        super(decoratedQuery);
        this.field = field;
    }

    @Override
    public List<Result> query(Q topic) {
        List<Result> result = decoratedQuery.query(topic);
        List<Result> unifiedResult = new ArrayList<>();
        Set<Object> seenFieldValues = new HashSet<>();
        for (Result r : result) {
            if (seenFieldValues.add(r.getSourceFields().get(field)))
                unifiedResult.add(r);
        }
        return unifiedResult;
    }
}
