package de.julielab.ir.es;

public class DFRParameters implements SimilarityParameters {

    private String basicModel;
    private String afterEffect;
    private String normalization;

    public DFRParameters(String basicModel, String afterEffect, String normalization) {

        this.basicModel = basicModel;
        this.afterEffect = afterEffect;
        this.normalization = normalization;
    }

    @Override
    public String toString() {
        return "DFRParameters{" +
                "basicModel='" + basicModel + '\'' +
                ", afterEffect='" + afterEffect + '\'' +
                ", normalization='" + normalization + '\'' +
                '}';
    }

    public String getBasicModel() {
        return basicModel;
    }

    public void setBasicModel(String basicModel) {
        this.basicModel = basicModel;
    }

    public String getAfterEffect() {
        return afterEffect;
    }

    public void setAfterEffect(String afterEffect) {
        this.afterEffect = afterEffect;
    }

    public String getNormalization() {
        return normalization;
    }

    public void setNormalization(String normalization) {
        this.normalization = normalization;
    }

    @Override
    public String getBaseSimilarity() {
        return "dfr";
    }

    @Override
    public String printToString() {
        return toString();
    }
}
