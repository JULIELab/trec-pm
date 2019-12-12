package de.julielab.ir.paramopt;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.trec.experiment.Experiment;
import at.medunigraz.imi.bst.trec.experiment.TrecPmRetrieval;
import at.medunigraz.imi.bst.trec.experiment.registry.LiteratureArticlesRetrievalRegistry;
import at.medunigraz.imi.bst.trec.model.Metrics;
import at.medunigraz.imi.bst.trec.model.Topic;
import de.julielab.ir.goldstandards.GoldStandard;
import de.julielab.ir.goldstandards.TrecPMGoldStandardFactory;
import de.julielab.ir.ltr.features.FeatureControlCenter;
import de.julielab.ir.model.QueryDescription;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.ImmutableNode;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SigirPM19SmacWrapper extends SmacWrapperBase {

    private static final GoldStandard<Topic> GOLD_STANDARD = TrecPMGoldStandardFactory.pubmedOfficial2019();

    public static void main(String args[]) throws ConfigurationException {
        SigirPM19SmacWrapper sigirSmacWrapper = new SigirPM19SmacWrapper();
        sigirSmacWrapper.parseAndRunConfiguration(args);
    }

    @Override
    protected double calculateScore(HierarchicalConfiguration<ImmutableNode> config, String instance, int seed) {
        FeatureControlCenter.initialize(config);
        //TrecPmRetrieval trecPmRetrieval = LiteratureArticlesRetrievalRegistry.jlpmcommon2Generic(TrecConfig.SIZE);
        TrecPmRetrieval trecPmRetrieval = LiteratureArticlesRetrievalRegistry.jlpmcommon2paramopt(TrecConfig.SIZE);
        Experiment<QueryDescription> exp = new Experiment<>(GOLD_STANDARD, trecPmRetrieval);
        Metrics metrics = exp.run();
        logMetrics(config, metrics);
        // SMAC always minimizes the objective
        return -1 * harmonicMean(metrics.getInfNDCG(), metrics.getRPrec(), metrics.getP10());
    }

    private void logMetrics(HierarchicalConfiguration<ImmutableNode> config, Metrics metrics) {
        File dir = new File("smac-metrics-logging");
        File logfile = new File(dir, "matchallnegoptimization.tsv");
        if (!dir.exists())
            dir.mkdirs();
        boolean logFileExists = logfile.exists();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(logfile, UTF_8, true))) {
            if (!logFileExists) {
                // Write header
                Iterator<String> keys = config.getKeys();
                bw.write(String.join("\t", (Iterable<String>) () -> keys));
                bw.write("\t");
                bw.write(String.join("\t", "infNDCG", "R-prec", "P@10"));
                bw.newLine();
            }
            Iterator<String> keys = config.getKeys();
            List<String> values = new ArrayList<>();
            while (keys.hasNext()) {
                String key = keys.next();
                values.add(config.getString(key));
            }
            bw.write(String.join("\t", values));
            bw.write("\t");
            bw.write(String.join("\t", String.valueOf(metrics.getInfNDCG()), String.valueOf(metrics.getRPrec()), String.valueOf(metrics.getP10())));
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}