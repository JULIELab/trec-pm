package at.medunigraz.imi.bst.trec.query.covid;

import at.medunigraz.imi.bst.retrieval.Query;
import at.medunigraz.imi.bst.retrieval.QueryDecorator;
import at.medunigraz.imi.bst.trec.model.Result;
import at.medunigraz.imi.bst.trec.model.Topic;
import de.julielab.ir.model.CovidTopic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This decorator is applied to the original CovidTopic. Thus, {@link CovidTopic#getMandatoryBoW()} should be empty.
 */
public class WordRemovalQueryDecorator extends QueryDecorator<CovidTopic> {

    private static final Set<String> DOMAIN_STOPWORDS = new HashSet<>();
    private static final String TOKEN_SEPARATOR = " ";


    static {
        // TODO replace by covid-related stop words
        DOMAIN_STOPWORDS.add("cancer");

    }

    public WordRemovalQueryDecorator(Query decoratedQuery) {
        super(decoratedQuery);
        readStopwords();
    }

    @Override
    public List<Result> query(CovidTopic topic) {
        String narrative = topic.getNarrative();
        Set<String> unfilteredWords = new HashSet<>();
        // TODO filter the narrative
        topic.setMandatoryBoW(unfilteredWords);
        return decoratedQuery.query(topic);
    }

    private void readStopwords() {
        // TODO read stopwords list from file
    }



}
