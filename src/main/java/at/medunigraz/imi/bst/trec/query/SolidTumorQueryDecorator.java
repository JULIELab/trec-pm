package at.medunigraz.imi.bst.trec.query;

import at.medunigraz.imi.bst.retrieval.Query;
import at.medunigraz.imi.bst.trec.model.Topic;

import java.util.ArrayList;
import java.util.List;

public class SolidTumorQueryDecorator extends DynamicQueryDecorator {

    private static final List<String> NON_SOLID_TUMOR = new ArrayList<>();
    static {
        NON_SOLID_TUMOR.add("lymphoma");
        NON_SOLID_TUMOR.add("leukemia");
    }

    public SolidTumorQueryDecorator(Query decoratedQuery) {
        super(decoratedQuery);
    }

    @Override
    public Topic expandTopic(Topic topic) {
        String disease = topic.getDisease();

        for (String s : NON_SOLID_TUMOR) {
            // If there's any matching of non-solid, then it's non-solid and we keep the topic as is.
            if (disease.contains(s)) {
                return topic;
            }
        }

        return topic.withDiseaseHypernym("solid");
    }

}
