package at.medunigraz.imi.bst.trec.query.covid;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.retrieval.ElasticSearchQuery;
import at.medunigraz.imi.bst.retrieval.Query;
import at.medunigraz.imi.bst.retrieval.TemplateQueryDecorator;
import at.medunigraz.imi.bst.trec.model.Result;
import at.medunigraz.imi.bst.trec.model.Topic;
import de.julielab.ir.model.CovidTopic;
import at.medunigraz.imi.bst.trec.query.DummyElasticSearchQuery;
import at.medunigraz.imi.bst.trec.query.QueryDecoratorTest;
import at.medunigraz.imi.bst.trec.query.covid.WordRemovalQueryDecorator;
import de.julielab.ir.model.QueryDescription;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class WordRemovalQueryDecoratorTest {
    private static final String NARRATIVE = "Seeking  information on COVID-19 fatality rates in different countries and " +
            "in different population groups based on gender, blood types, or other factors";
    //private static final String FILTERED_NARRATIVE = "COVID-19 fatality rates countries population groups gender blood factors";
    private static final String FILTERED_NARRATIVE = "[gender, rates, covid-19, groups, fatality, countries, blood, population, factors]";

    private final String template ="/test-templates/match-title-narrative.json";


    @Test
    public void testQuery() {
        CovidTopic topic = new CovidTopic().withNarrative(NARRATIVE);
        Query<CovidTopic> decoratedQuery = new WordRemovalQueryDecorator(
                new TemplateQueryDecorator<CovidTopic> (template, new DummyElasticSearchQuery<CovidTopic> ()));
        decoratedQuery.query(topic);


        String actual = decoratedQuery.getJSONQuery();
        String expected = String.format("{\"match\":{\"title\":\"%s\"}}", FILTERED_NARRATIVE);
        assertEquals(expected, actual);
    }
}
