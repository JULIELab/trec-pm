package de.julielab.ir.es;

public class LMDParameters implements SimilarityParameters {
    private int mu;

    public LMDParameters(int mu) {
        this.mu = mu;
    }

    @Override
    public String toString() {
        return "LMDParameters{" +
                "mu=" + mu +
                '}';
    }

    public int getMu() {

        return mu;
    }

    public void setMu(int mu) {
        this.mu = mu;
    }

    @Override
    public String getBaseSimilarity() {
        return "lmd";
    }

    @Override
    public String printToString() {
        return toString();
    }
}
