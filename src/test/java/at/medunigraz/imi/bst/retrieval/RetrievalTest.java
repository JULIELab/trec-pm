package at.medunigraz.imi.bst.retrieval;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.trec.experiment.TrecPmRetrieval;
import at.medunigraz.imi.bst.trec.model.ResultList;
import at.medunigraz.imi.bst.trec.model.Topic;
import at.medunigraz.imi.bst.trec.model.TrecPMTopicSetFactory;
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
        final String indexName = TrecConfig.ELASTIC_BA_INDEX;
        final List<Topic> topics = TrecPMTopicSetFactory.topics2019().getTopics();
        final int SIZE = 10;

        ResultList<Topic> firstTopicResults = new TrecPmRetrieval(indexName)
                .withSize(SIZE)
                .withTemplate(TEMPLATE)
                .retrieve(topics, q -> String.valueOf(q.getNumber()))
                .get(0);

        assertEquals(SIZE, firstTopicResults.getResults().size());
    }
}