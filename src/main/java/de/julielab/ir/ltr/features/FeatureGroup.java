package de.julielab.ir.ltr.features;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * A feature group is just a MALLET pipe that adds feature values to MALLET instances. In the case of LtR,
 * each instance is a {@link de.julielab.ir.pm.pmclassifier.Document} with a {@link at.medunigraz.imi.bst.retrieval.Query}.
 * </p>
 * <p>Feature groups may contain multiple {@link Feature}s. Individual Features may be similar to traditional machine
 * learning features in the sense they just set a value at some position of the feature vector. However, they might
 * also be more complex like "the bag of words of the document" which would result in high number of feature vector
 * positions getting a value set.</p>
 * <p>These two levels - feature groups and features - are created to have some means of switching feature layers
 * on and of for experiments via the {@link FeatureControlCenter}. I hope those two levels are enough.
 * But possibly even more fine grained sub-features would be
 * desireable at some point, for example to more closely control which words are included in the bag-of-words feature.
 * On the other hand, such an example would greatly increase the number of feature combinations, perhaps making
 * throrough experiments unfeasible anyway.</p>
 */
public abstract class FeatureGroup extends Pipe {
    private final static Logger log = LoggerFactory.getLogger(FeatureGroup.class);
    /**
     * This is the set of features contained in this feature group.
     */
    protected List<Feature> features = new ArrayList<>();
    private String name;

    public FeatureGroup(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public final Instance pipe(Instance instance) {
        log.trace("Piping instance through feature group {}", name);
        if (!isActive())
            return instance;
        if (features.isEmpty())
            addFeatures();
        applyFeatures(instance);
        return instance;
    }

    protected abstract void addFeatures();

    protected void addFeature(String name, FeatureValueAssigner valueAssinger) {
        features.add(new Feature(name, valueAssinger));
    }

    /**
     * <p>For feature groups with toggleable feature, this method will apply all active features.</p>
     * <p>The feature group
     * should first create all its feature functions in the form of {@link Feature} objects and set them to the
     * {@link #features} field. Calling this method will apply the features which have been set to be active
     * in the configuration which is checked via a call to {@link FeatureControlCenter#filterActive(FeatureGroup, Feature)}.</p>
     *
     * @param inst The instance to add feature values to.
     */
    private void applyFeatures(Instance inst) {
        features.stream().filter(f -> FeatureControlCenter.getInstance().filterActive(this, f)).forEach(f -> f.assignFeature(inst));
    }

    private boolean isActive() {
        return FeatureControlCenter.getInstance().filterActive(this);
    }
}
