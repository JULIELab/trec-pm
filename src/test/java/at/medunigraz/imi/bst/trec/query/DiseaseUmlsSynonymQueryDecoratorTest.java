package at.medunigraz.imi.bst.trec.query;

import at.medunigraz.imi.bst.trec.model.Challenge;
import at.medunigraz.imi.bst.trec.model.Task;
import at.medunigraz.imi.bst.trec.model.Topic;
import at.medunigraz.imi.bst.trec.model.TopicSet;
import org.junit.Test;

import java.io.File;

public class DiseaseUmlsSynonymQueryDecoratorTest {
    @Test
    public void testPmTopics() {
        DummyElasticSearchQuery dummyQuery = new DummyElasticSearchQuery();
        final DiseaseUmlsSynonymQueryDecorator decorator = new DiseaseUmlsSynonymQueryDecorator(dummyQuery);
        final TopicSet topicSet = new TopicSet(new File("src/main/resources/topics/topics2018.xml"), Challenge.TREC_PM, Task.PUBMED, 2017);
        for (Topic topic : topicSet.getTopics()) {
            decorator.expandTopic(topic);
            System.out.println(topic.getDisease());
            System.out.println("   " + topic.diseaseSynonyms);
        }
    }
}
