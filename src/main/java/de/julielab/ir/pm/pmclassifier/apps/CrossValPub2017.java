package de.julielab.ir.pm.pmclassifier.apps;

import de.julielab.ir.pm.pmclassifier.DataReadingException;
import de.julielab.ir.pm.pmclassifier.MalletClassifier;

public class CrossValPub2017 {
    public static void main(String args[]) throws DataReadingException {
        CrossVal.performCrossVal(new MalletClassifier(),"resources/gs2017DocsJson.zip", "resources/20180622processedGoldStandardTopics.tsv.gz");
    }
}
