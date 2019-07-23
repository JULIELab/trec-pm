package at.medunigraz.imi.bst.trec.query;

import at.medunigraz.imi.bst.trec.model.Challenge;
import at.medunigraz.imi.bst.trec.model.Topic;
import at.medunigraz.imi.bst.trec.model.TopicSet;
import de.julielab.ir.umls.UmlsSynsetProvider;
import org.junit.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
public class DiseaseUmlsSynonymQueryDecoratorTest {
    private class UmlsSynsetTestProvider extends UmlsSynsetProvider{
        private UmlsSynsetTestProvider(boolean containTermInSynset) {
            super("src/test/resources/umls/melanoma.synset", UmlsSynsetProvider.DEFAULT_SEPARATOR, containTermInSynset, false);
        }
    }
    @Test
    public void testPmTopics() {
        DummyElasticSearchQuery dummyQuery = new DummyElasticSearchQuery();
        final DiseaseUmlsSynonymQueryDecorator decorator = new DiseaseUmlsSynonymQueryDecorator(dummyQuery);
        decorator.setUmlsSynsetProvider(new UmlsSynsetTestProvider(false));
        final TopicSet topicSet = new TopicSet(new File("src/main/resources/topics/topics2018.xml"), Challenge.TREC_PM, 2017);
        for (Topic topic : topicSet.getTopics()) {
            decorator.expandTopic(topic);
            if (topic.getDisease().equals("melanoma"))
                assertThat(topic.getDiseaseSynonyms()).contains("melanoma synonym");
        }
    }
}
