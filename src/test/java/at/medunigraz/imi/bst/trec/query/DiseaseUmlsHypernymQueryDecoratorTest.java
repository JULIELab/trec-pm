package at.medunigraz.imi.bst.trec.query;

import at.medunigraz.imi.bst.trec.model.Challenge;
import at.medunigraz.imi.bst.trec.model.Task;
import at.medunigraz.imi.bst.trec.model.Topic;
import at.medunigraz.imi.bst.trec.model.TopicSet;
import de.julielab.ir.umls.UmlsRelationsProvider;
import de.julielab.ir.umls.UmlsSynsetProvider;
import org.junit.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class DiseaseUmlsHypernymQueryDecoratorTest {
    @Test
    public void testPmTopics() {
        UmlsRelationsProvider.setRelationsSourceFile("src/test/resources/umls/melanoma.relations");
        UmlsRelationsProvider.setUseCache(false);
        // We must also set the synset provider for testing because it is internally used
        UmlsSynsetProvider.setSynsetSourceFile("src/test/resources/umls/melanoma.synset");
        UmlsSynsetProvider.setUseCache(false);
        DummyElasticSearchQuery dummyQuery = new DummyElasticSearchQuery();
        final DiseaseUmlsHypernymQueryDecorator decorator = new DiseaseUmlsHypernymQueryDecorator(dummyQuery);
        final TopicSet topicSet = new TopicSet(new File("src/main/resources/topics/topics2018.xml"), Challenge.TREC_PM, Task.PUBMED, 2017);
        for (Topic topic : topicSet.getTopics()) {
            decorator.expandTopic(topic);
            if (topic.getDisease().equals("melanoma"))
                assertThat(topic.getDiseaseHypernyms()).contains("neoplasm of skin", "melanocytes", "tumor", "disease of epidermal appendage");
        }
    }
}
