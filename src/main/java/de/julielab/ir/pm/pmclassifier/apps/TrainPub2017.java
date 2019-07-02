package de.julielab.ir.pm.pmclassifier.apps;

import de.julielab.ir.pm.pmclassifier.DataReadingException;
import de.julielab.ir.pm.pmclassifier.MalletClassifier;

import java.io.IOException;

public class TrainPub2017 {
    public static void main(String args[]) throws ClassNotFoundException, IOException, DataReadingException {
        Train.doTrain(new MalletClassifier(), "resources/gs2017DocsJson.zip", "resources/20180622processedGoldStandardTopics.tsv.gz", "src/main/resources/models/malletPmClassifier2017.mod.gz");
    }
}
