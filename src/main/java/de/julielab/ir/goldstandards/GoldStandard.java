package de.julielab.ir.goldstandards;

import at.medunigraz.imi.bst.trec.model.GoldStandardType;
import de.julielab.ir.ltr.DocumentList;
import de.julielab.ir.model.QueryDescription;

import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface GoldStandard<Q extends QueryDescription> {
    Stream<Q> getQueries();

    String getDatasetId();

    default List<List<Q>> createStratifiedTopicPartitioning(int nPartitions, Function<Q, String> topicProperty) {
        // We will first group all the topics by the property to be stratified by, e.g. the disease topic field.
        // Then we will iterate through the (initially empty) partitions as long as there are still topics to distribute.
        // For each iteration round we put one topic from each property value into the current partition.
        // This way, the different property values are as well distributed over the partitions as possible.
        final Map<String, Deque<Q>> topicsByProperty = getQueries().collect(Collectors.groupingBy(topicProperty, LinkedHashMap::new, Collectors.toCollection(ArrayDeque::new)));

        List<Map.Entry<String, Deque<Q>>> propertiesSortedByFrequency = new ArrayList<>(topicsByProperty.entrySet());
        propertiesSortedByFrequency.sort((e1,e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size()));
        final ArrayDeque<Map.Entry<String, Deque<Q>>> deque = new ArrayDeque<>(propertiesSortedByFrequency);

        List<List<Q>> partitioning = new ArrayList<>();
        for (int i = 0; i < nPartitions; i++)
            partitioning.add(new ArrayList<>());

        while (!deque.isEmpty()) {
            for (List<Q> partition : partitioning) {
                if (deque.isEmpty())
                    break;
                final Q pop = deque.peekFirst().getValue().pop();
                partition.add(pop);
                if (deque.peekFirst().getValue().isEmpty())
                    deque.removeFirst();
            }
        }
        return partitioning;
    }

    List<Q> getQueriesAsList();

    Map<Q, DocumentList> getQrelDocumentsPerQuery();

    /**
     * Writes the underlying data structure to a traditional qrel file.
     * @param qrelFile
     */
    void writeQrelFile(File qrelFile);

    /**
     *  Writes the underlying data structure to a sample qrel file, if possible.
     * @param qrelFile
     */
    void writeSampleQrelFile(File qrelFile);

    boolean isSampleGoldStandard();

    DocumentList<Q> getQrelDocuments();

    DocumentList<Q> getSampleQrelDocuments();

    Map<Integer, Q> getQueriesByNumber();

    DocumentList<Q> getQrelDocumentsForQuery(int queryId);

    default DocumentList<Q> getQrelDocumentsForQuery(Q query) {
        return getQrelDocumentsForQuery(query.getNumber());
    }

    default DocumentList<Q> getQrelDocumentsForQueries(Collection<Q> queries) {
        return queries.stream().map(this::getQrelDocumentsForQuery).flatMap(Collection::stream).collect(Collectors.toCollection(DocumentList::new));
    }

    Function<QueryDescription, String> getQueryIdFunction();

    GoldStandardType getType();
}
