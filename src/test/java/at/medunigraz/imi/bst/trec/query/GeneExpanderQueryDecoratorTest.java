package at.medunigraz.imi.bst.trec.query;

import static org.junit.Assert.assertEquals;

import java.io.File;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.retrieval.ElasticSearchQuery;
import at.medunigraz.imi.bst.retrieval.Query;
import at.medunigraz.imi.bst.retrieval.TemplateQueryDecorator;
import org.junit.Test;

import at.medunigraz.imi.bst.trec.model.Gene;
import at.medunigraz.imi.bst.trec.model.Topic;

public class GeneExpanderQueryDecoratorTest extends QueryDecoratorTest {
	private static final String GENE = "TP53";

	private final File template = new File(getClass().getResource("/templates/match-title-gene.json").getFile());

	private static final Gene.Field[] EXPAND_TO = { Gene.Field.SYMBOL, Gene.Field.SYNONYMS };

	public GeneExpanderQueryDecoratorTest() {
		this.decoratedQuery = new GeneExpanderQueryDecorator(EXPAND_TO,
				new TemplateQueryDecorator(template, new ElasticSearchQuery(TrecConfig.ELASTIC_BA_INDEX)));
		this.topic = new Topic().withGene(GENE);
	}

	@Test
	public void testGetTopic() {
		DummyElasticSearchQuery dummyQuery = new DummyElasticSearchQuery();
		Query decorator = new GeneExpanderQueryDecorator(EXPAND_TO, dummyQuery);

		decorator.query(new Topic().withGene(GENE));
		String actual = dummyQuery.getTopic().getGene();
		String expected = "TRP53 BCC7 BMFS5 LFS1 TP53 P53";
		assertEquals(expected, actual);
	}
}
