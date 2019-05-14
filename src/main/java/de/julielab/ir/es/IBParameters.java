package de.julielab.ir.es;

public class IBParameters implements SimilarityParameters{
    private String distribution;
    private String lambda;

    @Override
    public String toString() {
        return "IBParameters{" +
                "distribution='" + distribution + '\'' +
                ", lambda='" + lambda + '\'' +
                ", normalization='" + normalization + '\'' +
                '}';
    }

    public String getDistribution() {
        return distribution;
    }

    public void setDistribution(String distribution) {
        this.distribution = distribution;
    }

    public String getLambda() {
        return lambda;
    }

    public void setLambda(String lambda) {
        this.lambda = lambda;
    }

    public String getNormalization() {
        return normalization;
    }

    public void setNormalization(String normalization) {
        this.normalization = normalization;
    }

    public IBParameters(String distribution, String lambda, String normalization) {

        this.distribution = distribution;
        this.lambda = lambda;
        this.normalization = normalization;
    }

    private String normalization;

    @Override
    public String getBaseSimilarity() {
        return "ib";
    }

    @Override
    public String printToString() {
        return toString();
    }
}
