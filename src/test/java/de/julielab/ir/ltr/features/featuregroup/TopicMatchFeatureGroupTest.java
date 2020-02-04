package de.julielab.ir.ltr.features.featuregroup;

import at.medunigraz.imi.bst.trec.model.Topic;
import at.medunigraz.imi.bst.trec.model.TopicSet;
import at.medunigraz.imi.bst.trec.model.TrecPMTopicSetFactory;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.Token2FeatureVector;
import cc.mallet.types.Alphabet;
import cc.mallet.types.FeatureVector;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import de.julielab.ir.TrecCacheConfiguration;
import de.julielab.ir.ltr.Document;
import de.julielab.ir.ltr.features.Document2TokenPipe;
import de.julielab.ir.ltr.features.FeatureControlCenter;
import de.julielab.ir.ltr.features.SetFeatureVectorPipe;
import de.julielab.ir.ltr.features.featuregroups.RunTopicMatchAnnotatorFeatureGroup;
import de.julielab.ir.ltr.features.featuregroups.TopicMatchFeatureGroup;
import de.julielab.ir.ltr.features.featurenames.MatchType;
import de.julielab.ir.umls.UmlsRelationsProvider;
import de.julielab.ir.umls.UmlsSynsetProvider;
import de.julielab.java.utilities.ConfigurationUtilities;
import de.julielab.java.utilities.cache.CacheService;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class TopicMatchFeatureGroupTest {

    @BeforeClass
    public static void setUp() {
        System.setProperty(CacheService.CACHING_ENABLED_PROP, "false");
        UmlsRelationsProvider.setRelationsSourceFile("src/test/resources/umls/example.relations");
        UmlsSynsetProvider.setDefaultSynsetFile("src/test/resources/umls/example.synsets");
        UmlsSynsetProvider.setDefaultSemanticTypesFile("src/test/resources/umls/semanticTypes.test");
    }

    /**
     * This test expects the UMLS-derived files for hypernyms and synonyms to be present. They are
     * used in decorators employed by {@link RunTopicMatchAnnotatorFeatureGroup}.
     * @throws Exception
     */
    @Test
    public void test() throws Exception {
        CacheService.initialize(new TrecCacheConfiguration());
        final HierarchicalConfiguration<ImmutableNode> featureConfig = ConfigurationUtilities.createEmptyConfiguration();

        if (!FeatureControlCenter.isInitialized())
            FeatureControlCenter.initialize(featureConfig);
        else
            FeatureControlCenter.reconfigure(featureConfig);

        final TopicSet topicSet = TrecPMTopicSetFactory.topics2018();
        final Topic testTopic = topicSet.getTopics().get(4);
        // This topic matches to the document text
        // The following topic expansions are semantic nonsense and just serve the test.
        testTopic.withDiseaseSynonym("membrane");
        testTopic.withDiseaseHypernym("remodeling");

        List<Pipe> featurePipes = new ArrayList<>();
        featurePipes.add(new Document2TokenPipe());
        featurePipes.add(new RunTopicMatchAnnotatorFeatureGroup());
        featurePipes.add(new TopicMatchFeatureGroup());
        featurePipes.add(new Token2FeatureVector(false, false));
        featurePipes.add(new SetFeatureVectorPipe());
        final SerialPipes pipe = new SerialPipes(featurePipes);

        Document<Topic> doc = new Document<>();
        doc.setId("testdoc");
        final JCas jCas = JCasFactory.createJCas("de.julielab.jcore.types.jcore-semantics-biology-types");
        jCas.setDocumentText("Just for the test, here the variant without the gene name: V600E. Arf6 is a member of the Ras-superfamily of small GTPases and controls membrane trafficking and cytoskeletal remodeling, functioning mainly in endocytosis pathways at the cell periphery. Arf6 is activated by various extracellular signals and oncogenic events and has been shown to promote cell migration and pro-invasive phenotype in human cancer cells. Small molecule inhibition of ARF6 reduces spontaneous metastasis in xenograft models of human cutaneous melanoma, suggesting that ARF6 is necessary for disease progression. Using a genetically-engineered mouse model of BRAF-mutant melanoma, we determined whether activation of Arf6 is sufficient to induce spontaneous metastasis in vivo. For melanocyte-specific primary tumor induction, Cre recombinase was delivered via local injection of RCAS virus into DCT-TVA;Cdkn2alox/lox;BRAFV600E mice. In the experimental mice, a constitutively active mutant form of Arf6 (Q67L) was virally delivered with Cre. In this study, we observed a significant increase in spontaneous metastatic disease burden in Arf6 Q67L mice. Likewise, tail vein injection of melanoma cell lines derived from Arf6 Q67L tumors consistently show a diffuse pattern of pulmonary metastasis compared to controls that show rare, microscopic metastasis. Recently, it has been demonstrated that activation of Akt, but not Pten loss, leads to an aggressive phenotype in melanoma that includes the acquisition of brain metastases. Immunohistochemical analysis of phospho-Akt revealed that Arf6 Q67L is sufficient to induce Akt activation in tumors. In addition, ARF6 is necessary for AKT activation in human melanoma lines. We did not observe brain metastasis in DCT-TVA;Cdkn2alox/lox;BRAFV600E + Arf6 Q67L mice. When we added Ptenlox/lox allele to this genetic background, however, we observed microscopic brain metastases at a low frequency, suggesting that the combination of Pten loss and Arf6 activation reaches a threshold level of Akt activation that is sufficient to cause brain metastasis. Taken together our data indicate that activation of Arf6 is sufficient to potentiate melanoma metastasis, consistent with the proinvasive cellular phenotype attributed to Arf6. In addition, our data suggests a novel signaling mechanism by which Arf6, a small GTPase involved in trafficking, is somehow involved in Akt activation and that this step may be important for the acquisition of metastatic capacity.");
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XmiCasSerializer.serialize(jCas.getCas(), baos);
        doc.setFullDocumentData(baos.toByteArray());
        doc.setQueryDescription(testTopic);

        final Instance inst = new Instance(doc, 0, doc.getId(), doc);
        final InstanceList il = new InstanceList(pipe);
        il.addThruPipe(inst);

        FeatureVector fv = (FeatureVector) inst.getData();
        final Alphabet alphabet = fv.getAlphabet();
        assertThat(fv.getIndices().length).isEqualTo(6);
        for (int i : fv.getIndices()) {
            final String featureName = (String) alphabet.lookupObject(i);
            assertThat(featureName).isIn(Arrays.stream(MatchType.values()).map(Enum::name).collect(Collectors.toSet()));
            double value = fv.value(i);
            MatchType matchType = MatchType.valueOf(featureName);
            if (matchType == MatchType.VARIANT)
                assertThat(value).isEqualTo(1.0);
            if (matchType == MatchType.GENE)
                assertThat(value).isEqualTo(1.0);
            if (matchType == MatchType.GENE_AND_VARIANT)
                assertThat(value).isEqualTo(2.0);
            if (matchType == MatchType.DISEASE_HYPERNYM)
                assertThat(value).isEqualTo(4.0);
            if (matchType == MatchType.DISEASE)
                assertThat(value).isEqualTo(5.0);
            if (matchType == MatchType.DISEASE_SYNONYM)
                assertThat(value).isEqualTo(1.0);
        }
    }
}
