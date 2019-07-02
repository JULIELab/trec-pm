package at.medunigraz.imi.bst.trec.evaluator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TrecEval extends AbstractEvaluator {

    private static final String TREC_EVAL_SCRIPT = "target/lib/trec_eval";

    /**
     * -m all_trec -q -c -M1000
     */
    private String command;

    public TrecEval(File goldStandard, File results) {
        this(goldStandard, results, 1000, true);
    }

    public TrecEval(File goldStandard, File results, int k, boolean calculateWithMissingResults) {
        super.goldStandard = goldStandard;
        super.results = results;
        command = TREC_EVAL_SCRIPT + " -m all_trec -q -M"+k;
        if (calculateWithMissingResults)
            command = command + " -c";
        evaluate();
    }

    public List<String> getFullCommand() {
        List<String> fullCommand = new ArrayList<>();
        fullCommand.add(command);
        fullCommand.add(goldStandard.getAbsolutePath());
        fullCommand.add(results.getAbsolutePath());
        return fullCommand;
    }

    public static boolean scriptExists() {
        return new File(TREC_EVAL_SCRIPT).isFile();
    }
}
