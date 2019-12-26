package at.medunigraz.imi.bst.trec.query;

import at.medunigraz.imi.bst.trec.model.Topic;
import at.medunigraz.imi.bst.trec.model.TopicSet;
import at.medunigraz.imi.bst.trec.model.TrecPMTopicSetFactory;
import de.julielab.ir.umls.UmlsSynsetProvider;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DiseaseUmlsPreferredTermQueryDecoratorTest {
    @Test
    public void testPmTopics() {
        DummyElasticSearchQuery dummyQuery = new DummyElasticSearchQuery();
        final DiseaseUmlsSynonymQueryDecorator decorator = new DiseaseUmlsSynonymQueryDecorator(dummyQuery);
        decorator.setUmlsSynsetProvider(new UmlsSynsetTestProvider(false));
        final TopicSet topicSet = TrecPMTopicSetFactory.topics2018();
        for (Topic topic : topicSet.getTopics()) {
            decorator.expandTopic(topic);
            if (topic.getDisease().equals("melanoma"))
                assertThat(topic.getDiseaseSynonyms()).contains("melanoma synonym");
        }
    }

    private class UmlsSynsetTestProvider extends UmlsSynsetProvider{
        private UmlsSynsetTestProvider(boolean containTermInSynset) {
            super("src/test/resources/umls/melanoma.synset", "src/test/resources/umls/semanticTypes.test", "src/test/resources/umls/example.prefterms",UmlsSynsetProvider.DEFAULT_SEPARATOR, containTermInSynset, false);
        }
    }
}
