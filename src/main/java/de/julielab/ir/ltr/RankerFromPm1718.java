package de.julielab.ir.ltr;

import de.julielab.ir.ltr.features.IRScoreFeatureKey;

import java.io.File;
import java.io.IOException;

public class RankerFromPm1718 implements Ranker{
    @Override
    public void train(DocumentList documentList) {

    }

    @Override
    public void load(File modelFile) throws IOException {

    }

    @Override
    public void save(File modelFile) {

    }

    @Override
    public DocumentList rank(DocumentList documentList) {
        return null;
    }

    @Override
    public IRScoreFeatureKey getOutputScoreType() {
        return null;
    }

    @Override
    public void setOutputScoreType(IRScoreFeatureKey outputScoreType) {

    }
}
