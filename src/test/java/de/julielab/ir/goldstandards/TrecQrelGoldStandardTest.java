package de.julielab.ir.goldstandards;

import at.medunigraz.imi.bst.trec.model.Challenge;
import at.medunigraz.imi.bst.trec.model.Task;
import at.medunigraz.imi.bst.trec.model.Topic;
import at.medunigraz.imi.bst.trec.model.TopicSet;
import de.julielab.ir.ltr.Document;
import de.julielab.ir.ltr.DocumentList;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TrecQrelGoldStandardTest {

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    private static final TopicSet TOPICS = new TopicSet(new File(TrecQrelGoldStandardTest.class.getResource("/topics/topics2017.xml").getPath()), Challenge.TREC_PM, 2017);
    private static final File QRELS = new File(TrecQrelGoldStandardTest.class.getResource("/gold-standard/qrels-treceval-abstracts.2017.txt").getPath());
    private static final File SAMPLE_QRELS = new File(TrecQrelGoldStandardTest.class.getResource("/gold-standard/sample-qrels-final-abstracts.2017.txt").getPath());

    @Test
    public void readQrels() {
        // TODO Use GoldStandardBuilder (#17)
        final TrecQrelGoldStandard<Topic> gs = new TrecQrelGoldStandard<>(Challenge.TREC_PM, Task.PUBMED, 2017, TOPICS.getTopics(), QRELS, SAMPLE_QRELS);

        assertEquals(22642, gs.getQrelDocuments().size());
        assertEquals(439, gs.getQrelDocumentsForQuery(1).size());
    }

    @Test
    public void writeQrels() throws IOException {
        // Create a document
        final Document<Topic> document = new Document<>();
        document.setQueryDescription(new Topic().withNumber(1));
        document.setId("19860577");
        document.setRelevance(2);

        // Add the document to the document list
        DocumentList<Topic> qrelDocuments = new DocumentList<>();
        qrelDocuments.add(document);

        // Create a gold standard with it
        final File generatedQrels = testFolder.newFile("generated.qrels");
        final TrecQrelGoldStandard<Topic> gs = new TrecQrelGoldStandard<>(Challenge.TREC_PM, Task.PUBMED, 2017, TOPICS.getTopics(), qrelDocuments);
        gs.writeQrelFile(generatedQrels);

        assertTrue(generatedQrels.exists());
        assertTrue(generatedQrels.length() > 10);   // File must have some content
        assertEquals("1 0 19860577 2", FileUtils.readFileToString(generatedQrels, "UTF-8").trim());
    }

    @Test
    public void readAndWriteQrels() throws IOException {
        // Read from official qrels file
        final TrecQrelGoldStandard<Topic> officialGs = new TrecQrelGoldStandard<>(Challenge.TREC_PM, Task.PUBMED, 2017, TOPICS.getTopics(), QRELS, SAMPLE_QRELS);

        // Extract documents
        DocumentList<Topic> qrelDocuments = officialGs.getQrelDocuments();

        // Write back the documents into a new file
        final File generatedQrels = testFolder.newFile("generated.qrels");
        final TrecQrelGoldStandard<Topic> gsCopy = new TrecQrelGoldStandard<>(Challenge.TREC_PM, Task.PUBMED, 2017, TOPICS.getTopics(), qrelDocuments);
        gsCopy.writeQrelFile(generatedQrels);

        // Check files are identical
        assertEquals(FileUtils.readFileToString(QRELS, "UTF-8"), FileUtils.readFileToString(generatedQrels, "UTF-8"));
    }
}