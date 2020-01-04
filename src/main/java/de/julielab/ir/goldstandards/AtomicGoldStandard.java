package de.julielab.ir.goldstandards;

import at.medunigraz.imi.bst.trec.model.Challenge;
import at.medunigraz.imi.bst.trec.model.GoldStandardType;
import at.medunigraz.imi.bst.trec.model.Task;
import de.julielab.ir.ltr.Document;
import de.julielab.ir.ltr.DocumentList;
import de.julielab.ir.model.QueryDescription;

import java.io.File;
import java.io.IOException;
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
    protected GoldStandardType type;
    protected File documentDatabaseConfiguration;

    /**
     * The documents in {@link #qrelDocuments} grouped by query.
     */
    protected Map<Q, DocumentList<Q>> documentsByQuery;
    /**
     * The queries in {@link #queries} grouped by number.
     */
    protected Map<Integer, Q> queriesByNumber;

    public AtomicGoldStandard(Challenge challenge, Task task, int year, GoldStandardType type, List<Q> queries) {
        this.queries = queries.stream().map(q -> (Q)q.getCleanCopy()).collect(Collectors.toList());
        this.challenge = challenge;
        this.task = task;
        this.year = year;
        this.type = type;
        this.queries.forEach(this::setIndexToQuery);
    }

    public AtomicGoldStandard(Challenge challenge, Task task, int year, GoldStandardType type, List<Q> queries, String qrelsFile, BiFunction<String, Map<Integer, Q>, DocumentList<Q>> qrelsReader) {
        this(challenge, task ,year, type, queries);
        qrelDocuments = qrelsReader.apply(qrelsFile, getQueriesByNumber());
    }

    public AtomicGoldStandard(Challenge challenge, Task task, int year, GoldStandardType type, List<Q> queries, DocumentList<Q> qrelDocuments) {
        this(challenge, task ,year, type, queries);
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
    public Map<Q, DocumentList<Q>> getQrelDocumentsPerQuery() {
        if (documentsByQuery == null) {
            documentsByQuery = getQueries().collect(Collectors.toMap(Function.identity(), q -> new DocumentList<Q>()));
            documentsByQuery.putAll(getQrelDocuments().stream().collect(Collectors.groupingBy(Document::getQueryDescription, Collectors.toCollection(DocumentList::new))));
        }
        return documentsByQuery;
    }

    @Override
    public DocumentList<Q> getQrelDocumentsForQuery(int queryId) {
        return getQrelDocumentsForQuery(getQueriesByNumber().get(queryId));
    }

    @Override
    public DocumentList<Q> getQrelDocuments() {
        if (isSampleGoldStandard()) {
            return convertSampleToTraditional(qrelDocuments);
        }
        return qrelDocuments;
    }

    public void setQrelDocuments(DocumentList qrelDocuments) {
        this.qrelDocuments = qrelDocuments;
    }

    /**
     * Converts a given DocumentList to a non-stratified list.
     *
     * @param sampleQrelDocuments
     * @return
     */
    private DocumentList<Q> convertSampleToTraditional(DocumentList<Q> sampleQrelDocuments) {
        DocumentList<Q> ret = new DocumentList<>();
        for (Document<Q> doc : sampleQrelDocuments) {
            if (doc.getRelevance() != -1) {
                ret.add(doc);
            }
        }
        return ret;
    }

    @Override
    public DocumentList<Q> getSampleQrelDocuments() {
        return qrelDocuments;
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
        return String.valueOf(year);
    }

    @Override
    public GoldStandardType getType() {
        return type;
    }

    public File getDocumentDatabaseConfiguration() {
        return documentDatabaseConfiguration;
    }

    public void setDocumentDatabaseConfiguration(File documentDbConfiguration) throws IOException {
        this.documentDatabaseConfiguration = documentDbConfiguration;
        for (Document<Q> d : qrelDocuments)
            d.setDocumentDbConfiguration(documentDbConfiguration);
    }

    protected abstract void setIndexToQuery(Q query);
}
