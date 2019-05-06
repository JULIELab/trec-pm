package de.julielab.ir.ltr.features.featuregroups;

import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import de.julielab.ir.ltr.Document;
import de.julielab.ir.ltr.features.FeatureGroup;

public class TopicMatchFeatureGroup extends FeatureGroup {
    public TopicMatchFeatureGroup() {
        super("TOPIC_MATCH");
    }

    @Override
    public Instance pipe(Instance instance) {
        final Token token = (Token) instance.getData();
        final Document document = (Document) instance.getSource();
        return instance;
    }
}
