package at.medunigraz.imi.bst.pmclassifier.apps;

import at.medunigraz.imi.bst.pmclassifier.DataReadingException;
import at.medunigraz.imi.bst.pmclassifier.MalletClassifier;
import at.medunigraz.imi.bst.pmclassifier.lucene.LuceneClassifier;

import java.io.IOException;

public class TrainPub2017Lucene {
    public static void main(String args[]) throws ClassNotFoundException, IOException, DataReadingException {
        Train.doTrain(new LuceneClassifier(), "resources/gs2017DocsJson.zip", "resources/20180622processedGoldStandardTopics.tsv.gz", "src/main/resources/models/lucene2017");
    }
}
