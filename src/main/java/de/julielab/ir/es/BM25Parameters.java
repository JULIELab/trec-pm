package de.julielab.ir.es;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BM25Parameters implements SimilarityParameters{
    private double k1;
    private double b;

    @JsonProperty("k1")
    public double getK1() {
        return k1;
    }

    public void setK1(double k1) {
        this.k1 = k1;
    }

    @JsonProperty("b")
    public double getB() {
        return b;
    }

    public void setB(double b) {
        this.b = b;
    }

    public BM25Parameters(double k1, double b) {

        this.k1 = k1;
        this.b = b;
    }

    @Override
    public String toString() {
        return "BM25Parameters{" +
                "k1=" + k1 +
                ", b=" + b +
                '}';
    }

    @Override
    public String getBaseSimilarity() {
        return "bm25";
    }

    @Override
    public String printToString() {
        return toString();
    }
}
