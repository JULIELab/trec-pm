package de.julielab.ir.ltr.features.featuregroups;

import cc.mallet.types.Token;
import de.julielab.ir.ltr.Document;
import de.julielab.ir.ltr.features.*;

import java.util.function.BiFunction;

public class IRSimilarityFeatureGroup extends FeatureGroup {
    public static final String BM25 = "bm25";
    public static final String DFR = "dfr";
    public static final String DFI = "dfi";
    public static final String IB = "ib";
    public static final String LMD = "lmd";
    public static final String LMJM = "lmjm";
    public static final String GROUP_NAME = "similarity";

    public IRSimilarityFeatureGroup() {
        super(GROUP_NAME);
    }

    @Override
    protected void addFeatures() {
        BiFunction<String, IRScoreFeatureKey, FeatureValueAssigner> assignerFactory = (fName, scoreType) -> inst -> {
            Token t = (Token) inst.getData();
            Document<?> d = (Document<?>) inst.getSource();
            final Double irScore = d.getIrScore(scoreType);
            if (irScore != null)
                t.setFeatureValue(fName, irScore);
        };
        // This is a hard case for the feature configuration because it is combinatorical.
        for (IRScore score : IRScore.values()) {
            for (TrecPmQueryPart part : TrecPmQueryPart.values()) {
                final IRScoreFeatureKey key = new IRScoreFeatureKey(score, part);
                addFeature(key.toString(), assignerFactory.apply(key.toString(), key));

            }
        }
    }
}
