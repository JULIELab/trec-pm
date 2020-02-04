package de.julielab.ir.paramopt;

import de.julielab.java.utilities.ConfigurationUtilities;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.ImmutableNode;

import java.io.File;

public class ConfigWriterSmacWrapper extends SmacWrapperBase {

    public static void main(String args[]) throws ConfigurationException {
        final ConfigWriterSmacWrapper wrapper = new ConfigWriterSmacWrapper();

//        final HierarchicalConfiguration<ImmutableNode> c = ConfigurationUtilities.createEmptyConfiguration();
//        c.addProperty("/ltrfeatures/featuregroups/featuregroup", "muh");
//        c.addProperty("/ltrfeatures/featuregroups/featuregroup", "muh");
//        c.addProperty("/ltrfeatures/featuregroups/featuregroup[last()]/hallo", "muh");
//        c.addProperty("/ltrfeatures/featuregroups/featuregroup", "muh");
//        ConfigurationUtilities.writeConfiguration(c,new File("testconf.xml") );
        final HierarchicalConfiguration<ImmutableNode> config = wrapper.parseConfiguration(args);
        ConfigurationUtilities.writeConfiguration(config, new File("smacPassedConfiguration.xml"));
    }

    @Override
    protected String calculateScore(HierarchicalConfiguration<ImmutableNode> config, String[] metricsToReturn, String instance, int seed) {
        throw new IllegalArgumentException("We want to stop here and only check if the configuration was right.");
    }
}
