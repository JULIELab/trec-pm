package at.medunigraz.imi.bst.trec.query;

import at.medunigraz.imi.bst.lexigram.Lexigram;
import at.medunigraz.imi.bst.retrieval.Query;
import at.medunigraz.imi.bst.trec.model.Topic;

import java.util.List;

public class DiseaseHypernymQueryDecorator extends DynamicQueryDecorator {

    public DiseaseHypernymQueryDecorator(Query decoratedQuery) {
        super(decoratedQuery);
    }

    @Override
    public Topic expandTopic(Topic topic) {
        String disease = topic.getDisease();
        List<String> ancestors = Lexigram.getAncestorsFromBestConceptMatch(disease);
        for (String hypernym : ancestors) {
            topic.withDiseaseHypernym(hypernym);
        }
//        System.out.println("LEXI " + topic.getDisease() + " " + topic.getDiseaseHypernyms());
        return topic;
    }

}
