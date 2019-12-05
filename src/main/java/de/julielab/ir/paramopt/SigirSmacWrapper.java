package de.julielab.ir.paramopt;

import de.julielab.ir.ltr.features.FeatureControlCenter;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.ImmutableNode;

public class SigirSmacWrapper extends SmacWrapperBase {
    public static void main(String args[]) throws ConfigurationException {
        SigirSmacWrapper sigirSmacWrapper = new SigirSmacWrapper();
        sigirSmacWrapper.parseAndRunConfiguration(args);
    }

    @Override
    protected double calculateScore(HierarchicalConfiguration<ImmutableNode> config, String instance, int seed) {
        FeatureControlCenter.initialize(config);
        return 0;
    }
}
