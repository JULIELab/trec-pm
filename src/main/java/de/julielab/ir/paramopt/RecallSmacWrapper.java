package de.julielab.ir.paramopt;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

public class RecallSmacWrapper extends SmacWrapperBase {
    @Override
    protected double calculateScore(HierarchicalConfiguration<ImmutableNode> config, String instance, int seed) {
        return 0;
    }
}
