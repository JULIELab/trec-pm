package at.medunigraz.imi.bst.trec.evaluator;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class TrecEvalTest {

    private static final String GOLD = "/gold-standard/test.qrels";
    private static final String RESULTS = "/results/test.trec_results";
    private static final String OUTPUT = "/stats/test.trec_eval";

    @Before
    public void setUp() {
        Assume.assumeTrue(TrecEval.scriptExists());
    }

    @Test
    public void testEvaluate() {
        File goldStandard = new File(getClass().getResource(GOLD).getFile());
        File results = new File(getClass().getResource(RESULTS).getFile());
        File output = new File(getClass().getResource(OUTPUT).getFile());

        TrecEval t = new TrecEval(goldStandard, results, output);
        assertEquals(0.6309, t.getNDCG(), 0.00001);
        assertEquals(0.0, t.getRPrec(), 0.00001);	// TODO Figure out a better test set
        assertEquals(0.5, t.getInfAP(), 0.00001);
        assertEquals(0.1, t.getP10(), 0.00001);
        assertEquals(0.5, t.getF(), 0.00001);
    }
}
