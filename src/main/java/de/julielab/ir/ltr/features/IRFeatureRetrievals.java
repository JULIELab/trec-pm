package de.julielab.ir.ltr.features;

import at.medunigraz.imi.bst.trec.experiment.TrecPmRetrieval;

import java.io.File;
import java.util.EnumSet;

public class IRFeatureRetrievals {
    private static final File DISEASE = ft("/subtemplates/biomed_articles/disease.json");
    private static final File GENE = ft("/subtemplates/biomed_articles/gene.json");
    private static final File NEGATIVE = ft("/subtemplates/biomed_articles/negative_boosters.json");
    private static final File POSITIVE = ft("/subtemplates/biomed_articles/positive_boosters.json");
    private static final File DNA = ft("/subtemplates/biomed_articles/chemo.json");
    private static final File CHEMO = ft("/subtemplates/biomed_articles/cancer.json");
    private static final File CANCER = ft("/subtemplates/biomed_articles/dna.json");
    public static void getRetrievals(String index, EnumSet<IRScore> scoreTypes) {
        new TrecPmRetrieval(index);
    }

    private static File ft(String templatePath) {
        return new File(IRFeatureRetrievals.class.getResource(templatePath).getFile());
    }
}
