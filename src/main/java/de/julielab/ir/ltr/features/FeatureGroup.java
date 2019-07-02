package de.julielab.ir.ltr.features;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.Token;

import java.util.Set;

public abstract class FeatureGroup extends Pipe {
    private String name;
    /**
     * This is the set of features contained in this feature group.
     */
    protected Set<Feature> features;

    public String getName() {
        return name;
    }

    public FeatureGroup(String name) {
        this.name = name;
    }

    @Override
    public abstract Instance pipe(Instance instance);

    /**
     * <p>For feature groups with toggleable feature, this method will apply all active features.</p>
     * <p>This is a helper method, its usage is optional for each feature group. If used, the feature group
     * should first create all its feature functions in the form of {@link Feature} objects and set them to the
     * {@link #features} field. Calling this method will apply the features which have been set to be active
     * in the configuration which is checked via a call to {@link FeatureControlCenter#filterActive(FeatureGroup, Feature)}.</p>
     * @param featureToken The token to add feature values to.
     */
    protected void addFeatures(Token featureToken) {
        features.stream().filter(f -> FeatureControlCenter.getInstance().filterActive(this, f)).forEach(f -> f.assignFeature(featureToken));
    }
}
