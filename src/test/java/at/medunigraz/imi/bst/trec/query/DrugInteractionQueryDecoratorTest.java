package at.medunigraz.imi.bst.trec.query;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.retrieval.ElasticSearchQuery;
import at.medunigraz.imi.bst.retrieval.Query;
import at.medunigraz.imi.bst.retrieval.SubTemplateQueryDecorator;
import at.medunigraz.imi.bst.trec.model.Topic;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.util.Map;

/**
 * This test is ignored because it queries DGIdb which has varying results over time. At the time of writing
 * this comment, most of the tested drugs won't be returned because their score is now too low than the set
 * threshold.
 */
@Ignore
public class DrugInteractionQueryDecoratorTest extends QueryDecoratorTest {
    private static final String GENE = "BRAF";

    private final String template = "/templates/match-title-drug-interaction.json";

    public DrugInteractionQueryDecoratorTest() {
        this.decoratedQuery = new DrugInteractionQueryDecorator(
                new SubTemplateQueryDecorator(template, new ElasticSearchQuery(TrecConfig.ELASTIC_BA_INDEX)));
        this.topic = new Topic().withGeneField(GENE);
    }

    @Test
    public void testGetTopic() {
        DummyElasticSearchQuery dummyQuery = new DummyElasticSearchQuery();
        Query decorator = new DrugInteractionQueryDecorator(dummyQuery);

        decorator.query(new Topic().withGeneField(GENE));

        Map<String, String> actual = dummyQuery.getTopic().getAttributes();
        Assert.assertThat(actual, Matchers.hasValue("selumetinib"));
        Assert.assertThat(actual, Matchers.hasValue("dabrafenib"));
        Assert.assertThat(actual, Matchers.hasValue("bevacizumab"));
        Assert.assertThat(actual, Matchers.hasValue("obatoclax"));
    }
}
