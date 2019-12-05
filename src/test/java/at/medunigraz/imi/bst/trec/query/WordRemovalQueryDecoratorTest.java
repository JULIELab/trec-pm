package at.medunigraz.imi.bst.trec.query;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.retrieval.ElasticSearchQuery;
import at.medunigraz.imi.bst.retrieval.Query;
import at.medunigraz.imi.bst.retrieval.TemplateQueryDecorator;
import at.medunigraz.imi.bst.trec.model.Topic;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class WordRemovalQueryDecoratorTest extends QueryDecoratorTest {
	private static final String DISEASE = "Thyroid cancer";
	private static final String FILTERED_DISEASE = "Thyroid";

	private final String template ="/templates/match-title.json";

	public WordRemovalQueryDecoratorTest() {
		this.decoratedQuery = new WordRemovalQueryDecorator(
				new TemplateQueryDecorator(template, new ElasticSearchQuery(TrecConfig.ELASTIC_BA_INDEX)));
		this.topic = new Topic().withDisease(DISEASE);
	}

	@Test
	public void testGetJSONQuery() {
		Query decoratedQuery = new WordRemovalQueryDecorator(
				new TemplateQueryDecorator(template, new DummyElasticSearchQuery()));
		decoratedQuery.query(topic);

		String actual = decoratedQuery.getJSONQuery();
		String expected = String.format("{\"match\":{\"title\":\"%s\"}}", FILTERED_DISEASE);
		assertEquals(expected, actual);
	}
}
