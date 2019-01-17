package at.medunigraz.imi.bst.pmclassifier;

import java.io.File;
import java.util.Map;

public interface PMClassifier {
    void train(File documentJsonZip, File gsTable) throws DataReadingException;

    void train(Map<String, Document> labeledDocuments);

    String predict(Document document);

    double predictProbabiltyForPM(Document document);

    void setInstancePreparator(InstancePreparator instancePreparator);
}
