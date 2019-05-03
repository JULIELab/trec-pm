package at.medunigraz.imi.bst.trec.query;

import at.medunigraz.imi.bst.lexigram.Lexigram;
import at.medunigraz.imi.bst.retrieval.Query;
import at.medunigraz.imi.bst.trec.model.Topic;

import java.util.List;

public class DiseaseSynonymQueryDecorator extends DynamicQueryDecorator {

    public DiseaseSynonymQueryDecorator(Query decoratedQuery) {
        super(decoratedQuery);
    }

    @Override
    public Topic expandTopic(Topic topic) {
        String disease = topic.getDisease();
        List<String> synonyms = Lexigram.getSynonymsFromBestConceptMatch(disease);
        for (String synonym : synonyms) {
            topic.withDiseaseSynonym(synonym);
        }
        return topic;
    }

}
