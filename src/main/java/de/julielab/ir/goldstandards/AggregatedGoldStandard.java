package de.julielab.ir.goldstandards;

import at.medunigraz.imi.bst.trec.model.Topic;
import de.julielab.ir.ltr.Document;
import de.julielab.ir.ltr.DocumentList;
import de.julielab.ir.model.QueryDescription;
import de.julielab.java.utilities.FileUtilities;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A convenience class that manages multiple gold standards to make them appear as one.
 */
public abstract class AggregatedGoldStandard<Q extends QueryDescription> implements GoldStandard<Q> {

    private Map<Integer, Q> queriesByNumber;
    private Map<String, Q> queriesByCrossDatasetId;
    private Logger log;
    protected Map<String, AtomicGoldStandard<Q>> goldStandards;
    private List<Q> queryList;
    private Map<Q, DocumentList> documentsByQuery;
    private File qrelFile;
    private File sampleQrelFile;

    public AggregatedGoldStandard(Logger log, File qrelFile, Function<Document, String> qrelRecordFunction, AtomicGoldStandard... goldStandards) {
        this(log, qrelFile, null, qrelRecordFunction, null, goldStandards);
    }

    public AggregatedGoldStandard(Logger log, File qrelFile, File sampleQrelFile, Function<Document, String> qrelRecordFunction, Function<Document, String> sampleQrelRecordFunction, AtomicGoldStandard... goldStandards) {
        this.log = log;
        this.qrelFile = qrelFile;
        this.sampleQrelFile = sampleQrelFile;
        this.goldStandards = Stream.of(goldStandards).collect(Collectors.toMap(AtomicGoldStandard::getDatasetId, Function.identity()));
        if (qrelFile != null)
            writeAggregatedQrelFile(qrelFile, goldStandards, gs -> gs.getQrelDocuments(), qrelRecordFunction);
        if (sampleQrelFile != null) {
            writeAggregatedQrelFile(sampleQrelFile, goldStandards, gs -> gs.getSampleQrelDocuments(), sampleQrelRecordFunction);
        }
    }

    @Override
    public DocumentList<Q> getSampleQrelDocuments() {
        return goldStandards.values().stream().map(GoldStandard::getSampleQrelDocuments).flatMap(Collection::stream).collect(Collectors.toCollection(DocumentList::new));
    }

    @Override
    public DocumentList<Q> getQrelDocuments() {
        return goldStandards.values().stream().map(GoldStandard::getQrelDocuments).flatMap(Collection::stream).collect(Collectors.toCollection(DocumentList::new));
    }

    public DocumentList getDocumentsForTopic(Topic topic) {
        return goldStandards.get(topic.getCrossDatasetId()).getQrelDocumentsForQuery(topic.getNumber());
    }

    /**
     * Returns a stream over all topics of all gold standards, in no specific order.
     *
     * @return The topics of the underlying gold standards.
     */
    public Stream<Q> getTopics() {
        return goldStandards.values().stream().flatMap(GoldStandard::getQueries);
    }

    @Override
    public Stream<Q> getQueries() {
        return goldStandards.values().stream().flatMap(GoldStandard::getQueries);
    }

    @Override
    public String getDatasetId() {
        return goldStandards.values().stream().map(GoldStandard::getDatasetId).collect(Collectors.joining("-"));
    }

    @Override
    public List<Q> getQueriesAsList() {
        if (queryList == null)
            queryList = goldStandards.values().stream().flatMap(GoldStandard::getQueries).collect(Collectors.toList());
        return queryList;
    }

    @Override
    public DocumentList<Q> getQrelDocumentsForQuery(QueryDescription query) {
        return getQrelDocumentsPerQuery().get(query);
    }

    @Override
    public Map<Q, DocumentList> getQrelDocumentsPerQuery() {
        if (documentsByQuery == null) {
            documentsByQuery = goldStandards.values().stream().map(GoldStandard::getQrelDocumentsPerQuery).collect(HashMap::new, (m1, m2) -> m1.putAll(m2), (m1, m2) -> m1.putAll(m2));
        }
        return documentsByQuery;
    }

    @Override
    public Map<Integer, Q> getQueriesByNumber() {
        if (queriesByNumber == null)
            queriesByNumber = getQueries().collect(Collectors.toMap(Q::getNumber, Function.identity()));
        return queriesByNumber;
    }

    public Map<String, Q> getQueriesByCrossDatasetId() {
        if (queriesByCrossDatasetId == null)
            queriesByCrossDatasetId = getQueries().collect(Collectors.toMap(Q::getCrossDatasetId, Function.identity()));
        return queriesByCrossDatasetId;
    }

    @Override
    public DocumentList<Q> getQrelDocumentsForQuery(int queryId) {
        return getQrelDocumentsPerQuery().get(queryId);
    }

    protected void writeAggregatedQrelFile(File qrelFile, GoldStandard<Q>[] goldStandards, Function<GoldStandard<Q>, DocumentList<Q>> documentListFunction, Function<Document, String> recordFunction) {
        if (!qrelFile.exists()) {
            if (!qrelFile.getParentFile().exists())
                qrelFile.getParentFile().mkdirs();
            try (final BufferedWriter bw = FileUtilities.getWriterToFile(qrelFile)) {
                for (GoldStandard<Q> gs : goldStandards) {
                    final DocumentList<Q> gsDocs = documentListFunction.apply(gs);
                    for (Document gsDoc : gsDocs) {
                        String line = recordFunction.apply(gsDoc);
                        bw.write(line);
                        bw.newLine();
                    }
                }
            } catch (IOException e) {
                log.error("Exception while writing aggregated qrel file to {}", qrelFile, e);
            }
        }
    }

    @Override
    public File getQrelFile() {
        return qrelFile;
    }

    @Override
    public File getSampleQrelFile() {
        return sampleQrelFile;
    }
}
