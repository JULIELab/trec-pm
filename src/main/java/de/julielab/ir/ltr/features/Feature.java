package de.julielab.ir.ltr.features;

import cc.mallet.types.Token;

import java.util.function.Consumer;

public class Feature {
    private String name;
    /**
     * This consumer takes a MALLET token and sets a feature value to it.
     */
    private Consumer<Token> featureAssigner;

    public Feature(String name, Consumer<Token> featureAssigner) {
        this.name = name;
        this.featureAssigner = featureAssigner;
    }

    public void assignFeature(Token token) {
        featureAssigner.accept(token);
    }

    public String getName() {
        return name;
    }
}
