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
    private final File template = new File(getClass().getResource("/templates/match-title-gene-hypernym.json").getFile());

    public GeneFamilyQueryDecoratorTest() {
        this.decoratedQuery = new GeneFamilyQueryDecorator(
                new SubTemplateQueryDecorator(template, new ElasticSearchQuery(TrecConfig.ELASTIC_BA_INDEX)));
        this.topic = new Topic().withGene("TP53");
    }

    @Test
    public void testGetTopic() {
        DummyElasticSearchQuery dummyQuery = new DummyElasticSearchQuery();
        Query decorator = new GeneFamilyQueryDecorator(dummyQuery);

        decorator.query(new Topic().withGene("TP53"));
        Map<String, String> actual = dummyQuery.getTopic().getAttributes();
        Assert.assertThat(actual, Matchers.hasEntry("geneHypernyms0", "TP"));

        decorator.query(new Topic().withGene("CDK6"));
        actual = dummyQuery.getTopic().getAttributes();
        Assert.assertThat(actual, Matchers.hasEntry("geneHypernyms0", "CDK"));

        decorator.query(new Topic().withGene("FGFR1"));
        actual = dummyQuery.getTopic().getAttributes();
        Assert.assertThat(actual, Matchers.hasEntry("geneHypernyms0", "FGF"));

        decorator.query(new Topic().withGene("EGFR"));
        actual = dummyQuery.getTopic().getAttributes();
        Assert.assertThat(actual, Matchers.hasEntry("geneHypernyms0", "EGF"));

        decorator.query(new Topic().withGene("PIK3CA"));
        actual = dummyQuery.getTopic().getAttributes();
        Assert.assertThat(actual, Matchers.hasEntry("geneHypernyms0", "PIK"));

        decorator.query(new Topic().withGene("NF2"));
        actual = dummyQuery.getTopic().getAttributes();
        Assert.assertThat(actual, Matchers.hasEntry("geneHypernyms0", "NF"));

        decorator.query(new Topic().withGene("CDKN2A"));
        actual = dummyQuery.getTopic().getAttributes();
        Assert.assertThat(actual, Matchers.hasEntry("geneHypernyms0", "CDKN")); // CDK?

        decorator.query(new Topic().withGene("EML4"));
        actual = dummyQuery.getTopic().getAttributes();
        Assert.assertThat(actual, Matchers.hasEntry("geneHypernyms0", "EML"));

        // NOT
        decorator.query(new Topic().withGene("BRCA"));
        actual = dummyQuery.getTopic().getAttributes();
        Assert.assertThat(actual, not(Matchers.hasEntry("geneHypernyms0", "B")));
    }
}
