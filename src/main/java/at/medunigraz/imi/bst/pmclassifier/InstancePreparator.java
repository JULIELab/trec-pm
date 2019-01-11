package at.medunigraz.imi.bst.pmclassifier;

import at.medunigraz.imi.bst.pmclassifier.featurepipes.*;
import cc.mallet.pipe.*;
import cc.mallet.pipe.tsf.LexiconMembership;
import cc.mallet.pipe.tsf.TrieLexiconMembership;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import com.wcohen.ss.BasicStringWrapper;
import com.wcohen.ss.BasicStringWrapperIterator;
import com.wcohen.ss.TFIDF;
import com.wcohen.ss.api.StringWrapper;
import com.wcohen.ss.api.Token;
import com.wcohen.ss.api.Tokenizer;
import com.wcohen.ss.tokens.BasicSourcedToken;
import com.wcohen.ss.tokens.BasicToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class InstancePreparator implements Serializable {

    private static final long serialVersionUID = 2018_08_02L;

    private static final Logger LOG = LogManager.getLogger();

    private static InstancePreparator service;

    private transient TFIDF tfidf;
    private List<String> tfIdfTrainData;

    private InstancePreparator() {
    }

    public static InstancePreparator getInstance() {
        if (service == null) {
            service = new InstancePreparator();
        }
        return service;
    }

    void setAsInstance() {
        service = this;
    }

    public TFIDF getTfidf() {
        if (tfidf == null) {
            if (tfIdfTrainData == null)
                throw new IllegalStateException("TFIDF is requested, but it is not yet trained and the internal train data is also null");
            trainTfIdfFromInternalTrainData();
        }
        return tfidf;
    }

    public void setTfidf(TFIDF tfidf) {
        this.tfidf = tfidf;
    }

    public InstanceList getInstancesForGoldData(File documentJsonZip, File gsTable) throws DataReadingException {

        Map<String, Document> docsById = DataReader.readDocuments(documentJsonZip);
        InstanceList ret = createClassificationInstances(gsTable, docsById);

        return ret;
    }

    public InstanceList createClassificationInstances(File gsTable, Map<String, Document> docsById) throws DataReadingException {
        InstanceList ret = new InstanceList(new SerialPipes(getPipes()));
        DataReader.addPMLabels(gsTable, docsById);
        docsById.values().stream().map(doc -> new Instance(doc, doc.getPmLabel(), doc.getId(), "")).forEach(ret::addThruPipe);
        return ret;
    }

    public InstanceList createClassificationInstances(Map<String, Document> docsById) {
        InstanceList ret = new InstanceList(new SerialPipes(getPipes()));
        docsById.values().stream().map(doc -> new Instance(doc, doc.getPmLabel(), doc.getId(), "")).forEach(ret::addThruPipe);
        return ret;
    }

    public void trainTfIdf(Collection<Document> documents) {
        tfIdfTrainData = new ArrayList<>();
        for (Document doc : documents) {
            String text = doc.getTitle() + " " + doc.getAbstractText();
            //String text = doc.getAlltextreplacedentities();
            tfIdfTrainData.add(text);
        }
        trainTfIdfFromInternalTrainData();
    }

    private void trainTfIdfFromInternalTrainData() {
        assert tfIdfTrainData != null : "The training data for TF/IDF has not been set.";
        tfidf = new TFIDF();
        List<StringWrapper> trainData = new ArrayList<>(tfIdfTrainData.size());
        for (String str : tfIdfTrainData) {
            trainData.add(new BasicStringWrapper(str));
        }
        tfidf.train(new BasicStringWrapperIterator(trainData.iterator()));
    }


    private Collection<Pipe> getPipes() {
        return getTfIdfPipes();
    }

    private Collection<Pipe> getTfIdfPipes() {
        List<Pipe> pipes = new ArrayList<>();
        pipes.add(new Document2TokenPipe());
        pipes.add(new TfIdfPipe());
        pipes.add(new HasGenesPipe());
        pipes.add(new MeshTagsForTokenPipe());
        pipes.add(new HasPositiveBoostWordPipe());
        //pipes.add(new HasNegativeBoostWordPipe());
        //pipes.add(new HasDiseasePipe());
        //pipes.add(new HasKeywordPipe());
        //pipes.add(new Doc2VecPipe());
        //pipes.add(new HasPubTypePipe());
        //pipes.add(new TopicModelPipe());
        pipes.add(new Token2FeatureVector());
        return pipes;
    }


    private Collection<Pipe> getWordBasedPipes() {
        List<Pipe> pipes = new ArrayList<>();
        pipes.add(new Document2TokenSequencePipe());
        pipes.add(new MeshTagsForTokenSequencePipe());
        pipes.add(new TokenSequence2FeatureSequence());
        pipes.add(new FeatureSequence2AugmentableFeatureVector(false));
        return pipes;
    }

}
