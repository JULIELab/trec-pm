package de.julielab.ir.goldstandards;

import at.medunigraz.imi.bst.trec.model.*;
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

    private static final TopicSet TOPICS = TrecPMTopicSetFactory.topics2018();
    private static final File QRELS = new File(TrecQrelGoldStandardTest.class.getResource("/gold-standard/qrels-treceval-abstracts.2018.txt").getPath());
    private static final File SAMPLE_QRELS = new File(TrecQrelGoldStandardTest.class.getResource("/gold-standard/qrels-sample-abstracts.2018.txt").getPath());


    @Test
    public void readQrels() {
        final TrecQrelGoldStandard<Topic> gs = TrecPMGoldStandardFactory.pubmedOfficial2018();

        assertEquals(65906, gs.getSampleQrelDocuments().size());
        assertEquals(22429, gs.getQrelDocuments().size());
        assertEquals(421, gs.getQrelDocumentsForQuery(1).size());
    }

    @Test
    public void writeQrels() throws IOException {
        // Create a document
        final Document<Topic> document = new Document<>();
        document.setQueryDescription(new Topic().withNumber(1));
        document.setId("19860577");
        document.setRelevance(2);
        document.setStratum(1);

        // Add the document to a document list
        DocumentList<Topic> qrelDocuments = new DocumentList<>();
        qrelDocuments.add(document);

        // Create a gold standard with it
        final File generatedQrels = testFolder.newFile("generated.qrels");
        final TrecQrelGoldStandard<Topic> gs = new TrecQrelGoldStandard<>(Challenge.TREC_PM, Task.PUBMED, 2018, GoldStandardType.INTERNAL, TOPICS.getTopics(), qrelDocuments);

        // Traditional qrel file
        gs.writeQrelFile(generatedQrels);
        assertTrue(generatedQrels.exists());
        assertTrue(generatedQrels.length() > 10);   // File must have some content
        assertEquals("1 0 19860577 2", FileUtils.readFileToString(generatedQrels, "UTF-8").trim());

        // Sample file
        gs.writeSampleQrelFile(generatedQrels);
        assertTrue(generatedQrels.exists());
        assertTrue(generatedQrels.length() > 10);   // File must have some content
        assertEquals("1 0 19860577 1 2", FileUtils.readFileToString(generatedQrels, "UTF-8").trim());
    }

    @Test
    public void readAndWriteQrels() throws IOException {
        // Read from official qrels file
        final TrecQrelGoldStandard<Topic> officialGs = TrecPMGoldStandardFactory.pubmedOfficial2018();

        // Extract documents
        DocumentList<Topic> qrelDocuments = officialGs.getQrelDocuments();

        // Write back the documents into a new file
        final File generatedQrels = testFolder.newFile("generated.qrels");
        final TrecQrelGoldStandard<Topic> gsCopy = new TrecQrelGoldStandard<>(Challenge.TREC_PM, Task.PUBMED, 2018, GoldStandardType.OFFICIAL, TOPICS.getTopics(), qrelDocuments);
        gsCopy.writeQrelFile(generatedQrels);

        // Check files are identical
        assertEquals(FileUtils.readFileToString(QRELS, "UTF-8"), FileUtils.readFileToString(generatedQrels, "UTF-8"));
    }

    @Test
    public void readAndWriteSampleQrels() throws IOException {
        // Read from official qrels file
        final TrecQrelGoldStandard<Topic> officialGs = TrecPMGoldStandardFactory.pubmedOfficial2018();

        // Extract documents
        DocumentList<Topic> qrelDocuments = officialGs.getSampleQrelDocuments();

        // Write back the documents into a new file
        final File generatedQrels = testFolder.newFile("generated.qrels");
        final TrecQrelGoldStandard<Topic> gsCopy = new TrecQrelGoldStandard<>(Challenge.TREC_PM, Task.PUBMED, 2018, GoldStandardType.OFFICIAL, TOPICS.getTopics(), qrelDocuments);
        gsCopy.writeSampleQrelFile(generatedQrels);

        // Check files are identical
        assertEquals(FileUtils.readFileToString(SAMPLE_QRELS, "UTF-8"), FileUtils.readFileToString(generatedQrels, "UTF-8"));
    }
}