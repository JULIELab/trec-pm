package de.julielab.ir.pm.pmclassifier.apps;

import de.julielab.ir.pm.pmclassifier.DataReadingException;
import de.julielab.ir.pm.pmclassifier.MalletClassifier;

import java.io.IOException;

public class EvalPub2017On2018 {
    public static void main(String args[]) throws ClassNotFoundException, IOException, DataReadingException {
        Eval.doEval(new MalletClassifier(), "resources/gs2018DocsJson.zip", "resources/20190111processedGoldStandardPub2018.tsv.gz", "src/main/resources/models/malletPmClassifier2017.mod.gz");
    }
}
