package de.julielab.ir.ltr.features.featuregroups;

import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import de.julielab.ir.ltr.Document;
import de.julielab.ir.ltr.features.FeatureGroup;
import de.julielab.ir.ltr.features.IRScore;

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
        BiFunction<String, IRScore, Consumer<Instance>> assignerFactory = (fName, scoreType) -> inst -> {
            Token t = (Token) inst.getData();
            Document<?> d = (Document<?>) inst.getSource();
            final Double irScore = d.getIrScore(scoreType);
            if (irScore != null)
                t.setFeatureValue(fName, irScore);
        };
        addFeature(BM25, assignerFactory.apply(BM25, IRScore.BM25));
        addFeature(BM25, assignerFactory.apply(DFR, IRScore.DFR));
        addFeature(BM25, assignerFactory.apply(DFI, IRScore.DFI));
        addFeature(BM25, assignerFactory.apply(IB, IRScore.IB));
        addFeature(BM25, assignerFactory.apply(LMD, IRScore.LMD));
        addFeature(BM25, assignerFactory.apply(LMJM, IRScore.LMJM));
    }
}
