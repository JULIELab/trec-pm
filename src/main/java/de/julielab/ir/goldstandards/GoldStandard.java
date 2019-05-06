package de.julielab.ir.goldstandards;

import de.julielab.ir.ltr.DocumentList;
import de.julielab.ir.model.QueryDescription;

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
        final Map<String, Deque<Q>> topicsByProperty = getQueries().collect(Collectors.groupingBy(topicProperty, HashMap::new, Collectors.toCollection(ArrayDeque::new)));

        List<List<Q>> partitioning = new ArrayList<>();
        for (int i = 0; i < nPartitions; i++)
            partitioning.add(new ArrayList<>());

        while (!topicsByProperty.isEmpty()) {
            for (List<Q> partition : partitioning) {
                // When one property value has no topics left (because we will remove the topics when assigning them
                // to a partition), we remove the whole property from the map. This will eventually leave the
                // map empty, triggering the termination criterium of the while loop.
                List<String> emptyProperties = new ArrayList<>();
                for (Map.Entry<String, Deque<Q>> topicsForProperty : topicsByProperty.entrySet()) {
                    final Q topic = topicsForProperty.getValue().pop();
                    if (topicsForProperty.getValue().isEmpty())
                        emptyProperties.add(topicsForProperty.getKey());
                    partition.add(topic);
                }
                for (String property : emptyProperties)
                    topicsByProperty.remove(property);
            }
        }
        return partitioning;
    }

    List<Q> getQueriesAsList();

    Map<Q, DocumentList> getDocumentsPerQuery();

    Map<Integer, Q> getQueriesByNumber();

    DocumentList<Q> getDocumentsForQuery(int queryId);

    default DocumentList<Q> getDocumentsForQuery(Q query) {
        return getDocumentsForQuery(query.getNumber());
    }

    default DocumentList<Q> getDocumentsForQueries(Collection<Q> queries) {
        return queries.stream().map(this::getDocumentsForQuery).flatMap(Collection::stream).collect(Collectors.toCollection(DocumentList::new));
    }
}
