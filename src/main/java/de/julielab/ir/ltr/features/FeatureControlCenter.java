package de.julielab.ir.ltr.features;

import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.Token2FeatureVector;
import cc.mallet.types.FeatureVector;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import com.wcohen.ss.TFIDF;
import de.julielab.ir.OriginalDocumentRetrieval;
import de.julielab.ir.ltr.Document;
import de.julielab.ir.ltr.DocumentList;
import de.julielab.ir.ltr.features.featuregroups.TfidfFeatureGroup;
import de.julielab.ir.ltr.features.featuregroups.TopicMatchFeatureGroup;
import de.julielab.ir.model.QueryDescription;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static de.julielab.ir.ltr.features.FCConstants.*;
import static de.julielab.java.utilities.ConfigurationUtilities.*;

public class FeatureControlCenter {
    private static final Logger log = LogManager.getLogger();
    private static FeatureControlCenter singleton;
    private HierarchicalConfiguration<ImmutableNode> configuration;

    private FeatureControlCenter(HierarchicalConfiguration<ImmutableNode> configuration) {
        this.configuration = configuration;
    }

    public static FeatureControlCenter getInstance() {
        return singleton;
    }

    public static void initialize(HierarchicalConfiguration<ImmutableNode> configuration) {
        singleton = new FeatureControlCenter(configuration);
    }

    public boolean filterActive(FeatureGroup featureGroup) {
        final boolean isActive = configuration.getBoolean(slash(FEATUREGROUPS, FEATUREGROUP + attrEqPred(NAME_ATTR, featureGroup.getName()), ACTIVE_ATTR), true);
        log.trace("Checking if feature group '{}' is active {}: ", featureGroup.getName(), isActive);
        return isActive;
    }

    public boolean filterActive(FeatureGroup featureGroup, Feature feature) {
        final boolean isActive = configuration.getBoolean(slash(FEATUREGROUPS, FEATUREGROUP + attrEqMultiPred("and", NAME_ATTR, featureGroup.getName(), ACTIVE_ATTR, "true")), true);
        log.trace("Checking if feature group '{}' is active {}: ", featureGroup.getName(), isActive);
        return isActive;
    }

    public void createFeatures(DocumentList<? extends QueryDescription> documents, TFIDF tfidf) {
        // We here use the MALLET facilities to create feature vectors.
        List<Pipe> featurePipes = new ArrayList<>();
        featurePipes.add(new Document2TokenPipe());
        featurePipes.add(new TfidfFeatureGroup(tfidf));
        featurePipes.add(new TopicMatchFeatureGroup());
        featurePipes.add(new Token2FeatureVector(false, false));
        featurePipes.add(new SetFeatureVectorPipe());
        final SerialPipes serialPipes = new SerialPipes(featurePipes);

        // Fetch the XMI cas information for the documents
        OriginalDocumentRetrieval.getInstance().setXmiCasDataToDocuments(documents);

        final InstanceList instanceList = new InstanceList(serialPipes);
        for (Document<? extends QueryDescription> document : documents) {
            final Instance instance = new Instance(document, document.getRelevance(), document.getId(), document);
            instanceList.addThruPipe(instance);
            // Within the pipes, the documents need access to their CAS. Since we only have a limited
            // amount of those for performance and scalability considerations, we need to release
            // the CASes back to the CAS pool after usage. The CAS pool is managed by the
            // OriginalDocumentRetrieval class.
            document.releaseJCas();
        }

        for (Instance instance : instanceList) {
            final FeatureVector fv = (FeatureVector) instance.getData();
            // TODO continue
        }
    }
}
