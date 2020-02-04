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

public class StaticMapQueryDecoratorTest extends QueryDecoratorTest {
	
	private static final String KEYWORD = "cancer";

	private final String template = "/test-templates/match-keyword.json";

	private static Map<String, String> keymap = new HashMap<>();

	static {
		keymap.put("keyword", KEYWORD);
	}

	public StaticMapQueryDecoratorTest() {
		this.decoratedQuery = new TemplateQueryDecorator(template,
				new StaticMapQueryDecorator(keymap, new ElasticSearchQuery(TrecConfig.ELASTIC_BA_INDEX)));
		this.topic = new Topic();
	}

	@Test
	public void testGetJSONQuery() {
		Query decoratedQuery = new TemplateQueryDecorator(template,
				new StaticMapQueryDecorator(keymap, new DummyElasticSearchQuery()));
		
		decoratedQuery.query(topic);
		String actual = decoratedQuery.getJSONQuery();
		String expected = String.format("{\"match\":{\"title\":\"%s\"}}", KEYWORD);
		assertEquals(expected, actual);
		
		decoratedQuery.query(topic);
		actual = decoratedQuery.getJSONQuery();
		expected = String.format("{\"match\":{\"title\":\"%s\"}}", KEYWORD);
		assertEquals(expected, actual);
	}

}
