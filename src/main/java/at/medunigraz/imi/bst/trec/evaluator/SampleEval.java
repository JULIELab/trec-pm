package at.medunigraz.imi.bst.trec.evaluator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SampleEval extends AbstractEvaluator {

    private static final String SCRIPT = "target/lib/sample_eval.pl";

    /**
     * $ perl sample_eval.pl
     * Usage:  sample_eval.pl [-q] <qrel_file> <trec_file>
     */
    private static final String COMMAND = "perl " + SCRIPT + " -q";

    public SampleEval(File goldStandard, File results) {
        super.goldStandard = goldStandard;
        super.results = results;
        evaluate();
    }

    public static boolean scriptExists() {
        return new File(SCRIPT).isFile();
    }

    public List<String> getFullCommand() {
        List<String> fullCommand = new ArrayList<>();
        fullCommand.add(COMMAND);
        fullCommand.add(goldStandard.getAbsolutePath());
        fullCommand.add(results.getAbsolutePath());
        return fullCommand;
    }

}
