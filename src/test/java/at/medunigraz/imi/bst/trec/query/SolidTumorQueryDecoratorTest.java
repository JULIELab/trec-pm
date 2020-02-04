package at.medunigraz.imi.bst.trec.query;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.retrieval.ElasticSearchQuery;
import at.medunigraz.imi.bst.retrieval.Query;
import at.medunigraz.imi.bst.retrieval.SubTemplateQueryDecorator;
import at.medunigraz.imi.bst.trec.model.Topic;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.Map;

import static org.hamcrest.Matchers.not;

public class SolidTumorQueryDecoratorTest extends QueryDecoratorTest {
	private static final String SOLID_DISEASE = "Cholangiocarcinoma";
	private static final String NON_SOLID_DISEASE = "acute myeloid leukemia";

	private final String template ="/test-templates/match-title-custom.json";

	public SolidTumorQueryDecoratorTest() {
		this.decoratedQuery = new SolidTumorQueryDecorator(
				new SubTemplateQueryDecorator(template, new ElasticSearchQuery(TrecConfig.ELASTIC_BA_INDEX)));
		this.topic = new Topic().withDisease(SOLID_DISEASE);
	}

	@Test
	public void testGetTopic() {
		DummyElasticSearchQuery dummyQuery = new DummyElasticSearchQuery();
		Query decorator = new SolidTumorQueryDecorator(dummyQuery);

		decorator.query(new Topic().withDisease(SOLID_DISEASE));
		Map<String, String> actual = dummyQuery.getTopic().getAttributes();
		Assert.assertThat(actual, Matchers.hasEntry("customDiseaseExpansions0", "solid"));

		decorator.query(new Topic().withDisease(NON_SOLID_DISEASE));
		actual = dummyQuery.getTopic().getAttributes();
		Assert.assertThat(actual, not(Matchers.hasEntry("customDiseaseExpansions0", "solid")));
	}
}
