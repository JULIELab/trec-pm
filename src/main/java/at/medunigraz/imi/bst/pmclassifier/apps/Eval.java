package at.medunigraz.imi.bst.pmclassifier.apps;

import at.medunigraz.imi.bst.pmclassifier.DataReader;
import at.medunigraz.imi.bst.pmclassifier.DataReadingException;
import at.medunigraz.imi.bst.pmclassifier.Document;
import at.medunigraz.imi.bst.pmclassifier.MalletClassifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class Eval {
    private static final Logger LOG = LogManager.getLogger();

    public static void doEval(String jsongsdocs, String annotatedGs, String modelFile) throws DataReadingException, IOException, ClassNotFoundException {
        LOG.info("Reading documents");
        Map<String, Document> documents = DataReader.readDocuments(new File(jsongsdocs));
        DataReader.addPMLabels(new File(annotatedGs), documents);
        doEval(documents, modelFile);
    }

    public static void doEval(Map<String, Document> documents, String modelfile) throws IOException, ClassNotFoundException {
        LOG.info("Loading model and doing classification on given data.");
        MalletClassifier classifier = new MalletClassifier();
        classifier.readClassifier(modelfile);
        int corr = 0;
        for (Document doc : documents.values()) {
            String label = classifier.predict(doc);
            if (label.equalsIgnoreCase(doc.getPmLabel()))
                ++corr;
        }
        LOG.info("Total: " + documents.size());
        LOG.info("Correct: " + corr);
        LOG.info("That is " + (corr / (double) documents.size()) * 100 + "%");
    }
}
