package de.julielab.ir.ltr.features.featuregroups;

import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import com.wcohen.ss.TFIDF;
import de.julielab.ir.ltr.Document;
import de.julielab.ir.ltr.features.FeatureGroup;
import de.julielab.ir.ltr.features.FeatureValueAssigner;
import de.julielab.java.utilities.IOStreamUtilities;

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
    public static String GROUP_NAME = "TFIDF";
    private Set<String> stopwords;
    transient private TFIDF tfidf;
    private Set<String> vocabulary;

    public TfidfFeatureGroup(TFIDF tfidf, Set<String> vocabulary) {
        super(GROUP_NAME);
        this.tfidf = tfidf;
        this.vocabulary = vocabulary;
        stopwords = IOStreamUtilities.getReaderFromInputStream(getClass().getResourceAsStream("/data/stopwords.txt")).lines().collect(Collectors.toSet());
    }

    public void setTfidf(TFIDF tfidf) {
        this.tfidf = tfidf;
    }

    @Override
    protected void addFeatures() {
        FeatureValueAssigner featureAssigner = inst -> {
            Token token = (Token) inst.getData();
            final Document document = (Document) inst.getSource();
            if (document.getCas() != null) {
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
            }
        };
        addFeature("TFIDF", featureAssigner);
    }

}
