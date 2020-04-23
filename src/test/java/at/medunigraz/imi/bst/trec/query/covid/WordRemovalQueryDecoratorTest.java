package at.medunigraz.imi.bst.trec.query.covid;

import at.medunigraz.imi.bst.retrieval.Query;
import at.medunigraz.imi.bst.retrieval.TemplateQueryDecorator;
import at.medunigraz.imi.bst.trec.query.DummyElasticSearchQuery;
import de.julielab.ir.model.CovidTopic;
import org.junit.Test;

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
                new TemplateQueryDecorator<> (template, new DummyElasticSearchQuery<CovidTopic> ()));
        decoratedQuery.query(topic);


        String actual = decoratedQuery.getJSONQuery();
        String expected = String.format("{\"match\":{\"title\":\"%s\"}}", FILTERED_NARRATIVE);
        assertEquals(expected, actual);
    }
}
