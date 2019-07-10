package de.julielab.ir.goldstandards;

import at.medunigraz.imi.bst.trec.model.Challenge;
import at.medunigraz.imi.bst.trec.model.Task;
import at.medunigraz.imi.bst.trec.model.Topic;
import at.medunigraz.imi.bst.trec.model.TopicSet;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class TrecQrelGoldStandardTest {

    private static final File TOPICS = new File(TrecQrelGoldStandardTest.class.getResource("/topics/topics2017.xml").getPath());
    private static final File QRELS = new File(TrecQrelGoldStandardTest.class.getResource("/gold-standard/qrels-treceval-abstracts.2017.txt").getPath());
    private static final File SAMPLE_QRELS = new File(TrecQrelGoldStandardTest.class.getResource("/gold-standard/sample-qrels-final-abstracts.2017.txt").getPath());

    @Test
    public void readQrels() {
        // TODO Use GoldStandardBuilder (#17)
        final TopicSet topics = new TopicSet(TOPICS, Challenge.TREC_PM, Task.PUBMED, 2017);
        final TrecQrelGoldStandard<Topic> gs = new TrecQrelGoldStandard<>(Challenge.TREC_PM, Task.PUBMED, 2017, topics.getTopics(), QRELS, SAMPLE_QRELS);

        assertEquals(22642, gs.getQrelDocuments().size());
        assertEquals(439, gs.getQrelDocumentsForQuery(1).size());
    }
}