package de.julielab.ir.ltr.features.featuregroups;

import de.julielab.ir.ltr.features.FeatureGroup;
import de.julielab.ir.ltr.features.features.FastTextEmbeddingFeatures;

public class DocumentEmbeddingFeatureGroup extends FeatureGroup {
    public static final String GROUP_NAME = "documentEmbeddings";
    public DocumentEmbeddingFeatureGroup() {
        super(GROUP_NAME);
    }

    @Override
    protected void addFeatures() {
       features.add(new FastTextEmbeddingFeatures());
    }
}
