package de.julielab.ir.pm.pmclassifier.apps;

import de.julielab.ir.pm.pmclassifier.DataReadingException;
import de.julielab.ir.pm.pmclassifier.MalletClassifier;

import java.io.IOException;

public class EvalPub2018On2017 {
    public static void main(String args[]) throws ClassNotFoundException, IOException, DataReadingException {
        Eval.doEval(new MalletClassifier(), "resources/gs2017DocsJson.zip", "resources/20180622processedGoldStandardTopics.tsv.gz", "src/main/resources/models/malletPmClassifier2018.mod.gz");
    }
}
