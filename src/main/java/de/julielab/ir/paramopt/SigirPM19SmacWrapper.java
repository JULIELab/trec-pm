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

public class SigirPM19SmacWrapper extends SmacWrapperBase {

    private static final GoldStandard<Topic> GOLD_STANDARD = TrecPMGoldStandardFactory.pubmedOfficial2019();

    public static void main(String args[]) throws ConfigurationException {
        SigirPM19SmacWrapper sigirSmacWrapper = new SigirPM19SmacWrapper();
        sigirSmacWrapper.parseAndRunConfiguration(args);
    }

    @Override
    protected double calculateScore(HierarchicalConfiguration<ImmutableNode> config, String instance, int seed) {
        FeatureControlCenter.initialize(config);
        TrecPmRetrieval trecPmRetrieval = LiteratureArticlesRetrievalRegistry.jlpmcommon2Generic(TrecConfig.SIZE);
        Experiment<QueryDescription> exp = new Experiment<>(GOLD_STANDARD, trecPmRetrieval);
        Metrics metrics = exp.run();
        // SMAC always minimizes the objective
        return -1*harmonicMean(metrics.getInfNDCG(), metrics.getRPrec(), metrics.getP10());
    }
}
