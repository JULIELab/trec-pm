package de.julielab.ir.paramopt;

import org.junit.Test;

public class SmacWrapperBaseTest {
    @Test
    public void test() throws Exception {
        String[] params = {"-ltrfeatures.featuregroups.featuregroup.TFIDF.active", "true",
        "-ltrfeatures.featuregroups.featuregroup.similarity.features.bm25.active", "true",
        "-indexparameters.bm25.k1", "1.2",
        "-indexparameters.bm25.b", "0.75",
        "-esqueryparameters.template", "hpipubnone.json",
        "-esqueryparameters.templateparameters.gene_match_type", "best_field"};

       ConfigWriterSmacWrapper.main(params);
    }
}
