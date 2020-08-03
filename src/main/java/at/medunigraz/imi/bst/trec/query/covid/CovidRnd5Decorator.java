package at.medunigraz.imi.bst.trec.query.covid;

import at.medunigraz.imi.bst.retrieval.Query;
import at.medunigraz.imi.bst.retrieval.QueryDecorator;
import at.medunigraz.imi.bst.trec.model.Result;
import de.julielab.ir.model.CovidTopic;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Checking in how far we can reproduce experiments from https://github.com/castorini/anserini/blob/master/docs/experiments-covid.md
 */
public class CovidRnd5Decorator extends QueryDecorator<CovidTopic> {

    private static final String COVID_NAMES = "(COVID-?19|2019-?nCov|SARS-?COV-?2?|corona ?virus)";
    private static final Pattern COVID_PATTERN = Pattern.compile(".*" + COVID_NAMES + ".*", Pattern.CASE_INSENSITIVE);

    public CovidRnd5Decorator(Query<CovidTopic> decoratedQuery) {
        super(decoratedQuery);
    }
    public boolean isCovidQuery(String q) {
        return COVID_PATTERN.matcher(q).matches();
    }
    @Override
    public List<Result> query(CovidTopic topic) {
        String queryText = topic.getQuery();

        // If query doesn't contain variants of COVID-19, then just pass through with BoW generator.
        if (isCovidQuery(queryText)) {
            // Remove the variant of covid-19 itself.
            queryText = queryText.replaceAll("(?i)" + COVID_NAMES, " ");
            topic.setQuery(queryText);

            topic.setMandatorySynonymWords(List.of(Set.of("COVID-19", "2019-nCov", "SARS-CoV-2")));

        }
        return decoratedQuery.query(topic);
    }
}
