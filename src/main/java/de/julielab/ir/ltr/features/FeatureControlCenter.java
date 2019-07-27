package de.julielab.ir.ltr.features;

import at.medunigraz.imi.bst.trec.model.Topic;
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
import de.julielab.ir.ltr.features.featuregroups.RunTopicMatchAnnotatorFeatureGroup;
import de.julielab.ir.ltr.features.featuregroups.TfidfFeatureGroup;
import de.julielab.ir.ltr.features.featuregroups.TopicMatchFeatureGroup;
import de.julielab.ir.model.QueryDescription;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

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
        if (singleton == null)
            throw new IllegalStateException("The FeatureControlCenter must be initialized once per application start with the feature configuration to be used. This has not happened yet.");
        return singleton;
    }

    public static void initialize(HierarchicalConfiguration<ImmutableNode> configuration) {
        if (singleton != null)
            throw new IllegalStateException("The FeatureControlCenter has already been initialized. Do avoid confusion using the singleton pattern, reconfigurations are prohibited.");
        singleton = new FeatureControlCenter(configuration);
    }

    public static boolean isInitialized() {
        return singleton != null;
    }

    public boolean filterActive(FeatureGroup featureGroup) {
        final boolean isActive = configuration.getBoolean(slash(FEATUREGROUPS, FEATUREGROUP + attrEqPred(NAME_ATTR, featureGroup.getName()), ACTIVE_ATTR), true);
        log.trace("Checking if feature group '{}' is active: {} ", featureGroup.getName(), isActive);
        return isActive;
    }

    public boolean filterActive(FeatureGroup featureGroup, Feature feature) {
        // featuregroups/featuregroup[@name='fgname']/feature[@name='fname' and @active='true']
        final boolean isActive = configuration.getBoolean(slash(FEATUREGROUPS, FEATUREGROUP + attrEqPred(NAME_ATTR, featureGroup.getName()), FEATURE + attrEqMultiPred("and", NAME_ATTR, feature.getName(), ACTIVE_ATTR, "true")), true);
        log.trace("Checking if feature '{}' is active: {} ", slash(featureGroup.getName(), feature.getName()), isActive);
        return isActive;
    }

    public void createFeatures(DocumentList<? extends QueryDescription> documents, Iterable<Topic> topics, TFIDF tfidf, Set<String> vocabulary) {
        // We here use the MALLET facilities to create feature vectors.
        List<Pipe> featurePipes = new ArrayList<>();
        featurePipes.add(new Document2TokenPipe());
        Stream.of(
                new TfidfFeatureGroup(tfidf, vocabulary),
                new RunTopicMatchAnnotatorFeatureGroup(topics),
                new TopicMatchFeatureGroup()
        ).filter(this::filterActive)
                .forEach(featurePipes::add);
        featurePipes.add(new Token2FeatureVector(false, false));
        // Sort and consolidate the feature vector values for AugmentableFeatureVectors.
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
            // TODO EF July 4th, 2019: but - continue what? I forgot what I wanted to do here XD
        }
    }

    public String getActiveFeatureDescriptionString() {
        StringBuilder sb = new StringBuilder();
        final List<HierarchicalConfiguration<ImmutableNode>> featureGroupConfigurations = configuration.configurationsAt(FEATUREGROUPS);
        for (HierarchicalConfiguration<ImmutableNode> featureGroupConfiguration : featureGroupConfigurations) {
            final String name = featureGroupConfiguration.getString(slash(FEATUREGROUP, NAME_ATTR));
            final boolean isActive = featureGroupConfiguration.getBoolean(slash(FEATUREGROUP, ACTIVE_ATTR));
            if (isActive) {
                if (sb.length() != 0)
                    sb.append("-");
                sb.append("FG:").append(name);
                final List<HierarchicalConfiguration<ImmutableNode>> featureConfigurations = featureGroupConfiguration.configurationsAt(FEATURE);
                if (!featureConfigurations.isEmpty())
                    sb.append("[");
                for (HierarchicalConfiguration<ImmutableNode> featureConfiguration : featureConfigurations) {
                    String featureName = featureConfiguration.getString(slash(FEATUREGROUP, NAME_ATTR));
                    boolean featureIsActive = featureConfiguration.getBoolean(slash(FEATURE, ACTIVE_ATTR));
                    if (featureIsActive) {
                        sb.append("F:").append(featureName).append("-");
                    }
                    sb.deleteCharAt(sb.length() - 1);
                }
                if (!featureConfigurations.isEmpty())
                    sb.append("]");
            }
        }
        return sb.toString();
    }
}
