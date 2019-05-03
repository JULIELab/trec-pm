package de.julielab.ir.goldstandards;

import at.medunigraz.imi.bst.trec.model.Topic;
import de.julielab.ir.ltr.DocumentList;
import de.julielab.ir.model.QueryDescription;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A convenience class that manages multiple gold standards to make them appear as one.
 */
public class AggregatedGoldStandard<Q extends QueryDescription> implements GoldStandard<Q> {

    Map<Integer, Q> queriesByNumber;
    private Map<String, AtomicGoldStandard<Q>> goldstandards;
    private List<Q> queryList;
    private Map<Q, DocumentList> documentsByQuery;

    public AggregatedGoldStandard(AtomicGoldStandard... goldStandards) {
        this.goldstandards = Stream.of(goldStandards).collect(Collectors.toMap(AtomicGoldStandard::getDatasetId, Function.identity()));
    }

    public DocumentList getDocumentsForTopic(Topic topic) {
        return goldstandards.get(topic.getCrossDatasetId()).getDocumentsForQuery(topic.getNumber());
    }

    /**
     * Returns a stream over all topics of all gold standards, in no specific order.
     *
     * @return The topics of the underlying gold standards.
     */
    public Stream<Q> getTopics() {
        return goldstandards.values().stream().flatMap(GoldStandard::getQueries);
    }

    @Override
    public Stream<Q> getQueries() {
        return goldstandards.values().stream().flatMap(GoldStandard::getQueries);
    }

    @Override
    public String getDatasetId() {
        return goldstandards.values().stream().map(GoldStandard::getDatasetId).collect(Collectors.joining("-"));
    }

    @Override
    public List<Q> getQueriesAsList() {
        if (queryList == null)
            queryList = goldstandards.values().stream().flatMap(GoldStandard::getQueries).collect(Collectors.toList());
        return queryList;
    }

    @Override
    public DocumentList<Q> getDocumentsForQuery(QueryDescription query) {
        return null;
    }

    @Override
    public Map<Q, DocumentList> getDocumentsPerQuery() {
        if (documentsByQuery == null)
            documentsByQuery = getQueries().collect(Collectors.toMap(Function.identity(), this::getDocumentsForQuery));
        return documentsByQuery;
    }

    @Override
    public Map<Integer, Q> getQueriesByNumber() {
        if (queriesByNumber == null)
            queriesByNumber = getQueries().collect(Collectors.toMap(Q::getNumber, Function.identity()));
        return queriesByNumber;
    }

    @Override
    public DocumentList<Q> getDocumentsForQuery(int queryId) {
        return getDocumentsPerQuery().get(queryId);
    }
}
