package de.julielab.ir.pm.pmclassifier.apps;

import de.julielab.ir.pm.pmclassifier.Document;
import de.julielab.ir.pm.pmclassifier.MalletClassifier;

import java.io.File;
import java.io.IOException;

public class PredictTest {
    public static void main(String args[]) throws IOException, ClassNotFoundException {
        final MalletClassifier mc = new MalletClassifier();
        mc.readClassifier(new File("malletPmClassifier.mod.gz"));
        final Document d = new Document();
        d.setTitle("This is just kaesekuchen.");
        d.setAbstractText("Right.");
        final double predict = mc.predictProbabiltyForPM(d);
        System.out.println(predict);
    }
}
