package de.julielab.ir.paramopt;

import org.apache.commons.configuration2.ConfigurationUtils;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.junit.Test;

public class SmacWrapperBaseTest {
    @Test
    public void test() throws Exception {
        String[] params = {"1","2","3","4","5","-ltrfeatures.featuregroups.featuregroup.TFIDF.active", "true",
                "-ltrfeatures.featuregroups.featuregroup.similarity.features.bm25.active", "true",
                "-indexparameters.bm25.k1", "1.2",
                "-indexparameters.bm25.b", "0.75",
                "-esqueryparameters.template", "hpipubnone.json",
                "-esqueryparameters.templateparameters.gene_match_type", "best_field",
                "-retrievalparameters.keywords.cancer@word:'cancer'", "true",
                "-retrievalparameters.keywords.cancer@word:'carcinoma'", "false",
                "-retrievalparameters.keywords.cancer@word:'melanoma'", "true",
        };

        SmacWrapperBase testwrapper = new SmacWrapperBase() {
            @Override
            protected String calculateScore(HierarchicalConfiguration<ImmutableNode> config, String[] metricsToReturn, String instance, int seed) {
                return "0";
            }
        };
        HierarchicalConfiguration<ImmutableNode> config = testwrapper.parseConfiguration(params);
        System.out.println(ConfigurationUtils.toString(config));

    }
}
