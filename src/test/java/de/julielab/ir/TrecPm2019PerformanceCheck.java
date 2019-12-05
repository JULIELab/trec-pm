package de.julielab.ir;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.lexigram.Lexigram;
import at.medunigraz.imi.bst.trec.experiment.Experiment;
import at.medunigraz.imi.bst.trec.experiment.TrecPmRetrieval;
import at.medunigraz.imi.bst.trec.experiment.registry.LiteratureArticlesRetrievalRegistry;
import at.medunigraz.imi.bst.trec.model.Metrics;
import at.medunigraz.imi.bst.trec.model.Topic;
import at.medunigraz.imi.bst.trec.utils.ConnectionUtils;
import de.julielab.ir.goldstandards.TrecPMGoldStandardFactory;
import de.julielab.ir.goldstandards.TrecQrelGoldStandard;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * <p>
 * This test relies on the actual TREC PM 2019 index being available as well as the official TREC PM 19 sample qrels.
 * t then checks that the official results
 * are still being generated. Being an automated test, this should make sure we don't break anything without
 * noticing.
 * </p>
 */
public class TrecPm2019PerformanceCheck {

    private static final TrecQrelGoldStandard<Topic> GOLD_STANDARD = TrecPMGoldStandardFactory.pubmedOfficial2019();

    @Before
    public void SetUp() {
        Assume.assumeTrue(ConnectionUtils.checkElasticOpenPort());
    }

    @Test
    public void testJlPmCommon2() {
        TrecPmRetrieval trecPmRetrieval = LiteratureArticlesRetrievalRegistry.jlpmcommon2(1000);
        Experiment exp = new Experiment(GOLD_STANDARD, trecPmRetrieval);
        Metrics metrics = exp.run();
        assertEquals("jlpmcommon2 infNGDC result is wrong", 0.5783, metrics.getInfNDCG(), 0.0001);
        assertEquals("jlpmcommon2 R-prec result is wrong", 0.3572, metrics.getRPrec(), 0.0001);
        assertEquals("jlpmcommon2 P@10 result is wrong", 0.6525, metrics.getP10(), 0.0001);
    }
}
