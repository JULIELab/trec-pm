package de.julielab.ir.goldstandards;

import at.medunigraz.imi.bst.trec.model.Challenge;
import at.medunigraz.imi.bst.trec.model.Task;
import de.julielab.ir.ltr.Document;
import de.julielab.ir.ltr.DocumentList;
import de.julielab.ir.model.QueryDescription;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
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
     * All documents from the qrel file, across all queries.
     */
    protected DocumentList<Q> qrelDocuments;
    /**
     * All queries.
     */
    protected List<Q> queries;
    protected Challenge challenge;
    protected Task task;
    protected int year;

    /**
     * The documents in {@link #qrelDocuments} grouped by query.
     */
    protected Map<Q, DocumentList> documentsByQuery;
    /**
     * The queries in {@link #queries} grouped by number.
     */
    protected Map<Integer, Q> queriesByNumber;

    public AtomicGoldStandard(Challenge challenge, Task task, int year, List<Q> queries, File qrelsFile, BiFunction<File, Map<Integer, Q>, DocumentList<Q>> qrelsReader) {
        this.challenge = challenge;
        this.task = task;
        this.year = year;
        this.queries = queries;
        qrelDocuments = qrelsReader.apply(qrelsFile, getQueriesByNumber());
    }

    public AtomicGoldStandard(Challenge challenge, Task task, int year, List<Q> queries, DocumentList<Q> qrelDocuments) {
        this.challenge = challenge;
        this.task = task;
        this.year = year;
        this.queries = queries;
        this.qrelDocuments = qrelDocuments;
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
    public DocumentList getQrelDocumentsForQuery(Q query) {
        return getQrelDocumentsPerQuery().get(query);
    }

    @Override
    public Map<Q, DocumentList> getQrelDocumentsPerQuery() {
        if (documentsByQuery == null)
            documentsByQuery = getQrelDocuments().stream().collect(Collectors.groupingBy(Document::getQueryDescription, Collectors.toCollection(DocumentList::new)));
        return documentsByQuery;
    }

    @Override
    public DocumentList<Q> getQrelDocumentsForQuery(int queryId) {
        return getQrelDocumentsForQuery(getQueriesByNumber().get(queryId));
    }

    @Override
    public DocumentList<Q> getQrelDocuments() {
        return qrelDocuments;
    }

    public void setQrelDocuments(DocumentList qrelDocuments) {
        this.qrelDocuments = qrelDocuments;
    }

    public Stream<Q> getQueries() {
        return queries.stream();
    }

    public void setQueries(List<Q> queries) {
        this.queries = queries;
    }

    public Task getTask() {
        return task;
    }

    @Override
    public String getDatasetId() {
//        return Stream.of(challenge, task, year).filter(Objects::nonNull).map(String::valueOf).collect(Collectors.joining("-"));
        return String.valueOf(year);
    }

}
