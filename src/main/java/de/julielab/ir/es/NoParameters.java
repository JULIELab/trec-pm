package de.julielab.ir.es;

public class NoParameters implements SimilarityParameters{
    @Override
    public String toString() {
        return "NoParameters{}";
    }

    @Override
    public String getBaseSimilarity() {
        return "none";
    }

    @Override
    public String printToString() {
        return toString();
    }
}
