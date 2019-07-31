package de.julielab.ir.paramopt;

import de.julielab.java.utilities.ConfigurationUtilities;
import org.apache.commons.configuration2.ConfigurationUtils;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.julielab.ir.ltr.features.FCConstants.ACTIVE_ATTR;
import static de.julielab.ir.ltr.features.FCConstants.FEATURE;
import static de.julielab.ir.ltr.features.FCConstants.NAME_ATTR;
import static de.julielab.java.utilities.ConfigurationUtilities.slash;
import static de.julielab.java.utilities.ConfigurationUtilities.ws;

abstract public class SmacWrapper {
private final static Logger log = LoggerFactory.getLogger(SmacWrapper.class);
    protected abstract double calculateScore(HierarchicalConfiguration<ImmutableNode> config, String instance, int seed);

    public void runConfiguration(String[] args) throws ConfigurationException {
        HierarchicalConfiguration<ImmutableNode> config = parseConfiguration(args);
        String instance = args[0];
        int seed = Integer.valueOf(args[4]);
        double score = calculateScore(config, instance, seed);
        System.out.println("Result for SMAC: SUCCESS, 0, 0, " + score + ", 0");
    }

    protected HierarchicalConfiguration<ImmutableNode> parseConfiguration(String[] args) throws ConfigurationException {
        final HierarchicalConfiguration<ImmutableNode> config = ConfigurationUtilities.createEmptyConfiguration();
        for (int i = 5; i < args.length; i++) {
            String parameter = args[i];
            if (parameter.startsWith("-")) {
                parameter = parameter.substring(1);
                if (i == args.length - 1)
                    throw new IllegalArgumentException("The parameter " + parameter + " has no value specified.");
                String value = args[i + 1];
                if (!parameter.contains("@")) {
                    config.setProperty(parameter.replaceAll("\\.", "/"), value);
                } else {
                    String active = String.valueOf(value.equals("on"));
                    final String[] split = parameter.split("@");
                    String groupPath = split[0].replaceAll("\\.", "/");
                    final String featureGroup = split[1];
                    config.setProperty(groupPath, "");
                    if (split.length == 2) {
                        config.setProperty(ws(groupPath, NAME_ATTR), featureGroup);
                        config.setProperty(ws(groupPath, ACTIVE_ATTR), active);
                    } else {
                        final String feature = split[2];
                        final String featurePath = slash(groupPath, FEATURE);
                        config.setProperty(featurePath, "");
                        config.setProperty(ws(featurePath, NAME_ATTR), feature);
                        config.setProperty(ws(featurePath, ACTIVE_ATTR), active);
                    }
                }
            }
        }
        log.info("Configuration parsed from SMAC input: {}", ConfigurationUtils.toString(config));
        return config;
    }
}
