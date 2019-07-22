package de.julielab.ir.ltr.features.featuregroup;

import at.medunigraz.imi.bst.trec.model.Challenge;
import at.medunigraz.imi.bst.trec.model.Task;
import at.medunigraz.imi.bst.trec.model.Topic;
import at.medunigraz.imi.bst.trec.model.TopicSet;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.Token2FeatureVector;
import cc.mallet.types.FeatureVector;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import de.julielab.ir.ltr.Document;
import de.julielab.ir.ltr.features.Document2TokenPipe;
import de.julielab.ir.ltr.features.FCConstants;
import de.julielab.ir.ltr.features.FeatureControlCenter;
import de.julielab.ir.ltr.features.SetFeatureVectorPipe;
import de.julielab.ir.ltr.features.featuregroups.RunTopicMatchAnnotatorFeatureGroup;
import de.julielab.ir.ltr.features.featuregroups.TopicMatchFeatureGroup;
import de.julielab.java.utilities.ConfigurationUtilities;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ConfigurationUtils;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static de.julielab.ir.ltr.features.FCConstants.FEATUREGROUP;
import static de.julielab.ir.ltr.features.FCConstants.FEATUREGROUPS;
import static de.julielab.ir.ltr.features.FCConstants.NAME_ATTR;
import static de.julielab.java.utilities.ConfigurationUtilities.slash;
import static de.julielab.java.utilities.ConfigurationUtilities.ws;

public class TopicMatchFeatureGroupTest {
    @Test
    public void test() throws Exception {
        final HierarchicalConfiguration<ImmutableNode> featureConfig = ConfigurationUtilities.createEmptyConfiguration();
      //  featureConfig.addProperty(slash(FEATUREGROUPS, FEATUREGROUP+ NAME_ATTR), RunTopicMatchAnnotatorFeatureGroup.);

        FeatureControlCenter.initialize(featureConfig);

        final TopicSet topicSet = new TopicSet(new File(getClass().getResource("/topics/topics2018.xml").getFile()), Challenge.TREC_PM, Task.PUBMED, 2018);
        final Topic testTopic = topicSet.getTopics().get(4);
        // This topic matches to the document text
        // The following topic expansions are semantic nonsense and just serve the test.
        testTopic.withDiseaseSynonym("membrane");
        testTopic.withDiseaseHypernym("remodeling");

        List<Pipe> featurePipes = new ArrayList<>();
        featurePipes.add(new Document2TokenPipe());
        featurePipes.add(new RunTopicMatchAnnotatorFeatureGroup(topicSet.getTopics()));
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

        final Instance inst = new Instance(doc, 0, doc.getId(), doc);
        final InstanceList il = new InstanceList(pipe);
        il.addThruPipe(inst);

        FeatureVector fv = (FeatureVector) inst.getData();
        System.out.println("Hier: " + fv);
    }
}
