package de.julielab.ir.es;

public class TFIDFParameters implements SimilarityParameters {
    @Override
    public String toString() {
        return "TFIDFParameters{}";
    }

    @Override
    public String getBaseSimilarity() {
        return "tfidf";
    }

    @Override
    public String printToString() {
        return toString();
    }
}
