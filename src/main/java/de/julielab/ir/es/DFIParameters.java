package de.julielab.ir.es;

public class DFIParameters implements SimilarityParameters {
    private String independencyMeasure;

    public DFIParameters(String independencyMeasure) {

        this.independencyMeasure = independencyMeasure;
    }

    @Override
    public String toString() {
        return "DFIParameters{" +
                "independencyMeasure='" + independencyMeasure + '\'' +
                '}';
    }

    public String getIndependencyMeasure() {
        return independencyMeasure;
    }

    public void setIndependencyMeasure(String independencyMeasure) {
        this.independencyMeasure = independencyMeasure;
    }

    @Override
    public String getBaseSimilarity() {
        return "dfi";
    }

    @Override
    public String printToString() {
        return toString();
    }
}
