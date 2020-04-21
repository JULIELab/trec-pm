package at.medunigraz.imi.bst.trec.query.covid;

import at.medunigraz.imi.bst.retrieval.Query;
import at.medunigraz.imi.bst.trec.query.DynamicQueryDecorator;
import de.julielab.ir.model.CovidTopic;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * <p>
 * This decorator is applied after {@link WordRemovalQueryDecorator}, so that {@link CovidTopic#getMandatoryBoW()}
 * is populated.
 * </p>
 * <p>
 *     The task of this decorator is to find words in the topic that could help for relevance scoring but are
 *     not strictly required for a document to be relevant. Such words should go to {@link CovidTopic#setOptionalBoW(Set)}.
 * </p>
 */
public class OptionalQueryWordDecorator extends DynamicQueryDecorator<CovidTopic> {

    public OptionalQueryWordDecorator(Query decoratedQuery) {
        super(decoratedQuery);
    }



    @Override
    public CovidTopic expandTopic(CovidTopic topic) {
        Set<String> mandatoryBoW = topic.getMandatoryBoW();
        Set<String> optionalBoW = new HashSet<>();
        Iterator<String> mandatoryWordsIt = mandatoryBoW.iterator();
        while (mandatoryWordsIt.hasNext()) {
            String word =  mandatoryWordsIt.next();
            if (isOptional(word)) {
                optionalBoW.add(word);
                mandatoryWordsIt.remove();
            }
        }
        topic.setOptionalBoW(optionalBoW);
        return topic;
    }

    private boolean isOptional(String word) {
        // TODO if there should be words that fulfull the criteria of the class description, find them here
        return false;
    }


}
