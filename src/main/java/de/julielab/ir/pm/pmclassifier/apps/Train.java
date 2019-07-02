package de.julielab.ir.pm.pmclassifier.apps;

import de.julielab.ir.pm.pmclassifier.*;
import de.julielab.ir.pm.pmclassifier.lucene.LuceneClassifier;
import cc.mallet.classify.Classifier;
import cc.mallet.types.Alphabet;
import cc.mallet.types.FeatureVector;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Train {
    private static final Logger LOG = LogManager.getLogger();

    public static void doTrain(PMClassifier classifier, String jsongsdocs, String annotatedGs, String modeloutputfile) throws DataReadingException, IOException, ClassNotFoundException {
        LOG.info("Reading documents");
        Map<String, Document> documents = DataReader.readDocuments(new File(jsongsdocs));
        DataReader.addPMLabels(new File(annotatedGs), documents);
        writeLabelDistribution(documents);
        InstancePreparator ip = InstancePreparator.getInstance();
        ip.trainTfIdf(documents.values());
        classifier.setInstancePreparator(ip);
       // InstanceList instances = ip.createClassificationInstances(documents);
        LOG.info("Training the model");
        //writeFeaturestoSVMLight(instances);
        classifier.train(documents);
        String filename = modeloutputfile;
        LOG.info("Storing the model to " + filename);
        classifier.writeClassifier(new File(filename));

        LOG.info("Loading model and doing reclassification of training data to check that training was in order.");
        Eval.doEval(classifier, documents, filename);
    }

    private static void writeLabelDistribution(Map<String,Document> documents) {
        final long pm = documents.values().stream().filter(d -> d.getPmLabel().equals("PM")).count();
        final long notpm = documents.values().stream().filter(d -> d.getPmLabel().equals("Not PM")).count();
        long all = pm + notpm;
        LOG.info("Of all {} documents, {} ({}%) are PM and {} ({}%) are Not PM", all, pm, (double)pm/all*100, notpm, (double)notpm/all*100);
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
