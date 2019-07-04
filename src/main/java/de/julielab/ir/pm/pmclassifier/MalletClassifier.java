package de.julielab.ir.pm.pmclassifier;

import cc.mallet.classify.Classification;
import cc.mallet.classify.Classifier;
import cc.mallet.classify.MaxEntTrainer;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.Label;
import de.julielab.java.utilities.FileUtilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Map;

public class MalletClassifier implements Serializable, PMClassifier {

    private static final long serialVersionUID = 2018_08_02L;

    private static final Logger LOG = LogManager.getLogger();
    private Classifier classifier;
    private InstancePreparator instancePreparator;

    public InstancePreparator getInstancePreparator() {
        return instancePreparator;
    }

    public void setInstancePreparator(InstancePreparator instancePreparator) {
        this.instancePreparator = instancePreparator;
    }

    public void train(File documentJsonZip, File gsTable) throws DataReadingException {
        InstanceList instances = instancePreparator.getInstancesForGoldData(documentJsonZip, gsTable);
        train(instances);
    }

    public void train( Map<String, Document> train) {
        final InstanceList instances = instancePreparator.createClassificationInstances(train);
        train(instances);
    }

    public void train(InstanceList instances) {
        MaxEntTrainer maxEntTrainer = new MaxEntTrainer();
//        final FeatureSelector selector = new FeatureSelector(new InfoGain.Factory(), 40000);
//        final FeatureSelectingClassifierTrainer trainer = new FeatureSelectingClassifierTrainer(maxEntTrainer, selector);
        classifier = maxEntTrainer.train(instances);
        //classifier = trainer.train(instances);
    }


    public String predict(Document document) {
        Instance instance = classifier.getInstancePipe().instanceFrom(new Instance(document, "unknown", document.getId(), ""));
        Classification classification = classifier.classify(instance);
        Label bestLabel = classification.getLabeling().getBestLabel();
        //classification.getLabeling().value(classifier.getInstancePipe().getTargetAlphabet().lookupIndex(""))
        return (String) bestLabel.getEntry();
    }

    public double predictProbabiltyForPM(Document document) {
        Instance instance = classifier.getInstancePipe().instanceFrom(new Instance(document, "unknown", document.getId(), ""));
        Classification classification = classifier.classify(instance);
        final Label pmlabel = classification.getLabeling().getLabelAlphabet().lookupLabel("PM");
        return classification.getLabelVector().value(pmlabel);
    }

    public Classifier getClassifier() {
        return classifier;
    }

    public void writeClassifier(File destination) throws IOException {
        LOG.info("Writing classifier to " + destination.getAbsolutePath());
        try (BufferedOutputStream os = FileUtilities.getOutputStreamToFile(destination); ObjectOutputStream oos = new ObjectOutputStream(os)) {
            oos.writeObject(classifier);
            oos.writeObject(instancePreparator);
        }
    }



    public void readClassifier(File source) throws IOException, ClassNotFoundException {
        LOG.info("Reading classifier from " + source.getAbsolutePath());
        try (BufferedInputStream is = FileUtilities.getInputStreamFromFile(source)) {
            readClassifier(is);
        }
    }

    public void readClassifier(String source) throws IOException, ClassNotFoundException {
        InputStream is = FileUtilities.findResource(source);
        if (is == null)
            throw new IllegalArgumentException("Could not find model at source as a file, URI or classpath resource");
        readClassifier(is);
    }

    public void readClassifier(InputStream is) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(is)) {
            classifier = (Classifier) ois.readObject();
            classifier.getInstancePipe().getDataAlphabet().stopGrowth();
            instancePreparator = (InstancePreparator) ois.readObject();
            instancePreparator.setAsInstance();
        }
    }
}
