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

public class GeneFamilyQueryDecoratorTest extends QueryDecoratorTest {
    private final String template ="/test-templates/match-title-gene-hypernym.json";

    public GeneFamilyQueryDecoratorTest() {
        this.decoratedQuery = new GeneFamilyQueryDecorator(
                new SubTemplateQueryDecorator(template, new ElasticSearchQuery(TrecConfig.ELASTIC_BA_INDEX)));
        this.topic = new Topic().withGeneField("TP53");
    }

    @Test
    public void testGetTopic() {
        DummyElasticSearchQuery dummyQuery = new DummyElasticSearchQuery();
        Query decorator = new GeneFamilyQueryDecorator(dummyQuery);

        decorator.query(new Topic().withGeneField("TP53"));
        Map<String, String> actual = dummyQuery.getTopic().getAttributes();
        Assert.assertThat(actual, Matchers.hasEntry("geneHypernyms0", "TP"));

        decorator.query(new Topic().withGeneField("CDK6"));
        actual = dummyQuery.getTopic().getAttributes();
        Assert.assertThat(actual, Matchers.hasEntry("geneHypernyms0", "CDK"));

        decorator.query(new Topic().withGeneField("FGFR1"));
        actual = dummyQuery.getTopic().getAttributes();
        Assert.assertThat(actual, Matchers.hasEntry("geneHypernyms0", "FGF"));

        decorator.query(new Topic().withGeneField("EGFR"));
        actual = dummyQuery.getTopic().getAttributes();
        Assert.assertThat(actual, Matchers.hasEntry("geneHypernyms0", "EGF"));

        decorator.query(new Topic().withGeneField("PIK3CA"));
        actual = dummyQuery.getTopic().getAttributes();
        Assert.assertThat(actual, Matchers.hasEntry("geneHypernyms0", "PIK"));

        decorator.query(new Topic().withGeneField("NF2"));
        actual = dummyQuery.getTopic().getAttributes();
        Assert.assertThat(actual, Matchers.hasEntry("geneHypernyms0", "NF"));

        decorator.query(new Topic().withGeneField("CDKN2A"));
        actual = dummyQuery.getTopic().getAttributes();
        Assert.assertThat(actual, Matchers.hasEntry("geneHypernyms0", "CDKN")); // CDK?

        decorator.query(new Topic().withGeneField("EML4"));
        actual = dummyQuery.getTopic().getAttributes();
        Assert.assertThat(actual, Matchers.hasEntry("geneHypernyms0", "EML"));

        // NOT
        decorator.query(new Topic().withGeneField("BRCA"));
        actual = dummyQuery.getTopic().getAttributes();
        Assert.assertThat(actual, not(Matchers.hasEntry("geneHypernyms0", "B")));
    }
}
