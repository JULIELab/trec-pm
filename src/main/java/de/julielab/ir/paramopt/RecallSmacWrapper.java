package de.julielab.ir.paramopt;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

public class RecallSmacWrapper extends SmacWrapperBase {
    @Override
    protected String calculateScore(HierarchicalConfiguration<ImmutableNode> config, String[] metricsToReturn, String instance, int seed) {
        return null;
    }
}
