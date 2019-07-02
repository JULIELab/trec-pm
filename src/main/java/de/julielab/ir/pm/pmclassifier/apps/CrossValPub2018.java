package de.julielab.ir.pm.pmclassifier.apps;

import de.julielab.ir.pm.pmclassifier.DataReadingException;
import de.julielab.ir.pm.pmclassifier.MalletClassifier;

public class CrossValPub2018 {
    public static void main(String args[]) throws DataReadingException {
        CrossVal.performCrossVal(new MalletClassifier(), "resources/gs2018DocsJson.zip", "resources/20190111processedGoldStandardPub2018.tsv.gz");
    }
}
