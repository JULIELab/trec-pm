package de.julielab.ir.paramopt;

import de.julielab.java.utilities.ConfigurationUtilities;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.ImmutableNode;

public class SmacWrapper {
    public static void main(String args[]) throws ConfigurationException {
        final HierarchicalConfiguration<ImmutableNode> config = ConfigurationUtilities.createEmptyConfiguration();
        for (int i = 0; i < args.length; i++) {
            String parameter = args[i];
            if (parameter.startsWith("-")) {
                if (i == args.length-1)
                    throw new IllegalArgumentException("The parameter " + parameter + " has no value specified.");
                String value = args[i + 1];
                if (!parameter.startsWith("ltrfeatures")) {
                    config.setProperty(parameter, value);
                } else {

                }
            }
        }
    }
}
