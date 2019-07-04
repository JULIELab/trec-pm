package de.julielab.ir.ltr.features.featuregroups;

import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import com.wcohen.ss.TFIDF;
import de.julielab.ir.ltr.Document;
import de.julielab.ir.ltr.features.FeatureGroup;
import de.julielab.java.utilities.FileUtilities;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * This feature group does simply set the TF/IDF weights of the words in the document represented by the
 * instance passed to pipe() to the Token of the instance. Currently, the individual features - which would be
 * one feature for each possible word - cannot be toggled on/off individually using the toggle mechanism.
 * However, a stopword file is ready, currently a static one, excluding words found on that list.
 */
public class TfidfFeatureGroup extends FeatureGroup {

    private Set<String> stopwords;
    private TFIDF tfidf;
    private Set<String> vocabulary;

    public TfidfFeatureGroup(TFIDF tfidf, Set<String> vocabulary) {
        super("TFIDF");
        this.tfidf = tfidf;
        this.vocabulary = vocabulary;
        try {
            stopwords = FileUtilities.getReaderFromFile(new File("resources/stopwords.txt")).lines().collect(Collectors.toSet());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void addFeatures() {
        Consumer<Instance> featureAssigner = inst -> {
            Token token = (Token) inst.getData();
            final Document document = (Document) inst.getSource();
            com.wcohen.ss.api.Token[] tokens;
            // Synchronization is required because we might use the same TFIDF in multiple threads
            // and it is not thread safe (found out the hard way)
            synchronized (tfidf) {
                tfidf.prepare(document.getCas().getDocumentText());
                tokens = tfidf.getTokens();
            }
            for (com.wcohen.ss.api.Token tfidfToken : tokens) {
                if (vocabulary == null || vocabulary.contains(tfidfToken.getValue())) {
                    if (!stopwords.contains(tfidfToken.getValue().toLowerCase())) {
                        token.setFeatureValue(tfidfToken.getValue(), tfidf.getWeight(tfidfToken));
                    }
                }
            }
        };
        addFeature("TFIDF", featureAssigner);
    }

}
