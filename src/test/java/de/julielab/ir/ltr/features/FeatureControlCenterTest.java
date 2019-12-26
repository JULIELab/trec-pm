package de.julielab.ir.ltr.features;

import de.julielab.java.utilities.ConfigurationUtilities;
import org.apache.commons.configuration2.XMLConfiguration;
import org.junit.Test;

import java.io.File;
import java.util.Map;

import static de.julielab.ir.ltr.features.FCConstants.*;
import static de.julielab.java.utilities.ConfigurationUtilities.slash;
import static org.assertj.core.api.Assertions.assertThat;
public class FeatureControlCenterTest {

    @Test
    public void getWeightsFromFeatureConfiguration() throws Exception {
        XMLConfiguration config = ConfigurationUtilities.loadXmlConfiguration(new File("src/test/resources/feature-control-center/weightsFeatureTestConfiguration.xml"));
        Map<String, String> fieldBoosts = FeatureControlCenter.getWeightsFromFeatureConfiguration(config, slash(RETRIEVALPARAMETERS, TEMPLATEPARAMETERS, FIELDBOOSTS));
        assertThat(fieldBoosts.get("title_field_disease_boost")).isEqualTo("2");
        assertThat(fieldBoosts.get("title_field_gene_boost")).isEqualTo("3");
        assertThat(fieldBoosts.get("abstract_field_gene_boost")).isEqualTo("5");

        Map<String, String> clauseBoosts = FeatureControlCenter.getWeightsFromFeatureConfiguration(config, slash(RETRIEVALPARAMETERS, TEMPLATEPARAMETERS, KEYWORDBOOSTS));
        assertThat(clauseBoosts.get("match_all_boost")).isEqualTo("-100");
        assertThat(clauseBoosts.get("neg_keywords_boost")).isEqualTo("-1");
        assertThat(clauseBoosts.get("exists_abstract_boost")).isEqualTo("2");
    }
}