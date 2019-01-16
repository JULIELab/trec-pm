package at.medunigraz.imi.bst.pmclassifier.featurepipes;

import at.medunigraz.imi.bst.pmclassifier.InstancePreparator;
import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import com.wcohen.ss.TFIDF;
import de.julielab.java.utilities.FileUtilities;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

public class TfIdfPipe extends Pipe {
    Set<String> stopwords;

    public TfIdfPipe() {
        try {
            stopwords = FileUtilities.getReaderFromFile(new File("stopwords.txt")).lines().collect(Collectors.toSet());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Instance pipe(Instance inst) {
        final InstancePreparator instancePreparator = InstancePreparator.getInstance();
        TFIDF tfidf;
        synchronized (instancePreparator) {
            tfidf = instancePreparator.getTfidf();
        }
        assert tfidf != null : "The TFIDF model has not been trained.";
        Token token = (Token) inst.getData();
        com.wcohen.ss.api.Token[] tokens;
        // Synchronization is required because the InstancePreparator is static and thus, the TFIDF object
        // is the same for all threads and it is not thread safe (found out the hard way)
        synchronized (tfidf) {
            tfidf.prepare(token.getText());
            tokens = tfidf.getTokens();
        }
        com.wcohen.ss.api.Token lasttoken = null;
        for (com.wcohen.ss.api.Token tfidfToken : tokens) {
            if (!stopwords.contains(tfidfToken.getValue().toLowerCase())) {
                token.setFeatureValue(tfidfToken.getValue(), tfidf.getWeight(tfidfToken));
//                if (lasttoken != null)
//                    token.setFeatureValue(lasttoken.getValue()+tfidfToken.getValue(), (tfidf.getWeight(lasttoken) + tfidf.getWeight(tfidfToken))/2);
//                lasttoken = tfidfToken;
            }
        }
        return inst;
    }
}
