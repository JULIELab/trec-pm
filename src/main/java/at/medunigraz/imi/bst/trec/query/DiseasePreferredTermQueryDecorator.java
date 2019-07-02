package at.medunigraz.imi.bst.trec.query;

import at.medunigraz.imi.bst.lexigram.Lexigram;
import at.medunigraz.imi.bst.retrieval.Query;
import at.medunigraz.imi.bst.trec.model.Topic;

public class DiseasePreferredTermQueryDecorator extends DynamicQueryDecorator {

    public DiseasePreferredTermQueryDecorator(Query decoratedQuery) {
        super(decoratedQuery);
    }

    @Override
    public Topic expandTopic(Topic topic) {
        String disease = topic.getDisease();
        String preferredTerm = Lexigram.getPreferredTerm(disease);
        return topic.withDiseasePreferredTerm(preferredTerm);
    }

}
