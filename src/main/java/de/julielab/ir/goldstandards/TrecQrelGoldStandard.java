package de.julielab.ir.goldstandards;

import at.medunigraz.imi.bst.trec.model.Challenge;
import at.medunigraz.imi.bst.trec.model.GoldStandardType;
import at.medunigraz.imi.bst.trec.model.Task;
import de.julielab.ir.ltr.Document;
import de.julielab.ir.ltr.DocumentList;
import de.julielab.ir.model.QueryDescription;
import de.julielab.java.utilities.FileUtilities;
import de.julielab.java.utilities.IOStreamUtilities;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TrecQrelGoldStandard<Q extends QueryDescription> extends AtomicGoldStandard<Q> {

    private static final Logger log = LogManager.getLogger();

    public TrecQrelGoldStandard(Challenge challenge, Task task, int year, GoldStandardType type, Collection<Q> topics, String qrels) {
        super(challenge, task, year, type, topics.stream().sorted(Comparator.comparingInt(QueryDescription::getNumber)).collect(Collectors.toList()), qrels, TrecQrelGoldStandard::readQrels);
    }

    public TrecQrelGoldStandard(Challenge challenge, Task task, int year, GoldStandardType type, Collection<Q> topics, DocumentList<Q> qrelDocuments) {
        super(challenge, task, year, type, topics.stream().sorted(Comparator.comparingInt(QueryDescription::getNumber)).collect(Collectors.toList()), qrelDocuments);
    }

    private static <Q extends QueryDescription> DocumentList readQrels(String qrels, Map<Integer, Q> queriesByNumber) {
        final DocumentList<Q> documents = new DocumentList();
        try {
            final List<String> lines = IOStreamUtilities.getLinesFromInputStream(FileUtilities.findResource(qrels));
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
        List<String> lines = new ArrayList<>();

        for (Document<?> d : qrelDocuments) {
            // Do not write documents not judged.
            if (d.getRelevance() != -1) {
                lines.add(String.format("%d 0 %s %d", d.getQueryDescription().getNumber(), d.getId(), d.getRelevance()));
            }
        }

        write(lines, qrelFile);
    }

    public void writeSampleQrelFile(File qrelFile) {
        if (!isSampleGoldStandard()) {
            throw new UnsupportedOperationException("This is not a sample gold standard.");
        }

        List<String> lines = new ArrayList<>();
        for (Document<?> d : qrelDocuments) {
            lines.add(String.format("%d 0 %s %d %d", d.getQueryDescription().getNumber(), d.getId(), d.getStratum(), d.getRelevance()));
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
        return q -> String.valueOf(q.getNumber());
    }
}
