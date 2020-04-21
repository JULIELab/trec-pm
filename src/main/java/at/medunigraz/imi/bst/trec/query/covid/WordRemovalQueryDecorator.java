package at.medunigraz.imi.bst.trec.query.covid;

import at.medunigraz.imi.bst.retrieval.Query;
import at.medunigraz.imi.bst.retrieval.QueryDecorator;
import at.medunigraz.imi.bst.trec.model.Result;
import at.medunigraz.imi.bst.trec.model.Topic;
import de.julielab.ir.model.CovidTopic;
import de.julielab.ir.nlp.NLPSentence;
import de.julielab.ir.nlp.NLPToken;
import de.julielab.ir.nlp.PosTaggingService;

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
        // replace by covid-related stop words
        DOMAIN_STOPWORDS.add("range");
        DOMAIN_STOPWORDS.add("information");
        DOMAIN_STOPWORDS.add("studies");
        DOMAIN_STOPWORDS.add("study");
        DOMAIN_STOPWORDS.add("assays");
        DOMAIN_STOPWORDS.add("paper");
        DOMAIN_STOPWORDS.add("papers");
        DOMAIN_STOPWORDS.add("number");
        DOMAIN_STOPWORDS.add("proportion");
        DOMAIN_STOPWORDS.add("outcomes");
        DOMAIN_STOPWORDS.add("outcome");
        DOMAIN_STOPWORDS.add("people");
        DOMAIN_STOPWORDS.add("molecules");
        DOMAIN_STOPWORDS.add("patient");
        DOMAIN_STOPWORDS.add("patients");
    }

    public WordRemovalQueryDecorator(Query decoratedQuery) {
        super(decoratedQuery);
        readStopwords();
    }

    @Override
    public List<Result> query(CovidTopic topic) {
        String narrative = topic.getNarrative();
        Set<String> unfilteredWords = new HashSet<>();
        NLPSentence narrativeTokens = PosTaggingService.getInstance().tag(narrative);
        for (NLPToken token : narrativeTokens) {
            if (token.getPosTag().equals("NN"))
                unfilteredWords.add(token.getToken());
        }
        // filter the narrative
        unfilteredWords.removeAll(DOMAIN_STOPWORDS);
        topic.setMandatoryBoW(unfilteredWords);
        return decoratedQuery.query(topic);
    }

    private void readStopwords() {
        // TODO read stopwords list from file
    }



}
