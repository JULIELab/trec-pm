package de.julielab.ir.paramopt;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;

import static de.julielab.ir.ltr.features.PMFCConstants.*;
import static de.julielab.java.utilities.ConfigurationUtilities.slash;

public class SmacWrapperBaseTest {
    @Test
    public void test() throws Exception {
        String[] params = {"-ltrfeatures.featuregroups.featuregroup.TFIDF.active", "true",
                "-ltrfeatures.featuregroups.featuregroup.similarity.features.bm25.active", "true",
                "-indexparameters.bm25.k1", "1.2",
                "-indexparameters.bm25.b", "0.75",
                "-esqueryparameters.template", "hpipubnone.json",
                "-esqueryparameters.templateparameters.gene_match_type", "best_field",
                "-retrievalparameters.keywords.cancer.cancer", "true",
                "-retrievalparameters.keywords.cancer.carcinoma", "false",
                "-retrievalparameters.keywords.cancer.melanoma", "true",
        };

        SmacWrapperBase testwrapper = new SmacWrapperBase() {
            @Override
            protected double calculateScore(HierarchicalConfiguration<ImmutableNode> config, String instance, int seed) {
                return 0;
            }
        };
        HierarchicalConfiguration<ImmutableNode> config = testwrapper.parseConfiguration(params);
        List<HierarchicalConfiguration<ImmutableNode>> cancerConfigs = config.configurationsAt(slash(RETRIEVALPARAMETERS, KEYWORDS, CANCER));
        for (HierarchicalConfiguration<ImmutableNode> cconfig : cancerConfigs) {
            Iterator<String> keys = cconfig.getKeys();
            while (keys.hasNext()) {
                String key = keys.next();
                System.out.println(key);
                System.out.println(cconfig.getBoolean(key));
            }
        }

    }
}
