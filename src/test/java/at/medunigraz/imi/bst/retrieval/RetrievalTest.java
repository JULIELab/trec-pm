package at.medunigraz.imi.bst.retrieval;

import at.medunigraz.imi.bst.trec.experiment.TrecPmRetrieval;
import at.medunigraz.imi.bst.trec.model.*;
import at.medunigraz.imi.bst.trec.utils.ConnectionUtils;
import org.junit.Assume;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class RetrievalTest {

    private static final File TOPICS = new File(RetrievalTest.class.getResource("/topics/topics2019.xml").getPath());

    private static final File TEMPLATE = new File(RetrievalTest.class.getResource("/templates/match-title-thyroid.json").getFile());

    public RetrievalTest() {
        // There must be an available server
        Assume.assumeTrue(ConnectionUtils.checkElasticOpenPort());
    }

    @Test
    public void withSize() {
        final Task task = Task.PUBMED;
        final List<Topic> topics = new TopicSet(TOPICS, Challenge.TREC_PM, Task.PUBMED, 2019).getTopics();
        final int SIZE = 10;

        ResultList<Topic> firstTopicResults = new TrecPmRetrieval()
                .withSize(SIZE)
                .withTarget(task)
                .withTemplate(TEMPLATE)
                .retrieve(topics, q -> String.valueOf(q.getNumber()))
                .get(0);

        assertEquals(SIZE, firstTopicResults.getResults().size());
    }
}