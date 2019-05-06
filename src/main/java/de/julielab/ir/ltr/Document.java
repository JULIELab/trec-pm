package de.julielab.ir.ltr;

import cc.mallet.types.FeatureVector;
import de.julielab.ir.OriginalDocumentRetrieval;
import de.julielab.ir.ltr.features.IRScore;
import de.julielab.ir.model.QueryDescription;
import org.apache.uima.jcas.JCas;

import java.util.HashMap;
import java.util.Map;

/**
 * A (topic/document) pair. Such objects are the input to ranking algorithms that will rank multiple documents
 * with respect to their topic (query).
 */
public class Document<Q extends QueryDescription> {
    private String id;
    private Q topic;
    private String type;
    /**
     * The gold standard given relevance level, e.g. 0, 1, 2 for not relevant, partially relevant and definitively relevant.
     */
    private int relevance;
    /**
     * Scores that this document has for the given query.
     */
    private Map<IRScore, Double> irScores;
    /**
     * The serialized CAS data of this document as retrieved from the database. To be set through
     * {@link de.julielab.ir.OriginalDocumentRetrieval#setXmiCasDataToDocuments(DocumentList)}.
     */
    private byte[][] fullDocumentData;
    /**
     * The feature vector that is created for this document for the LtR algorithms.
     */
    private FeatureVector fv;
    /**
     * The deserialized version of fullDocumentData. To be created by {@link de.julielab.ir.OriginalDocumentRetrieval#parseXmiDataIntoJCas(byte[][])}.
     * This field will mostly be null because it is very expansive to keep a lot of CAS instances around.
     * Thus, this field will be populated on request.
     */
    private JCas cas;

    public FeatureVector getFeatureVector() {
        return fv;
    }

    public void setFeatureVector(FeatureVector fv) {
        this.fv = fv;
    }

    /**
     * Returns the UIMA CAS of the document. This can only be done if {@link #setFullDocumentData(byte[][])} has
     * been used before to set the serialized CAS data. If this method hasn't been called before or the CAS
     * had been released, the XMI CAS data will be parsed into a CAS from the CasPool managed by
     * {@link OriginalDocumentRetrieval#casPool}
     *
     * @return The UIMA CAS of this document.
     */
    public JCas getCas() {
        if (cas == null && fullDocumentData == null)
            throw new IllegalStateException("Cannot create a CAS because the XMI data has not been set to this document.");
        if (cas == null)
            OriginalDocumentRetrieval.getInstance().parseXmiDataIntoJCas(fullDocumentData);
        return cas;
    }

    public int getRelevance() {

        return relevance;
    }

    public void setRelevance(int relevance) {
        this.relevance = relevance;
    }

    public Map<IRScore, Double> getIrScores() {
        return irScores;
    }

    public void setScore(IRScore type, double score) {
        if (irScores == null)
            irScores = new HashMap<>();
        irScores.put(type, score);

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Q getQueryDescription() {
        return topic;
    }


    public void setTopic(Q topic) {
        this.topic = topic;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public byte[][] getFullDocumentData() {
        return fullDocumentData;
    }

    public void setFullDocumentData(byte[][] fullDocumentData) {
        this.fullDocumentData = fullDocumentData;
    }

    /**
     * Releases the JCas of this document and sets its reference to <tt>null</tt>. If the JCas is required again
     * after this call, its XMI data will be parsed into some CAS retrieved from {@link OriginalDocumentRetrieval#casPool}.
     */
    public void releaseJCas() {
        if (cas != null)
            cas.release();
        cas = null;
    }
}
