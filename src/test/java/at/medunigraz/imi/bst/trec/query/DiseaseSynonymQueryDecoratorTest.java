package at.medunigraz.imi.bst.trec.query;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.lexigram.Lexigram;
import at.medunigraz.imi.bst.retrieval.ElasticSearchQuery;
import at.medunigraz.imi.bst.retrieval.Query;
import at.medunigraz.imi.bst.retrieval.SubTemplateQueryDecorator;
import at.medunigraz.imi.bst.trec.model.Topic;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Map;

public class DiseaseSynonymQueryDecoratorTest extends QueryDecoratorTest {
	private static final String DISEASE = "Cholangiocarcinoma";

	private final File template = new File(getClass().getResource("/templates/match-title-expansion.json").getFile());

	@Before
	public void setUp() {
		Assume.assumeTrue(Lexigram.isAPIKeyLoaded());
	}

	public DiseaseSynonymQueryDecoratorTest() {
		this.decoratedQuery = new DiseaseSynonymQueryDecorator(
				new SubTemplateQueryDecorator(template, new ElasticSearchQuery(TrecConfig.ELASTIC_BA_INDEX)));
		this.topic = new Topic().withDisease(DISEASE);
	}

	@Test
	public void testGetTopic() {
		DummyElasticSearchQuery dummyQuery = new DummyElasticSearchQuery();
		Query decorator = new DiseaseSynonymQueryDecorator(dummyQuery);

		decorator.query(new Topic().withDisease(DISEASE));

		Map<String, String> actual = dummyQuery.getTopic().getAttributes();
		Assert.assertThat(actual, Matchers.hasEntry("diseaseSynonyms0", "cholangiocellular carcinoma"));
		Assert.assertThat(actual, Matchers.hasEntry("diseaseSynonyms1", "bile duct carcinoma"));
	}
}
