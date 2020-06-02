package at.medunigraz.imi.bst.retrieval;

import at.medunigraz.imi.bst.trec.model.Result;
import de.julielab.ir.goldstandards.GoldStandard;
import de.julielab.ir.ltr.Document;
import de.julielab.ir.ltr.DocumentList;
import de.julielab.ir.model.QueryDescription;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>Removes results for a given query when the result is contained in the given gold standard for this query.</p>
 *
 * @param <Q>
 */
public class ResultListGoldStandardFilterDecorator<Q extends QueryDescription> extends QueryDecorator<Q> {
    private final GoldStandard<Q> gs;
    private Function<Result, String> resultDocIdFunction;

    public ResultListGoldStandardFilterDecorator(Query<Q> decoratedQuery, @Nullable Function<Result, String> resultDocIdFunction, GoldStandard<Q> gs) {
        super(decoratedQuery);
        this.resultDocIdFunction = resultDocIdFunction;
        this.gs = gs;
    }

    @Override
    public List<Result> query(Q topic) {
        List<Result> results = decoratedQuery.query(topic);
        try {
            DocumentList<Q> qrelDocumentsForQuery = gs.getQrelDocumentsForQuery(topic);
            if (qrelDocumentsForQuery != null) {
                Set<String> docIdsInGs4topic = qrelDocumentsForQuery.stream().map(Document::getId).collect(Collectors.toSet());
                List<Result> filteredresults = new ArrayList<>();
                for (Result r : results) {
                    String rId = resultDocIdFunction == null ? r.getId() : resultDocIdFunction.apply(r);
                    if (!docIdsInGs4topic.contains(rId))
                        filteredresults.add(r);
                }
                return filteredresults;
            }
        } catch (IllegalArgumentException e) {
            // do nothing; this topic is not included in the  gold standard so we can't filter for its results
        }
        return results;
    }
}
