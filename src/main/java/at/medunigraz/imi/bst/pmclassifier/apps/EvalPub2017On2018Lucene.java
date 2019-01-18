package at.medunigraz.imi.bst.pmclassifier.apps;

import at.medunigraz.imi.bst.pmclassifier.DataReadingException;
import at.medunigraz.imi.bst.pmclassifier.MalletClassifier;
import at.medunigraz.imi.bst.pmclassifier.lucene.LuceneClassifier;
import cc.mallet.classify.Classifier;

import java.io.IOException;

public class EvalPub2017On2018Lucene {
    public static void main(String args[]) throws ClassNotFoundException, IOException, DataReadingException {
        Eval.doEval(new LuceneClassifier(), "resources/gs2018DocsJson.zip", "resources/20190111processedGoldStandardPub2018.tsv.gz", "src/main/resources/models/lucene2017");
    }
}
