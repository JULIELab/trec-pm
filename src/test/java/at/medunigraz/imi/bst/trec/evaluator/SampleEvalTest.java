package at.medunigraz.imi.bst.trec.evaluator;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class SampleEvalTest {

    private static final String GOLD = "/gold-standard/test-sample.qrels";
    private static final String RESULTS = "/results/test.trec_results";
    private static final String OUTPUT = "src/test/resources/stats/test.sampleval";

    @Before
    public void setUp() {
        Assume.assumeTrue(SampleEval.scriptExists());
    }

    @Test
    public void evaluate() {
        File goldStandard = new File(getClass().getResource(GOLD).getFile());
        File results = new File(getClass().getResource(RESULTS).getFile());
        File output = new File(OUTPUT);

        SampleEval t = new SampleEval(goldStandard, results, output);
        assertEquals(0.6309, t.getInfNDCG(), 0.00001);
        assertEquals(0.5, t.getInfAP(), 0.00001);
    }
}
