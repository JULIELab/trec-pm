package at.medunigraz.imi.bst.trec.query;

import at.medunigraz.imi.bst.retrieval.Query;
import at.medunigraz.imi.bst.trec.model.Topic;

import java.io.File;
import java.util.Map;
import java.util.Set;

public class ConditionalCancerQueryDecorator extends FileBasedQueryDecorator {

    private static final String CANCER ="/synonyms/cancer.txt";

    private static final String CANCER_BOOSTER = "cancer carcinoma tumor";
    private static final String CHEMOTHERAPY_BOOSTER = "*mab *nib *cin *one *ate *mus *lin";

    public ConditionalCancerQueryDecorator(Query decoratedQuery) {
        super(CANCER, decoratedQuery);
    }

    @Override
    public Topic expandTopic(Topic topic) {
        String disease = topic.getDisease().toLowerCase();

        for (Map.Entry<String, Set<String>> entry : expansionMap.entrySet()) {
            String concept = entry.getKey();

            // We use the key only to detect cancer and expand a fixed best_fields query,
            // as it has been usual in previous years. We do not create duplicates.
            if (disease.contains(concept) && topic.getCancerBoosters().size() == 0) {
                topic.withCancerBooster(CANCER_BOOSTER);
                topic.withChemotherapyBooster(CHEMOTHERAPY_BOOSTER);    // TODO test if needed
            }
        }

        return topic;
    }
}
