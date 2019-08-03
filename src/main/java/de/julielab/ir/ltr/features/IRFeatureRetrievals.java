package de.julielab.ir.ltr.features;

import at.medunigraz.imi.bst.trec.experiment.TrecPmRetrieval;

import java.io.File;
import java.util.*;

public class IRFeatureRetrievals {
    private static final File DISEASE = ft("/subtemplates/biomed_articles/disease.json");
    private static final File GENE = ft("/subtemplates/biomed_articles/gene.json");
    private static final File NEGATIVE = ft("/subtemplates/biomed_articles/negative_boosters.json");
    private static final File POSITIVE = ft("/subtemplates/biomed_articles/positive_boosters.json");
    private static final File DNA = ft("/subtemplates/biomed_articles/chemo.json");
    private static final File CHEMO = ft("/subtemplates/biomed_articles/cancer.json");
    private static final File CANCER = ft("/subtemplates/biomed_articles/dna.json");

    public static Map<IRScoreFeatureKey, TrecPmRetrieval> getRetrievals(String index, EnumSet<TrecPmQueryPart> retrievalTypes) {
        Map<IRScoreFeatureKey, TrecPmRetrieval> ret = new HashMap<>();
        if (retrievalTypes.contains(TrecPmQueryPart.DISEASE))
            ret.put(new IRScoreFeatureKey(IRScore.BM25, TrecPmQueryPart.DISEASE), new TrecPmRetrieval(index).withTemplate(DISEASE));
        if (retrievalTypes.contains(TrecPmQueryPart.GENE))
            ret.put(new IRScoreFeatureKey(IRScore.BM25, TrecPmQueryPart.GENE), new TrecPmRetrieval(index).withTemplate(GENE));
        if (retrievalTypes.contains(TrecPmQueryPart.CHEMO))
            ret.put(new IRScoreFeatureKey(IRScore.BM25, TrecPmQueryPart.CHEMO), new TrecPmRetrieval(index).withTemplate(CHEMO));
        if (retrievalTypes.contains(TrecPmQueryPart.CANCER))
            ret.put(new IRScoreFeatureKey(IRScore.BM25, TrecPmQueryPart.CANCER), new TrecPmRetrieval(index).withTemplate(CANCER));
        if (retrievalTypes.contains(TrecPmQueryPart.DNA))
            ret.put(new IRScoreFeatureKey(IRScore.BM25, TrecPmQueryPart.DNA), new TrecPmRetrieval(index).withTemplate(DNA));
        if (retrievalTypes.contains(TrecPmQueryPart.POS_BOOSTS))
            ret.put(new IRScoreFeatureKey(IRScore.BM25, TrecPmQueryPart.POS_BOOSTS), new TrecPmRetrieval(index).withTemplate(POSITIVE));
        if (retrievalTypes.contains(TrecPmQueryPart.NEG_BOOSTS))
            ret.put(new IRScoreFeatureKey(IRScore.BM25, TrecPmQueryPart.NEG_BOOSTS), new TrecPmRetrieval(index).withTemplate(NEGATIVE));
        return ret;
    }

    private static File ft(String templatePath) {
        return new File(IRFeatureRetrievals.class.getResource(templatePath).getFile());
    }
}
