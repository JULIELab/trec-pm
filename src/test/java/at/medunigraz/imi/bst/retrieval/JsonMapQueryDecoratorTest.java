package at.medunigraz.imi.bst.retrieval;

import at.medunigraz.imi.bst.TestTopic;
import at.medunigraz.imi.bst.trec.query.DummyElasticSearchQuery;
import org.junit.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.*;

public class JsonMapQueryDecoratorTest {

    @Test
    public void map() {
        TestTopic topic = new TestTopic().withQuery("unicorn");
        String template = "{\"title\":\"${query}\"}";
        String mappedTemplate = new JsonMapQueryDecorator<>(new DummyElasticSearchQuery<>()){}.map(template, topic.getAttributes(), null, Collections.emptyList());
        assertThat(mappedTemplate).contains("\"unicorn\"");
    }

    @Test
    public void mapQuote() {
        TestTopic topic = new TestTopic().withNumber(42);
        String template = "{\"title\":\"${QUOTE number}\"}";
        String mappedTemplate = new JsonMapQueryDecorator<>(new DummyElasticSearchQuery<>()){}.map(template, topic.getAttributes(), null, Collections.emptyList());
        assertThat(mappedTemplate).contains(":\"42\"");
    }

    @Test
    public void mapFloat() {
        TestTopic topic = new TestTopic().withWeight(3.7);
        String template = "{\"title\":\"${weight}\"}";
        String mappedTemplate = new JsonMapQueryDecorator<>(new DummyElasticSearchQuery<>()){}.map(template, topic.getAttributes(), null, Collections.emptyList());
        assertThat(mappedTemplate).contains(":3.7");
    }

    @Test
    public void mapConstantListIndex() {
        TestTopic topic = new TestTopic().withStopFilteredTermList("wand", "harry", "snape");
        String template = "{\"title\":\"${stopFilteredTermList[1]}\"}";
        String mappedTemplate = new JsonMapQueryDecorator<>(new DummyElasticSearchQuery<>()){}.map(template, topic.getAttributes(), null, Collections.emptyList());
        assertThat(mappedTemplate).contains("\"harry\"");
    }

    @Test
    public void mapConstantArrayIndex() {
        TestTopic topic = new TestTopic().withStopFilteredTermArray("wand", "harry", "snape");
        String template = "{\"title\":\"${stopFilteredTermArray[2]}\"}";
        String mappedTemplate = new JsonMapQueryDecorator<>(new DummyElasticSearchQuery<>()){}.map(template, topic.getAttributes(), null, Collections.emptyList());
        assertThat(mappedTemplate).contains("\"snape\"");
    }

    @Test
    public void mapExternalListIndex() {
        TestTopic topic = new TestTopic().withStopFilteredTermList("wand", "harry", "snape");
        String template = "{\"title\":\"${stopFilteredTermList[]}\"}";
        String mappedTemplate = new JsonMapQueryDecorator<>(new DummyElasticSearchQuery<>()){}.map(template, topic.getAttributes(), null, Collections.singletonList(1));
        assertThat(mappedTemplate).contains("\"harry\"");
    }

    @Test
    public void mapExternalArrayIndex() {
        TestTopic topic = new TestTopic().withStopFilteredTermArray("wand", "harry", "snape");
        String template = "{\"title\":\"${stopFilteredTermArray[]}\"}";
        String mappedTemplate = new JsonMapQueryDecorator<>(new DummyElasticSearchQuery<>()){}.map(template, topic.getAttributes(), null, Collections.singletonList(2));
        assertThat(mappedTemplate).contains("\"snape\"");
    }

    @Test
    public void mapJoinedList() {
        TestTopic topic = new TestTopic().withStopFilteredTermList("wand", "harry", "snape");
        String template = "{\"title\":\"${CONCAT stopFilteredTermList}\"}";
        String mappedTemplate = new JsonMapQueryDecorator<>(new DummyElasticSearchQuery<>()){}.map(template, topic.getAttributes(), null, Collections.emptyList());
        assertThat(mappedTemplate).contains("\"wand harry snape\"");
    }

    @Test
    public void mapJoinedArray() {
        TestTopic topic = new TestTopic().withStopFilteredTermArray("wand", "harry", "snape");
        String template = "{\"title\":\"${CONCAT stopFilteredTermArray}\"}";
        String mappedTemplate = new JsonMapQueryDecorator<>(new DummyElasticSearchQuery<>()){}.map(template, topic.getAttributes(), null, Collections.emptyList());
        assertThat(mappedTemplate).contains("\"wand harry snape\"");
    }

    @Test
    public void mapEmbeddedJoinedArray() {
        TestTopic topic = new TestTopic().withStopFilteredTermArray("wand", "harry", "snape");
        String template = "{\"title\":\"goblet of fire ${CONCAT stopFilteredTermArray}\"}";
        String mappedTemplate = new JsonMapQueryDecorator<>(new DummyElasticSearchQuery<>()){}.map(template, topic.getAttributes(), null, Collections.emptyList());
        assertThat(mappedTemplate).contains("\"goblet of fire wand harry snape\"");
    }

