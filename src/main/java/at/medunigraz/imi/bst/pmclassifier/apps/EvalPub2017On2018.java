package at.medunigraz.imi.bst.pmclassifier.apps;

import at.medunigraz.imi.bst.pmclassifier.DataReadingException;
import at.medunigraz.imi.bst.pmclassifier.MalletClassifier;

import java.io.IOException;

public class EvalPub2017On2018 {
    public static void main(String args[]) throws ClassNotFoundException, IOException, DataReadingException {
        Eval.doEval(new MalletClassifier(), "resources/gs2018DocsJson.zip", "resources/20190111processedGoldStandardPub2018.tsv.gz", "src/main/resources/models/malletPmClassifier2017.mod.gz");
    }
}
