package at.medunigraz.imi.bst.pmclassifier.apps;

import at.medunigraz.imi.bst.pmclassifier.DataReadingException;

import java.io.IOException;

public class TrainPub2017 {
    public static void main(String args[]) throws ClassNotFoundException, IOException, DataReadingException {
        Train.doTrain("resources/gs2017DocsJson.zip", "resources/20180622processedGoldStandardTopics.tsv.gz", "src/main/resources/models/malletPmClassifier2017.mod.gz");
    }
}
