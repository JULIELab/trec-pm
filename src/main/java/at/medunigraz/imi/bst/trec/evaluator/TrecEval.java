package at.medunigraz.imi.bst.trec.evaluator;

import at.medunigraz.imi.bst.config.TrecConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TrecEval extends AbstractEvaluator {

    private static final String TREC_EVAL_SCRIPT = "target/lib/trec_eval";

    /**
     * -m all_trec -q -c -M1000
     */
    private List<String> command;

    public TrecEval(File goldStandard, File results, File output) {
        this(goldStandard, results, output, TrecConfig.SIZE, true);
    }

    public TrecEval(File goldStandard, File results, File output, int k, boolean calculateWithMissingResults) {
        super(goldStandard, results, output);
        command = Arrays.asList(TREC_EVAL_SCRIPT, "-m", "all_trec", "-q", "-M", String.valueOf(k));
        if (calculateWithMissingResults) {
            command = new ArrayList<>(command);
            command.add("-c");
        }
        evaluate();
    }

    public List<String> getFullCommand() {
        List<String> fullCommand = new ArrayList<>(command);
        fullCommand.add(goldStandard.getAbsolutePath());
        fullCommand.add(results.getAbsolutePath());
        return fullCommand;
    }

    public static boolean scriptExists() {
        return new File(TREC_EVAL_SCRIPT).isFile();
    }
}
