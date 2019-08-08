package de.julielab.ir.ltr.features.featuregroups;

import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import de.julielab.ir.ltr.Document;
import de.julielab.ir.ltr.features.FeatureGroup;
import de.julielab.ir.ltr.features.IRScore;
import de.julielab.ir.ltr.features.IRScoreFeatureKey;
import de.julielab.ir.ltr.features.TrecPmQueryPart;

import java.util.function.BiFunction;
import java.util.function.Consumer;

public class IRSimilarityFeatureGroup extends FeatureGroup {
    public static final String BM25 = "bm25";
    public static final String DFR = "dfr";
    public static final String DFI = "dfi";
    public static final String IB = "ib";
    public static final String LMD = "lmd";
    public static final String LMJM = "lmjm";

    public IRSimilarityFeatureGroup() {
        super("similarity");
    }

    @Override
    protected void addFeatures() {
        BiFunction<String, IRScoreFeatureKey, Consumer<Instance>> assignerFactory = (fName, scoreType) -> inst -> {
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
