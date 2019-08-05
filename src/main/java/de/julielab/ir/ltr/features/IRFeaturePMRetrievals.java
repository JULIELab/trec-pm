package de.julielab.ir.ltr.features;

import at.medunigraz.imi.bst.trec.experiment.TrecPmRetrieval;

import java.io.File;
import java.util.*;

public class IRFeaturePMRetrievals {
    private static final File DISEASE = ft("/subtemplates/biomedical_articles/disease.json");
    private static final File GENE = ft("/subtemplates/biomedical_articles/gene.json");
    private static final File NEGATIVE = ft("/subtemplates/biomedical_articles/negative_boosters.json");
    private static final File POSITIVE = ft("/subtemplates/biomedical_articles/positive_boosters.json");
    private static final File DNA = ft("/subtemplates/biomedical_articles/dna.json");
    private static final File CHEMO = ft("/subtemplates/biomedical_articles/chemotherapy.json");
    private static final File CANCER = ft("/subtemplates/biomedical_articles/cancer.json");

    public static Map<IRScoreFeatureKey, TrecPmRetrieval> getRetrievals(String index, EnumSet<TrecPmQueryPart> retrievalTypes) {
        Map<IRScoreFeatureKey, TrecPmRetrieval> ret = new HashMap<>();
        if (retrievalTypes.contains(TrecPmQueryPart.DISEASE))
            ret.put(new IRScoreFeatureKey(IRScore.BM25, TrecPmQueryPart.DISEASE), new TrecPmRetrieval(index).withSubTemplate(DISEASE));
        if (retrievalTypes.contains(TrecPmQueryPart.GENE))
            ret.put(new IRScoreFeatureKey(IRScore.BM25, TrecPmQueryPart.GENE), new TrecPmRetrieval(index).withSubTemplate(GENE));
        if (retrievalTypes.contains(TrecPmQueryPart.CHEMO))
            ret.put(new IRScoreFeatureKey(IRScore.BM25, TrecPmQueryPart.CHEMO), new TrecPmRetrieval(index).withSubTemplate(CHEMO));
        if (retrievalTypes.contains(TrecPmQueryPart.CANCER))
            ret.put(new IRScoreFeatureKey(IRScore.BM25, TrecPmQueryPart.CANCER), new TrecPmRetrieval(index).withSubTemplate(CANCER));
        if (retrievalTypes.contains(TrecPmQueryPart.DNA))
            ret.put(new IRScoreFeatureKey(IRScore.BM25, TrecPmQueryPart.DNA), new TrecPmRetrieval(index).withSubTemplate(DNA));
        if (retrievalTypes.contains(TrecPmQueryPart.POS_BOOSTS))
            ret.put(new IRScoreFeatureKey(IRScore.BM25, TrecPmQueryPart.POS_BOOSTS), new TrecPmRetrieval(index).withSubTemplate(POSITIVE));
        if (retrievalTypes.contains(TrecPmQueryPart.NEG_BOOSTS))
            ret.put(new IRScoreFeatureKey(IRScore.BM25, TrecPmQueryPart.NEG_BOOSTS), new TrecPmRetrieval(index).withSubTemplate(NEGATIVE));
        return ret;
    }

    private static File ft(String templatePath) {
        return new File(IRFeatureCTRetrievals.class.getResource(templatePath).getFile());
    }

}
