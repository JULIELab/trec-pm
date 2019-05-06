package de.julielab.ir.goldstandards;

import at.medunigraz.imi.bst.trec.model.Challenge;
import at.medunigraz.imi.bst.trec.model.Task;
import de.julielab.ir.ltr.Document;
import de.julielab.ir.ltr.DocumentList;
import de.julielab.ir.model.QueryDescription;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>A single gold standard that does not consist of multiple aggregated gold standards (hence, 'atomic') in contract to {@link AggregatedGoldStandard}.</p>
 *
 * @param <Q> The query subclass to use.
 */
public abstract class AtomicGoldStandard<Q extends QueryDescription> implements GoldStandard<Q> {
    /**
     * All documents, across all queries.
     */
    protected DocumentList<Q> documents;
    /**
     * All queries.
     */
    protected List<Q> queries;
    protected Challenge challenge;
    protected Task task;
    protected int year;
    /**
     * The documents in {@link #documents} grouped by query.
     */
    private Map<Q, DocumentList> documentsByQuery;
    /**
     * The queries in {@link #queries} grouped by number.
     */
    private Map<Integer, Q> queriesByNumber;

    public AtomicGoldStandard(Challenge challenge, Task task, int year, List<Q> queries, DocumentList documents) {
        this.challenge = challenge;
        this.task = task;
        this.year = year;
        this.queries = queries;
        this.documents = documents;
    }

    public AtomicGoldStandard(Challenge challenge, Task task, int year, List<Q> queries) {
        this(challenge, task, year, queries, null);
    }

    @Override
    public List<Q> getQueriesAsList() {
        return queries;
    }

    @Override
    public Map<Integer, Q> getQueriesByNumber() {
        if (queriesByNumber == null)
            queriesByNumber = getQueries().collect(Collectors.toMap(Q::getNumber, Function.identity()));
        return queriesByNumber;
    }

    @Override
    public DocumentList getDocumentsForQuery(Q query) {
        return getDocumentsPerQuery().get(query);
    }

    @Override
    public Map<Q, DocumentList> getDocumentsPerQuery() {
        if (documentsByQuery == null)
            documentsByQuery = getDocuments().stream().collect(Collectors.groupingBy(Document::getQueryDescription, Collectors.toCollection(DocumentList::new)));
        return documentsByQuery;
    }

    @Override
    public DocumentList<Q> getDocumentsForQuery(int queryId) {
        return getDocumentsForQuery(getQueriesByNumber().get(queryId));
    }

    public DocumentList<Q> getDocuments() {
        return documents;
    }

    public void setDocuments(DocumentList documents) {
        this.documents = documents;
    }

    public Stream<Q> getQueries() {
        return queries.stream();
    }

    public void setQueries(List<Q> queries) {
        this.queries = queries;
    }

    @Override
    public String getDatasetId() {
        return Stream.of(challenge, task, year).filter(Objects::nonNull).map(String::valueOf).collect(Collectors.joining("-"));
    }
}
