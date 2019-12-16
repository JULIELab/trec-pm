package de.julielab.ir.ltr.features.featuregroup;

import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import cc.mallet.util.PropertyList;
import de.julielab.ir.ltr.features.FeatureControlCenter;
import de.julielab.ir.ltr.features.FeatureGroup;
import de.julielab.ir.ltr.features.featuregroups.DocumentEmbeddingFeatureGroup;
import de.julielab.ir.ltr.features.features.DocumentEmbeddingFeatures;
import de.julielab.java.utilities.ConfigurationUtilities;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class DocumentEmbeddingFeatureGroupTest {

    @Test
    public void testEmbeddingFeatures() throws Exception {
        if (!FeatureControlCenter.isInitialized())
            FeatureControlCenter.initialize(ConfigurationUtilities.createEmptyConfiguration());
        else
            FeatureControlCenter.reconfigure(ConfigurationUtilities.createEmptyConfiguration());
        final DocumentEmbeddingFeatureGroup fg = new DocumentEmbeddingFeatureGroup();
        final Field featuresField = FeatureGroup.class.getDeclaredField("features");
        featuresField.setAccessible(true);
        featuresField.set(fg, Collections.singletonList(new DocumentEmbeddingTestFeature()));

        final Instance inst = new Instance(new Token("TestToken"), null, null, null);
        fg.pipe(inst);
        final Token t = (Token) inst.getData();
        final PropertyList.Iterator features = t.getFeatures().iterator();
        assertThat(t.getFeatures().size()).isEqualTo(3);
        while (features.hasNext()) {
            features.next();
            assertThat(features.getKey()).isIn("TestEmbedding_2", "TestEmbedding_1", "TestEmbedding_0");
            if (features.getKey().equals("TestEmbedding_2"))
                assertThat(features.getNumericValue()).isEqualTo(0.3);
            if (features.getKey().equals("TestEmbedding_1"))
                assertThat(features.getNumericValue()).isEqualTo(0.2);
            if (features.getKey().equals("TestEmbedding_1"))
                assertThat(features.getNumericValue()).isEqualTo(0.2);
        }

    }

    private class DocumentEmbeddingTestFeature extends DocumentEmbeddingFeatures {
        public DocumentEmbeddingTestFeature() {
            super("TestEmbedding");
        }

        @Override
        protected void assignDocumentEmbeddingFeatures(Instance inst) {
            final Token t = (Token) inst.getData();
            addDocumentEmbeddingFeatures(t, getDocumentEmbedding("dummy"));
        }

        @Override
        protected double[] getDocumentEmbedding(String documentText) {
            return new double[]{.1, .2, .3};
        }
    }
}
