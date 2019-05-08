package de.julielab.ir.goldstandards;

import at.medunigraz.imi.bst.trec.model.Challenge;
import at.medunigraz.imi.bst.trec.model.Task;
import de.julielab.ir.ltr.Document;
import de.julielab.ir.ltr.DocumentList;
import de.julielab.ir.model.QueryDescription;
import de.julielab.java.utilities.FileUtilities;
import de.julielab.java.utilities.IOStreamUtilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TrecQrelGoldStandard<Q extends QueryDescription> extends AtomicGoldStandard<Q> {

    private static final Logger log = LogManager.getLogger();
    private File qrels;

    public File getQrelFile() {
        return qrels;
    }

    public TrecQrelGoldStandard(Challenge challenge, Task task, int year, Collection<Q> topics, File qrels) {
        super(challenge, task, year, topics.stream().sorted(Comparator.comparingInt(QueryDescription::getNumber)).collect(Collectors.toList()));
        this.qrels = qrels;
        setDocuments(readQrels(qrels));
    }

    private DocumentList readQrels(File qrels) {
        final Map<Integer, Q> queriesByNumber = getQueriesByNumber();
        final DocumentList<Q> documents = new DocumentList();
        try {
            final List<String> lines = IOStreamUtilities.getLinesFromInputStream(FileUtilities.getInputStreamFromFile(qrels));
            for (String line : lines) {
                final String[] record = line.split("\\s+");
                if (record.length != 4)
                    throw new IllegalArgumentException("Qrel file format error in line '" + line + "': Expected 4 columns but got " + record.length);
                Integer topicNumber = Integer.valueOf(record[0]);
                String documentId = record[2];
                Integer relevance = Integer.valueOf(record[3]);

                Q topic = queriesByNumber.get(topicNumber);
                if (topic == null)
                    throw new IllegalArgumentException("The qrels list documents for topic number " + topicNumber + " but in the topics set for the gold standard there is no topic with this number.");
                final Document<Q> document = new Document<>();
                document.setTopic(topic);
                document.setId(documentId);
                document.setRelevance(relevance);

                documents.add(document);
            }
        } catch (IOException e) {
            log.error("Could not read the qrels file", e);
        }
        return documents;
    }
}
