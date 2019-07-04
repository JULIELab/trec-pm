package de.julielab.ir.ltr.features;

import cc.mallet.types.Instance;

import java.util.function.Consumer;

/**
 * <p>This is a class assigning feature values to MALLET {@link Instance}s. Each instance should contain a MALLET
 * {@link cc.mallet.types.Token} object in its <tt>data</tt> field, obtained by {@link Instance#getData()}.
 * Each feature has a name and some {@link Consumer} that does the
 * actual work of taking an instance and assigning feature values to its Token. All this overhead serves
 * the purpose of having clearly defined and named feature or feature families (one <tt>Feature</tt> instance
 * may add an arbitrary number of actual feature values into the feature vector) which can be (de-)activated
 * via the {@link FeatureControlCenter} for experimental purposes.</p>
 */
public class Feature {
    private String name;
    /**
     * This consumer takes a MALLET token and sets a feature value to it.
     */
    private Consumer<Instance> valueAssigner;

    public Feature(String name, Consumer<Instance> valueAssigner) {
        this.name = name;
        this.valueAssigner = valueAssigner;
    }

    public void assignFeature(Instance inst) {
        valueAssigner.accept(inst);
    }

    public String getName() {
        return name;
    }
}
