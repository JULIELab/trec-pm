package de.julielab.ir.goldstandards;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.trec.model.Challenge;
import at.medunigraz.imi.bst.trec.model.GoldStandardType;
import at.medunigraz.imi.bst.trec.model.Task;
import de.julielab.ir.ltr.Document;
import de.julielab.ir.ltr.DocumentList;
import de.julielab.ir.model.QueryDescription;
import de.julielab.java.utilities.FileUtilities;
import de.julielab.java.utilities.IOStreamUtilities;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TrecQrelGoldStandard<Q extends QueryDescription> extends AtomicGoldStandard<Q> {

    private static final Logger log = LoggerFactory.getLogger(TrecQrelGoldStandard.class);
    private Function<QueryDescription, String> queryIdFunction = q -> String.valueOf(q.getNumber());

    public TrecQrelGoldStandard(Challenge challenge, Task task, int year, GoldStandardType type, Collection<Q> topics, String qrels) {
        super(challenge, task, year, type, topics.stream().sorted(Comparator.comparingInt(QueryDescription::getNumber)).collect(Collectors.toList()), qrels, TrecQrelGoldStandard::readQrels);
    }

    public TrecQrelGoldStandard(Challenge challenge, Task task, int year, GoldStandardType type, Collection<Q> topics, DocumentList<Q> qrelDocuments) {
        super(challenge, task, year, type, topics.stream().sorted(Comparator.comparingInt(QueryDescription::getNumber)).collect(Collectors.toList()), qrelDocuments);
    }

    private static <Q extends QueryDescription> DocumentList readQrels(String qrels, Map<Integer, Q> queriesByNumber) {
        final DocumentList<Q> documents = new DocumentList();
        try {
            InputStream qrelStream = FileUtilities.findResource(qrels);
            if (qrelStream == null)
                throw new FileNotFoundException("Could not find the qrel file " + qrels + " as classpath resource or regular file.");
            final List<String> lines = IOStreamUtilities.getLinesFromInputStream(qrelStream);
            for (String line : lines) {
                final String[] record = line.split("\\s+");
                if (record.length < 4 || record.length > 5)
                    throw new IllegalArgumentException("Qrel file format error in line '" + line + "': Expected 4 or 5 columns but got " + record.length);
                Integer topicNumber = Integer.valueOf(record[0]);
                String documentId = record[2];
                Integer relevance = Integer.valueOf(record.length > 4 ? record[4] : record[3]);
                Integer stratum = record.length > 4 ? Integer.valueOf(record[3]) : null;

                Q topic = queriesByNumber.get(topicNumber);
                if (topic == null)
                    throw new IllegalArgumentException("The qrels list documents for topic number " + topicNumber + " but in the topics set for the gold standard there is no topic with this number.");
                final Document<Q> document = new Document<>();
                document.setQueryDescription(topic);
                document.setId(documentId);
                document.setRelevance(relevance);
                if (stratum != null)
                    document.setStratum(stratum);

                documents.add(document);
            }
        } catch (IOException e) {
            log.error("Could not read the qrels file", e);
        }
        return documents;
    }

    public void writeQrelFile(File qrelFile) {
        writeQrelFile(qrelFile, null);
    }

    @Override
    public void writeQrelFile(File qrelFile, Collection<Q> queries) {
        List<String> lines = new ArrayList<>();
        Set<String> queryIds = queries == null ? null : queries.stream().map(QueryDescription::getCrossDatasetId).collect(Collectors.toSet());
        Stream<Document<Q>> documents = queries == null ? qrelDocuments.stream() : qrelDocuments.stream().filter(d -> queryIds.contains(d.getQueryDescription().getCrossDatasetId()));

        for (Document<?> d : (Iterable<Document<Q>>) () -> documents.iterator()) {
            // Do not write documents not judged.
            if (d.getRelevance() != -1) {
                lines.add(String.format("%s 0 %s %d", queryIdFunction.apply(d.getQueryDescription()), d.getId(), d.getRelevance()));
            }
        }

        write(lines, qrelFile);
    }

    public void writeSampleQrelFile(File qrelFile) {
        writeSampleQrelFile(qrelFile, null);
    }

    @Override
    public void writeSampleQrelFile(File qrelFile, Collection<Q> queries) {
        if (!isSampleGoldStandard()) {
            throw new UnsupportedOperationException("This is not a sample gold standard.");
        }
        Set<String> queryIds = queries == null ? null : queries.stream().map(QueryDescription::getCrossDatasetId).collect(Collectors.toSet());
        Stream<Document<Q>> documents = queries == null ? qrelDocuments.stream() : qrelDocuments.stream().filter(d -> queryIds.contains(d.getQueryDescription().getCrossDatasetId()));

        List<String> lines = new ArrayList<>();
        for (Document<?> d : (Iterable<Document<Q>>) () -> documents.iterator()) {
            lines.add(String.format("%s 0 %s %d %d", queryIdFunction.apply(d.getQueryDescription()), d.getId(), d.getStratum(), d.getRelevance()));
        }

        write(lines, qrelFile);
    }

    private void write(List<String> lines, File qrelFile) {
        try {
            FileUtils.writeLines(qrelFile, lines);
        } catch (IOException e) {
            log.error("Could not write to file {}", qrelFile);
            throw new IllegalArgumentException(e);
        }
    }

    public boolean isSampleGoldStandard() {
        if (qrelDocuments.size() == 0) {
            return false;
        }
        boolean isSample = qrelDocuments.get(0).isStratified();
        if (!isSample) {
            log.info("This is not a sample gold standard. `sample_eval` cannot be called and thus some metrics (like infNDCG) may not be available.");
        }
        return isSample;
    }



    @Override
    public Function<QueryDescription, String> getQueryIdFunction() {
        return queryIdFunction;
    }

    public void setQueryIdFunction(Function<QueryDescription, String> queryIdFunction) {
        this.queryIdFunction = queryIdFunction;
    }

    @Override
    protected void setIndexToQuery(Q query) {
        if (query.getIndex() != null)
            return;
        String index;
        Challenge challenge = query.getChallenge();
        int year = query.getYear();
        // TODO the queries don't know their task
        if (challenge == Challenge.TREC_PM) {
            if (task == Task.PUBMED) {
                switch (year) {
                    case 2017:
                        index = TrecConfig.ELASTIC_BA_INDEX_2017;
                        break;
                    case 2018:
                        index = TrecConfig.ELASTIC_BA_INDEX_2018;
                        break;
                    case 2019:
                        index = TrecConfig.ELASTIC_BA_INDEX_2019;
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown TREC PM year " + year);
                }
            } else if (task == Task.CLINICAL_TRIALS) {
                switch (year) {
                    case 2017:
                        index = TrecConfig.ELASTIC_CT_INDEX_2017;
                        break;
                    case 2018:
                        index = TrecConfig.ELASTIC_CT_INDEX_2018;
                        break;
                    case 2019:
                        index = TrecConfig.ELASTIC_CT_INDEX_2019;
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown TREC CT year " + year);
                }
            } else {
                throw new IllegalArgumentException("Unknown TREC PM task " + task);
            }
        } else {
            throw new IllegalArgumentException("Unknown challenge " + challenge);
        }
        query.setIndex(index);
    }
}
