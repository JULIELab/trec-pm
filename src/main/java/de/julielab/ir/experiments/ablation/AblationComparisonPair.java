package de.julielab.ir.experiments.ablation;

public class AblationComparisonPair {
    private double referenceScore;
    private double ablationScore;

    public AblationComparisonPair(double referenceScore, double ablationScore) {
        this.referenceScore = referenceScore;
        this.ablationScore = ablationScore;
    }

    /**
     * @return Score of the reference configuration without removing features.
     */
    public double getReferenceScore() {
        return referenceScore;
    }

    /**
     * @return Score of the ablation experiment where some features were removed / neutralized in the reference.
     */
    public double getAblationScore() {
        return ablationScore;
    }
}