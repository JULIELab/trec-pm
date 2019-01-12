package at.medunigraz.imi.bst.pmclassifier.apps;

import at.medunigraz.imi.bst.pmclassifier.DataReadingException;

public class CrossValPub2018 {
    public static void main(String args[]) throws DataReadingException {
        CrossVal.performCrossVal("resources/gs2018DocsJson.zip", "resources/20190111processedGoldStandardPub2018.tsv.gz");
    }
}
