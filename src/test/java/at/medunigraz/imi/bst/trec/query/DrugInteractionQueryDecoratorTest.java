package at.medunigraz.imi.bst.trec.query;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.trec.model.Topic;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.Map;

public class DrugInteractionQueryDecoratorTest extends QueryDecoratorTest {
    private static final String GENE = "BRAF";

    private final File template = new File(getClass().getResource("/templates/match-title-drug-interaction.json").getFile());

    public DrugInteractionQueryDecoratorTest() {
        this.decoratedQuery = new DrugInteractionQueryDecorator(
                new SubTemplateQueryDecorator(template, new ElasticSearchQuery(TrecConfig.ELASTIC_BA_INDEX)));
        this.topic = new Topic().withGene(GENE);
    }

    @Test
    public void testGetTopic() {
        DummyElasticSearchQuery dummyQuery = new DummyElasticSearchQuery();
        Query decorator = new DrugInteractionQueryDecorator(dummyQuery);

        decorator.query(new Topic().withGene(GENE));

        Map<String, String> actual = dummyQuery.getTopic().getAttributes();
        Assert.assertThat(actual, Matchers.hasValue("selumetinib"));
        Assert.assertThat(actual, Matchers.hasValue("dabrafenib"));
        Assert.assertThat(actual, Matchers.hasValue("bevacizumab"));
        Assert.assertThat(actual, Matchers.hasValue("obatoclax"));
    }
}
