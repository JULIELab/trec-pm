package at.medunigraz.imi.bst.trec.experiment.registry;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.trec.experiment.Cord19Retrieval;
import at.medunigraz.imi.bst.trec.experiment.TrecPmRetrieval;
import de.julielab.ir.es.BM25Parameters;
import de.julielab.ir.es.SimilarityParameters;
import de.julielab.ir.ltr.features.FCConstants;
import de.julielab.ir.ltr.features.FeatureControlCenter;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

import java.util.HashMap;
import java.util.Map;

import static de.julielab.ir.ltr.features.FCConstants.*;
import static de.julielab.ir.ltr.features.PMFCConstants.TEMPLATEPARAMETERS;
import static de.julielab.ir.ltr.features.PMFCConstants.*;
import static de.julielab.java.utilities.ConfigurationUtilities.slash;

public final class Cord19RetrievalRegistry {

    private static final String TEMPLATE ="/templates/cord19/template.json";

    public static Cord19Retrieval defaultRun() {
        return new Cord19Retrieval(TrecConfig.ELASTIC_CORD19_INDEX).withExperimentName("cord19def")
                .withSubTemplate(TEMPLATE);
    }
}
