package at.medunigraz.imi.bst.trec.query;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.retrieval.ElasticSearchQuery;
import at.medunigraz.imi.bst.retrieval.Query;
import at.medunigraz.imi.bst.retrieval.StaticMapQueryDecorator;
import at.medunigraz.imi.bst.retrieval.TemplateQueryDecorator;
import at.medunigraz.imi.bst.trec.model.Topic;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TemplateQueryDecoratorTest extends QueryDecoratorTest {

	private static final String DISEASE_1 = "thyroid";
	private static final String DISEASE_2 = "breast";

	private final File template = new File(getClass().getResource("/templates/match-title.json").getFile());

	public TemplateQueryDecoratorTest() {
		this.decoratedQuery = new TemplateQueryDecorator(template, new ElasticSearchQuery(TrecConfig.ELASTIC_BA_INDEX));
		this.topic = new Topic().withDisease(DISEASE_1);
	}

	@Test
	public void testGetJSONQuery() {
		Query decoratedQuery = new TemplateQueryDecorator(template, new DummyElasticSearchQuery());
		
		topic.withDisease(DISEASE_1);
		decoratedQuery.query(topic);
		String actual = decoratedQuery.getJSONQuery();
		String expected = String.format("{\"match\":{\"title\":\"%s\"}}", DISEASE_1);
		assertEquals(expected, actual);
		
		topic.withDisease(DISEASE_2);
		decoratedQuery.query(topic);
		actual = decoratedQuery.getJSONQuery();
		expected = String.format("{\"match\":{\"title\":\"%s\"}}", DISEASE_2);
		assertEquals(expected, actual);
	}

	@Test
	public void testUseTopicNumberInTemplate() {
		File template = new File(getClass().getResource("/templates/topicnumber-template.json").getFile());
		Query decoratedQuery = new TemplateQueryDecorator(template, new DummyElasticSearchQuery());

		topic.withNumber(42);
		decoratedQuery.query(topic);
		String actual = decoratedQuery.getJSONQuery();
		String expected = "{\"match\":{\"topic_42\": \"searchterm\"}}";
		assertEquals(expected, actual);
	}

    @Test
    public void testTemplateProperties() {
        File template = new File(getClass().getResource("/templates/template-properties.json").getFile());
        Map<String, String> properties = new HashMap<>();
        properties.put("prop1", "value1");
        properties.put("prop2", "value2");
        Query decoratedQuery = new TemplateQueryDecorator(template, new StaticMapQueryDecorator(properties, new DummyElasticSearchQuery()));

        topic.withNumber(42);
        decoratedQuery.query(topic);
        String actual = decoratedQuery.getJSONQuery();
        String expected = "{\"match\":{\"value1\": \"value2\"}}";
        assertEquals(expected, actual);
    }

}
