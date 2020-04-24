package at.medunigraz.imi.bst.trec.query.covid;

import at.medunigraz.imi.bst.retrieval.Query;
import at.medunigraz.imi.bst.trec.query.DummyElasticSearchQuery;
import de.julielab.ir.model.CovidTopic;
import org.hamcrest.collection.IsMapContaining;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NarrativeSynonymDecoratorTest {
    private static final String QUERY = "Coronavirus animal model";

    @Test
    public void testExpandTopic() {
        DummyElasticSearchQuery<CovidTopic> dummyQuery = new DummyElasticSearchQuery<>();
        Query<CovidTopic> decorator = new NarrativeSynonymDecorator(dummyQuery);
        CovidTopic topic = new CovidTopic().withNarrative(QUERY);
        topic.setMandatoryBoW(new HashSet<>(Set.of("Coronavirus", "animal", "model")));

        decorator.query(topic);

        Map<String, String> actual = dummyQuery.getTopic().getFlattenedAttributes();
        Assert.assertThat(actual, IsMapContaining.hasEntry("mandatoryBoW", "[model]"));
        Assert.assertThat(actual, IsMapContaining.hasEntry("mandatorySynonymWords", "[[COVID-19, SARS-CoV2, SARS-CoV-2, COVID19, Covid-19, Covid19, 2019-nCoV], [mouse], []]"));
    }

}
