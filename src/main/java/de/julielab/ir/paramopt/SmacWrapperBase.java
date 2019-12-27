package de.julielab.ir.paramopt;

import de.julielab.java.utilities.ConfigurationUtilities;
import org.apache.commons.configuration2.ConfigurationUtils;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static de.julielab.java.utilities.ConfigurationUtilities.last;
import static de.julielab.java.utilities.ConfigurationUtilities.ws;

abstract public class SmacWrapperBase {
    private final static Logger log = LoggerFactory.getLogger(SmacWrapperBase.class);

    protected abstract double calculateScore(HierarchicalConfiguration<ImmutableNode> config, String instance, int seed);

    public void parseAndRunConfiguration(String[] args) throws ConfigurationException {
        HierarchicalConfiguration<ImmutableNode> config = parseConfiguration(args);
        String instance = args[0];
        String instanceSpecificInfo = args[1];
        int cutoffTime = Integer.valueOf(args[2]);
        int cutoffLength = Integer.valueOf(args[3]);
        int seed = Integer.valueOf(args[4]);
        double score = calculateScore(config, instance, seed);
        System.out.println("Result for SMAC: SUCCESS, 0, 0, " + score + ", 0");
    }

    protected HierarchicalConfiguration<ImmutableNode> parseConfiguration(String[] args) throws ConfigurationException {
        final HierarchicalConfiguration<ImmutableNode> config = ConfigurationUtilities.createEmptyConfiguration();
        for (int i = 5; i < args.length; i++) {
            String parameter = args[i];
            if (i % 2 == 1) {
                parameter = "/" + parameter.substring(1);
                if (i == args.length - 1)
                    throw new IllegalArgumentException("The parameter " + parameter + " has no value specified.");
                String value = args[i + 1];
                if (parameter.contains("@")) {
                    String[] attrSplit = parameter.split("@");
                    String pathUntilAttr = attrSplit[0].replaceAll("\\.", "/");
                    String[] attrAndValue = attrSplit[1].split(":");
                    String attributeName = attrAndValue[0];
                    String valueWithQuotes = attrAndValue[1];
                    String valueWithoutQuotes = valueWithQuotes.substring(1, attrAndValue[1].length() - 1);
                    config.addProperty(pathUntilAttr, value);
                    config.addProperty(ws(last(pathUntilAttr), "@" + attributeName), valueWithoutQuotes);
                } else {
                    config.setProperty(parameter.replaceAll("\\.", "/"), value);
                }
            }
        }
        log.info("Configuration parsed from SMAC input: {}", ConfigurationUtils.toString(config));
        return config;
    }

    protected double harmonicMean(double... values) {
        double denominator = Arrays.stream(values).map(d -> 1 / d).sum();
        return values.length / denominator;
    }
}
