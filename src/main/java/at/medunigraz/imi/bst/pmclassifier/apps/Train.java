package at.medunigraz.imi.bst.pmclassifier.apps;

import at.medunigraz.imi.bst.pmclassifier.*;
import cc.mallet.types.Alphabet;
import cc.mallet.types.FeatureVector;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class Train {
    private static final Logger LOG = LogManager.getLogger();

    public static void main(String args[]) throws DataReadingException, IOException, ClassNotFoundException {
        MalletClassifier classifier = new MalletClassifier();

        LOG.info("Reading documents");
        Map<String, Document> documents = DataReader.readDocuments(new File("resources/gs2017DocsJson.zip"));
        DataReader.addPMLabels(new File("resources/20180622processedGoldStandardTopics.tsv.gz"), documents);
        InstancePreparator ip = InstancePreparator.getInstance();
        ip.trainTfIdf(documents.values());
        classifier.setInstancePreparator(ip);
        InstanceList instances = ip.createClassificationInstances(documents);
        LOG.info("Training the model");
        writeFeaturestoSVMLight(instances);
        classifier.train(instances);
        String filename = "src/main/resources/models/malletPmClassifier.mod.gz";
        LOG.info("Storing the model to " + filename);
        classifier.writeClassifier(new File(filename));

        LOG.info("Loading model and doing reclassification of training data to check that training was in order.");
        classifier = new MalletClassifier();
        classifier.readClassifier(filename);
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

    private static void writeFeaturestoSVMLight(InstanceList instances) {
        int no = 0;
        for (Instance inst : instances) {
            FeatureVector fv = (FeatureVector) inst.getData();
            //fv.toSimpFile("features/" + ((Document) inst.getSource()).getId(), no++, true);
            final Document source = (Document) inst.getSource();
            document2SVMLight(fv, "features", source.getPmLabel(), source.getId(), true);
        }
    }

    public static boolean document2SVMLight(FeatureVector fv, String FileName, String label, String docId, boolean printcounts) {
        final double[] values = fv.getValues();
        final int[] indices = fv.getIndices();
        final Alphabet dictionary = fv.getAlphabet();
        StringBuffer sb = new StringBuffer();
        String numericLabel = label.equalsIgnoreCase("PM") ? "1" : "-1";
        sb.append(numericLabel);
        if (values == null) {
            int indicesLength = fv.numLocations();
            for (int i = 0; i < indicesLength; i++) {
                if (dictionary == null)
                    sb.append("[" + i + "]");
                else {
                    sb.append(dictionary.lookupObject(indices[i]).toString());
                }
                sb.append('\n');
            }
        } else {
            int valuesLength = fv.numLocations();
            for (int i = 0; i < valuesLength; i++) {
                int idx = indices == null ? i : indices[i];
                if (dictionary == null)
                    sb.append("[" + i + "]");
                else {
                    sb.append(" " ).append(idx).append(":");
                }

                if (printcounts)
                    sb.append(values[i]);
            }
            sb.append(System.getProperty("line.separator"));
        }
        String str = sb.toString();

        File myfile = new File(FileName);
        try {
            FileWriter out = new FileWriter(myfile, true); // true -> append to the file
            out.write(str);
            out.close();
        } catch (IOException e) {
            System.err.println("Feature Vector exception when trying to print a file");
        }

        return true;
    }
}
