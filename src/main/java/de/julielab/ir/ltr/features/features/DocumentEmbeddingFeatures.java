package de.julielab.ir.ltr.features.features;

import cc.mallet.types.Token;

public abstract class DocumentEmbeddingFeatures {
    private String featureName;

    public DocumentEmbeddingFeatures(String featureName) {
        this.featureName = featureName;
    }

    protected abstract double[] getDocumentEmbedding(String documentText);

    /**
     * <p>Sets the given embedding vector values as feature values to the token.</p>
     * <p>The feature name for each embedding dimension is the value of the {@link #featureName} field,
     * followed by an underscore and the 0-based dimension number.</p>
     *
     * @param token     The MALLET token to add the embedding as feature values to.
     * @param embedding The embedding for the document of the token.
     */
    protected void addDocumentEmbeddingFeatures(Token token, double[] embedding) {
        for (int i = 0; i < embedding.length; i++) {
            double d = embedding[i];
            token.setFeatureValue(featureName + "_" + i, d);
        }
    }
}
