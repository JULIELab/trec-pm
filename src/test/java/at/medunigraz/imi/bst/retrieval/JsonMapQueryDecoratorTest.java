package at.medunigraz.imi.bst.retrieval;

import at.medunigraz.imi.bst.TestTopic;
import at.medunigraz.imi.bst.trec.query.DummyElasticSearchQuery;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
public class JsonMapQueryDecoratorTest {

    @Test
    public void map() {
        TestTopic topic = new TestTopic().withQuery("unicorn");
        String template = "{\"title\":\"${query}\"}";
        String mappedTemplate = new JsonMapQueryDecorator<>(new DummyElasticSearchQuery<>()){}.map(template, topic.getAttributes(), -1);
        assertThat(mappedTemplate).contains("\"unicorn\"");
    }

    @Test
    public void mapQuote() {
        TestTopic topic = new TestTopic().withNumber(42);
        String template = "{\"title\":\"${QUOTE number}\"}";
        String mappedTemplate = new JsonMapQueryDecorator<>(new DummyElasticSearchQuery<>()){}.map(template, topic.getAttributes(), -1);
        assertThat(mappedTemplate).contains(":\"42\"");
    }

    @Test
    public void mapConstantListIndex() {
        TestTopic topic = new TestTopic().withStopFilteredTermList("wand", "harry", "snape");
        String template = "{\"title\":\"${stopFilteredTermList[1]}\"}";
        String mappedTemplate = new JsonMapQueryDecorator<>(new DummyElasticSearchQuery<>()){}.map(template, topic.getAttributes(), -1);
        assertThat(mappedTemplate).contains("\"harry\"");
    }

    @Test
    public void mapConstantArrayIndex() {
        TestTopic topic = new TestTopic().withStopFilteredTermArray("wand", "harry", "snape");
        String template = "{\"title\":\"${stopFilteredTermArray[2]}\"}";
        String mappedTemplate = new JsonMapQueryDecorator<>(new DummyElasticSearchQuery<>()){}.map(template, topic.getAttributes(), -1);
        assertThat(mappedTemplate).contains("\"snape\"");
    }
}