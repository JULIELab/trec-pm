package de.julielab.ir.ltr.features.features;

import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import de.julielab.ir.ltr.features.Feature;

public abstract class DocumentEmbeddingFeatures extends Feature {

    public DocumentEmbeddingFeatures(String name) {
        super(name);
    }

    @Override
    public void assignFeature(Instance inst) {
        assignDocumentEmbeddingFeatures(inst);
    }

    protected abstract void assignDocumentEmbeddingFeatures(Instance inst);

    protected abstract double[] getDocumentEmbedding(String documentText);

    /**
     * <p>Sets the given embedding vector values as feature values to the token.</p>
     * <p>The feature name for each embedding dimension is the value of the {@link #name} field,
     * followed by an underscore and the 0-based dimension number.</p>
     *
     * @param token     The MALLET token to add the embedding as feature values to.
     * @param embedding The embedding for the document of the token.
     */
    protected void addDocumentEmbeddingFeatures(Token token, double[] embedding) {
        for (int i = 0; i < embedding.length; i++) {
            double d = embedding[i];
            token.setFeatureValue(name + "_" + i, d);
        }
    }
}
