package at.medunigraz.imi.bst.trec.evaluator;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SampleEval extends AbstractEvaluator {

    private static final String SCRIPT = "target/lib/sample_eval.pl";

    /**
     * $ perl sample_eval.pl
     * Usage:  sample_eval.pl [-q] <qrel_file> <trec_file>
     */
    private static final List<String> COMMAND = Arrays.asList("perl", SCRIPT, "-q");

    public SampleEval(File goldStandard, File results, File output) {
        super(goldStandard, results, output);
        evaluate();
    }

    public static boolean scriptExists() {
        return new File(SCRIPT).isFile();
    }

    public List<String> getFullCommand() {
        List<String> fullCommand = new ArrayList<>(COMMAND);
        fullCommand.add(goldStandard.getAbsolutePath());
        fullCommand.add(results.getAbsolutePath());
        return fullCommand;
    }

}
