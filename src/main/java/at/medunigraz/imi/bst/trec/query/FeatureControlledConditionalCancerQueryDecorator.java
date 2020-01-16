package at.medunigraz.imi.bst.trec.query;

import at.medunigraz.imi.bst.retrieval.Query;
import at.medunigraz.imi.bst.trec.model.Topic;
import de.julielab.ir.ltr.features.FeatureControlCenter;
import de.julielab.ir.ltr.features.PMFCConstants;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

import java.util.Map;
import java.util.Set;

import static de.julielab.ir.ltr.features.PMFCConstants.*;
import static de.julielab.java.utilities.ConfigurationUtilities.slash;

public class FeatureControlledConditionalCancerQueryDecorator extends FileBasedQueryDecorator {

    private static final String CANCER ="/synonyms/cancer.txt";

    public FeatureControlledConditionalCancerQueryDecorator(Query decoratedQuery) {
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
                HierarchicalConfiguration<ImmutableNode> config = FeatureControlCenter.getInstance().getFeatureConfiguration();
                String cancerKeywords = FeatureControlCenter.getKeywordStringFromFeatureConfiguration(config, slash(RETRIEVALPARAMETERS, KEYWORDS, PMFCConstants.CANCER));
                topic.withCancerBooster(cancerKeywords);

                String chemoKeywords = FeatureControlCenter.getKeywordStringFromFeatureConfiguration(config, slash(RETRIEVALPARAMETERS, KEYWORDS, CHEMOTHERAPY));
                topic.withChemotherapyBooster(chemoKeywords);
            }
        }

        return topic;
    }


}
