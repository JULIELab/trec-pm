package de.julielab.ir.ltr.features;

import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.Token2FeatureVector;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import com.wcohen.ss.TFIDF;
import de.julielab.ir.OriginalDocumentRetrieval;
import de.julielab.ir.ltr.Document;
import de.julielab.ir.ltr.DocumentList;
import de.julielab.ir.ltr.features.featuregroups.*;
import de.julielab.ir.model.QueryDescription;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static de.julielab.ir.ltr.features.FCConstants.*;
import static de.julielab.java.utilities.ConfigurationUtilities.*;

public class FeatureControlCenter {
    private static final Logger log = LogManager.getLogger();
    private static FeatureControlCenter singleton;
    private HierarchicalConfiguration<ImmutableNode> configuration;
    private DocumentEmbeddingFeatureGroup documentEmbeddingFeatureGroup;

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
            throw new IllegalStateException("The FeatureControlCenter has already been initialized. Do avoid confusion using the singleton pattern, reconfigurations must happen explicitly through the reconfigure method.");
        singleton = new FeatureControlCenter(configuration);
    }

    public static void reconfigure(HierarchicalConfiguration<ImmutableNode> configuration) {
        if (singleton == null)
            throw new IllegalStateException("The FeatureControlCenter was not yet initialized and thus cannot be reconfigured.");
        singleton = new FeatureControlCenter(configuration);
    }

    public static boolean isInitialized() {
        return singleton != null;
    }

    /**
     * <p>Extracts a string listing the active keywords in the feature configuration at the given path.</p>
     * <p>
     * The feature configuration has substructures like this:
     * <samp>
     * <pre>
     *             &lt;retrievalparameters&gt;
     *                 &lt;keywords&gt;
     *                      &lt;chemotherapy&gt;
     *                          &lt;*mab&gt;true&lt;/*mab&gt;
     *                          &lt;*nib&gt;false&lt;/*nib&gt;
     *                          &lt;*cin&gt;true&lt;*cin&gt;
     *                      &lt;/chemotherapy&gt;
     *                 &lt;/keywords&gt;
     *             &lt;/retrievalparameters&gt;
     *         </pre>
     * </samp>
     * To automatically create the list of active keywords - <tt>"*mab *cin"</tt> - the configuration path
     * <tt>retrievalparameter/keyword/chemotherapy</tt> is passed. By convention, the path must show to
     * a list of elements exactly named after the keywords with the text value <tt>true</tt> or <tt>false</tt>
     * for activation or omission. The list is automatically parsed from the configuration and pasted into
     * a single string where the keywords are whitespace-separated.
     * </p>
     *
     * @param config                   The feature configuration.
     * @param keywordConfigurationPath The path leading to the keyword list within the configuration.
     * @return A string containing the activated keywords in the feature configuration, whitespace-separated.
     */
    @NotNull
    public static String getKeywordStringFromFeatureConfiguration(HierarchicalConfiguration<ImmutableNode> config, String keywordConfigurationPath) {
        StringBuilder sb = new StringBuilder();
        List<HierarchicalConfiguration<ImmutableNode>> cancerConfigs = config.configurationsAt(keywordConfigurationPath);
        for (HierarchicalConfiguration<ImmutableNode> cconfig : cancerConfigs) {
            Iterator<String> keys = cconfig.getKeys();
            while (keys.hasNext()) {
                String key = keys.next();
                if (cconfig.getBoolean(key))
                    sb.append(key).append(" ");
            }
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    public boolean isTfIdfActive() {
        return configuration.getBoolean(slash(LTRFEATURES, FEATUREGROUPS, FEATUREGROUP + attrEqPred(NAME_ATTR, TfidfFeatureGroup.GROUP_NAME), ACTIVE_ATTR), true);
    }

    public boolean isSimilarityFeatureGroupActive() {
        return configuration.getBoolean(slash(LTRFEATURES, FEATUREGROUPS, FEATUREGROUP + attrEqPred(NAME_ATTR, IRSimilarityFeatureGroup.GROUP_NAME), ACTIVE_ATTR), true);
    }

    public boolean filterActive(FeatureGroup featureGroup) {
        final boolean isActive = configuration.getBoolean(slash(LTRFEATURES, FEATUREGROUPS, FEATUREGROUP + attrEqPred(NAME_ATTR, featureGroup.getName()), ACTIVE_ATTR), true);
        log.trace("Checking if feature group '{}' is active: {} ", featureGroup.getName(), isActive);
        return isActive;
    }

    public boolean filterActive(FeatureGroup featureGroup, Feature feature) {
        // featuregroups/featuregroup[@name='fgname']/feature[@name='fname' and @active='true']
        final boolean isActive = configuration.getBoolean(slash(LTRFEATURES, FEATUREGROUPS, FEATUREGROUP + attrEqPred(NAME_ATTR, featureGroup.getName()), FEATURE + attrEqMultiPred("and", NAME_ATTR, feature.getName(), ACTIVE_ATTR, "true")), true);
        log.trace("Checking if feature '{}' is active: {} ", slash(featureGroup.getName(), feature.getName()), isActive);
        return isActive;
    }

    /**
     * <p>Used given an already trained model.</p>
     *
     * @param documents
     * @param trainPipe
     * @param xmiTableName
     * @param <Q>
     */
    public <Q extends QueryDescription> void createFeatures(DocumentList<Q> documents, Pipe trainPipe, List<File> canonicalDbConnectionFiles, String xmiTableName) {
        // We here use the MALLET facilities to create feature vectors.

        // Fetch the XMI cas information for the documents
        OriginalDocumentRetrieval.getInstance().setXmiCasDataToDocuments(documents, canonicalDbConnectionFiles, xmiTableName);

        final InstanceList instanceList = new InstanceList(trainPipe);
        log.debug("Creating features for {} documents for ranking.", documents.size());
        for (Document<Q> document : documents) {
            final Instance instance = new Instance(document, document.getRelevance(), document.getId(), document);
            instanceList.addThruPipe(instance);
            document.setFeaturePipes(trainPipe);
            // Within the pipes, the documents need access to their CAS. Since we only have a limited
            // amount of those for performance and scalability considerations, we need to release
            // the CASes back to the CAS pool after usage. The CAS pool is managed by the
            // OriginalDocumentRetrieval class.
            document.releaseJCas();
        }
        log.debug("Done creating features for documents to be ranked.");
    }

    /**
     * Used to train a model.
     *
     * @param documents
     * @param tfidf
     * @param vocabulary
     * @param xmiTableName
     * @param <Q>
     */
    public <Q extends QueryDescription> void createFeatures(DocumentList<Q> documents, TFIDF tfidf, Set<String> vocabulary, String xmiTableName) {
        // We here use the MALLET facilities to create feature vectors.
        List<Pipe> featurePipes = new ArrayList<>();
        featurePipes.add(new Document2TokenPipe());
        if (documentEmbeddingFeatureGroup == null)
            documentEmbeddingFeatureGroup = new DocumentEmbeddingFeatureGroup();
        Stream.of(
                new TfidfFeatureGroup(tfidf, vocabulary),
                new RunTopicMatchAnnotatorFeatureGroup(),
                new TopicMatchFeatureGroup(),
                new IRSimilarityFeatureGroup(),
                documentEmbeddingFeatureGroup,
                new DocumentShapeFeatureGroup()
        ).filter(this::filterActive)
                .forEach(featurePipes::add);
        featurePipes.add(new Token2FeatureVector(false, true));
        // Sort and consolidate the feature vector values for AugmentableFeatureVectors.
        featurePipes.add(new SetFeatureVectorPipe());
        final SerialPipes serialPipes = new SerialPipes(featurePipes);

        // Fetch the XMI cas information for the documents
        OriginalDocumentRetrieval.getInstance().setXmiCasDataToDocuments(documents, null, xmiTableName);

        final InstanceList instanceList = new InstanceList(serialPipes);
        log.debug("Creating features for {} training documents.", documents.size());

        int linewidth = 80;

        int i = 0;
        for (Document<Q> document : documents) {
            i++;
            final Instance instance = new Instance(document, document.getRelevance(), document.getId(), document);
            instanceList.addThruPipe(instance);
            document.setFeaturePipes(serialPipes);
            // Within the pipes, the documents need access to their CAS. Since we only have a limited
            // amount of those for performance and scalability considerations, we need to release
            // the CASes back to the CAS pool after usage. The CAS pool is managed by the
            // OriginalDocumentRetrieval class.
            document.releaseJCas();

            double percentage = i / (double) documents.size();
            int progress = (int) (linewidth * percentage);
            System.out.print("[");
            for (int j = 0; j < linewidth; j++) {
                if (j < progress)
                    System.out.print("=");
                else
                    System.out.print(" ");
            }
            System.out.print("] " + percentage*100 +"%\r");
        }

        System.out.println();
        log.debug("Finished train document feature creation.");
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

    public HierarchicalConfiguration<ImmutableNode> getFeatureConfiguration() {
        return configuration;
    }
}
