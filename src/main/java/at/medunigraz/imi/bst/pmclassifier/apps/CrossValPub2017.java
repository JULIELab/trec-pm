package at.medunigraz.imi.bst.pmclassifier.apps;

import at.medunigraz.imi.bst.pmclassifier.DataReadingException;

public class CrossValPub2017 {
    public static void main(String args[]) throws DataReadingException {
        CrossVal.performCrossVal("resources/gs2017DocsJson.zip", "resources/20180622processedGoldStandardTopics.tsv.gz");
    }
}