    @Test
    public void mapJoinedArrayMultiDimensional() {
        TestTopic topic = new TestTopic().withFriends(new String[]{"hermione", "ron"}, new String[]{"mcgonagall", "dumbledore"});
        String template = "{\"title\":\"${CONCAT friends}\"}";
        String mappedTemplate = new JsonMapQueryDecorator<>(new DummyElasticSearchQuery<>()){}.map(template, topic.getAttributes(), null, Collections.emptyList());
        assertThat(mappedTemplate).contains("\"hermione ron mcgonagall dumbledore\"");
    }

    @Test
    public void mapJsonArray() {
        TestTopic topic = new TestTopic().withStopFilteredTermArray("wand", "harry", "snape");
        String template = "{\"title\":\"${JSONARRAY stopFilteredTermArray}\"}";
        String mappedTemplate = new JsonMapQueryDecorator<>(new DummyElasticSearchQuery<>()){}.map(template, topic.getAttributes(), null, Collections.emptyList());
        assertThat(mappedTemplate).contains(":[\"wand\",\"harry\",\"snape\"]");
    }

    @Test
    public void mapJsonArrayPrimitiveValue() {
        TestTopic topic = new TestTopic().withQuery("magic");
        String template = "{\"title\":\"${JSONARRAY query}\"}";
        String mappedTemplate = new JsonMapQueryDecorator<>(new DummyElasticSearchQuery<>()){}.map(template, topic.getAttributes(), null, Collections.emptyList());
        assertThat(mappedTemplate).contains(":[\"magic\"]");
    }

    @Test
    public void mapJsonArrayMultiDimensions() {
        TestTopic topic = new TestTopic().withFriends(new String[]{"hermione", "ron"}, new String[]{"mcgonagall", "dumbledore"});
        String template = "{\"title\":\"${JSONARRAY friends}\"}";
        String mappedTemplate = new JsonMapQueryDecorator<>(new DummyElasticSearchQuery<>()){}.map(template, topic.getAttributes(), null, Collections.emptyList());
        assertThat(mappedTemplate).isEqualTo("{\"title\":[[\"hermione\",\"ron\"],[\"mcgonagall\",\"dumbledore\"]]}");
    }

    @Test
    public void mapJsonList() {
        TestTopic topic = new TestTopic().withStopFilteredTermList("wand", "harry", "snape");
        String template = "{\"title\":\"${JSONARRAY stopFilteredTermList}\"}";
        String mappedTemplate = new JsonMapQueryDecorator<>(new DummyElasticSearchQuery<>()){}.map(template, topic.getAttributes(), null, Collections.emptyList());
        assertThat(mappedTemplate).contains(":[\"wand\",\"harry\",\"snape\"]");
    }

    @Test
    public void mapFlattenedJsonArray() {
        TestTopic topic = new TestTopic().withFriends(new String[]{"hermione", "ron"}, new String[]{"mcgonagall", "dumbledore"});
        String template = "{\"title\":\"${FLAT JSONARRAY friends}\"}";
        String mappedTemplate = new JsonMapQueryDecorator<>(new DummyElasticSearchQuery<>()){}.map(template, topic.getAttributes(), null, Collections.emptyList());
        assertThat(mappedTemplate).isEqualTo("{\"title\":[\"hermione\",\"ron\",\"mcgonagall\",\"dumbledore\"]}");
    }

    @Test
    public void mapTwoDimensions() {
        TestTopic topic = new TestTopic().withFriends(new String[]{"hermione", "ron"}, new String[]{"mcgonagall", "dumbledore"});
        String template = "{\"title\":\"${friends[1][0]}\"}";
        String mappedTemplate = new JsonMapQueryDecorator<>(new DummyElasticSearchQuery<>()){}.map(template, topic.getAttributes(), null, Collections.emptyList());
        assertThat(mappedTemplate).contains("\"mcgonagall\"");
    }

    @Test
    public void mapNonExistingArrayField() {
        TestTopic topic = new TestTopic().withStopFilteredTermList("wand", "harry", "snape");
        String template = "{\"title\":\"${JSONARRAY doesNotExist}\"}";
        assertThatIllegalArgumentException().isThrownBy(() -> new JsonMapQueryDecorator<>(new DummyElasticSearchQuery<>()) {
        }.map(template, topic.getAttributes(), null, Collections.emptyList())).withMessageContaining("A template contains the topic field reference 'doesNotExist'. However, no value for such a field was provided.");
    }

    @Test
    public void mapIndexOutOfBounds() {
        TestTopic topic = new TestTopic().withStopFilteredTermList("wand", "harry", "snape");
        String template = "{\"title\":\"${stopFilteredTermList[7]}\"}";
        assertThatExceptionOfType(ArrayIndexOutOfBoundsException.class).isThrownBy(() -> new JsonMapQueryDecorator<>(new DummyElasticSearchQuery<>()){}.map(template, topic.getAttributes(), null, Collections.emptyList())).withMessage("The template expression \"${stopFilteredTermList[7]}\" refers to the index 7. However, the value of the field 'stopFilteredTermList' has only 3 elements.");
    }
}