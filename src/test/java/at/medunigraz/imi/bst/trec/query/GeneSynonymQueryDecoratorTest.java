package at.medunigraz.imi.bst.trec.query;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.retrieval.ElasticSearchQuery;
import at.medunigraz.imi.bst.retrieval.Query;
import at.medunigraz.imi.bst.retrieval.SubTemplateQueryDecorator;
import at.medunigraz.imi.bst.trec.model.Topic;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class GeneSynonymQueryDecoratorTest extends QueryDecoratorTest {
    private static final String GENE = "TP53";

    public GeneSynonymQueryDecoratorTest() {
        String template = "/test-templates/match-title-gene-expansion.json";
        this.decoratedQuery = new GeneSynonymQueryDecorator(
                new SubTemplateQueryDecorator<>(template, new ElasticSearchQuery<>(TrecConfig.ELASTIC_BA_INDEX)));
        this.topic = new Topic().withGeneField(GENE);
    }

    @Test
    public void testGetTopic() {
        DummyElasticSearchQuery<Topic> dummyQuery = new DummyElasticSearchQuery<>();
        Query<Topic> decorator = new GeneSynonymQueryDecorator(dummyQuery);

        decorator.query(new Topic().withGeneField(GENE));

        Map<String, String> actual = dummyQuery.getTopic().getFlattenedAttributes();
        Assert.assertThat(actual, Matchers.hasValue("BCC7"));
        Assert.assertThat(actual, Matchers.hasValue("BMFS5"));
        Assert.assertThat(actual, Matchers.hasValue("LFS1"));
        Assert.assertThat(actual, Matchers.hasValue("P53"));
        Assert.assertThat(actual, Matchers.hasValue("TRP53"));
    }
}
