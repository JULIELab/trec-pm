package at.medunigraz.imi.bst.retrieval;

import at.medunigraz.imi.bst.trec.model.Result;
import de.julielab.ir.model.QueryDescription;
import de.julielab.java.utilities.FileUtilities;
import de.julielab.java.utilities.IOStreamUtilities;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * <p>Removes results for a given query when the result is contained in the given gold standard for this query.</p>
 *
 * @param <Q>
 */
public class ValidDocIdsFilterDecorator<Q extends QueryDescription> extends QueryDecorator<Q> {
    private final String validDocIdFile;
    private Function<Result, String> resultDocIdFunction;
    private Set<String> validDocIds;

    public ValidDocIdsFilterDecorator(Query<Q> decoratedQuery, @Nullable Function<Result, String> resultDocIdFunction, String validDocIdFile) {
        super(decoratedQuery);
        this.resultDocIdFunction = resultDocIdFunction;
        this.validDocIdFile = validDocIdFile;
    }

    @Override
    public List<Result> query(Q topic) {
        if (validDocIds == null)
            readValidDocIds(validDocIdFile);
        List<Result> results = decoratedQuery.query(topic);
        List<Result> filteredresults = new ArrayList<>();
        for (Result r : results) {
            String rId = resultDocIdFunction == null ? r.getId() : resultDocIdFunction.apply(r);
            if (validDocIds.contains(rId))
                filteredresults.add(r);
        }
        return filteredresults;
    }

    private void readValidDocIds(String validDocIdFile) {
        try (BufferedReader br = IOStreamUtilities.getReaderFromInputStream(FileUtilities.findResource(validDocIdFile))) {
            validDocIds = br.lines().map(String::trim).filter(Predicate.not(String::isBlank)).collect(Collectors.toSet());
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
