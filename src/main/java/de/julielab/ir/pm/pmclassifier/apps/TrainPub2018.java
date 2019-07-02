package de.julielab.ir.pm.pmclassifier.apps;

import de.julielab.ir.pm.pmclassifier.DataReadingException;
import de.julielab.ir.pm.pmclassifier.MalletClassifier;

import java.io.IOException;

public class TrainPub2018 {
    public static void main(String args[]) throws ClassNotFoundException, IOException, DataReadingException {
        Train.doTrain(new MalletClassifier(),"resources/gs2018DocsJson.zip", "resources/20190111processedGoldStandardPub2018.tsv.gz", "src/main/resources/models/malletPmClassifier2018.mod.gz");
    }
}
