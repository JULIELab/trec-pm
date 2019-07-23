package de.julielab.ir.ltr.features;

import at.medunigraz.imi.bst.trec.model.Challenge;
import at.medunigraz.imi.bst.trec.model.Topic;
import at.medunigraz.imi.bst.trec.model.TopicSet;
import de.julielab.ir.ltr.features.featurenames.MatchType;
import de.julielab.jcore.types.Disease;
import de.julielab.jcore.types.Gene;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
public class TopicFieldsCasAnnotatorTest {
    @Test
    public void test() throws Exception {
        final TopicSet topicSet = new TopicSet(new File(getClass().getResource("/topics/topics2018.xml").getFile()), Challenge.TREC_PM, 2018);
        final Topic testTopic = topicSet.getTopics().get(4);
        // This topic matches to the document text
        // The following topic expansions are semantic nonsense and just serve the test.
        testTopic.withDiseaseSynonym("membrane");
        testTopic.withDiseaseHypernym("remodeling");
        final TopicFieldsCasAnnotator annotator = new TopicFieldsCasAnnotator(topicSet.getTopics());
        final JCas jCas = JCasFactory.createJCas("de.julielab.jcore.types.jcore-semantics-biology-types");
        jCas.setDocumentText("Just for the test, here the variant without the gene name: V600E. Arf6 is a member of the Ras-superfamily of small GTPases and controls membrane trafficking and cytoskeletal remodeling, functioning mainly in endocytosis pathways at the cell periphery. Arf6 is activated by various extracellular signals and oncogenic events and has been shown to promote cell migration and pro-invasive phenotype in human cancer cells. Small molecule inhibition of ARF6 reduces spontaneous metastasis in xenograft models of human cutaneous melanoma, suggesting that ARF6 is necessary for disease progression. Using a genetically-engineered mouse model of BRAF-mutant melanoma, we determined whether activation of Arf6 is sufficient to induce spontaneous metastasis in vivo. For melanocyte-specific primary tumor induction, Cre recombinase was delivered via local injection of RCAS virus into DCT-TVA;Cdkn2alox/lox;BRAFV600E mice. In the experimental mice, a constitutively active mutant form of Arf6 (Q67L) was virally delivered with Cre. In this study, we observed a significant increase in spontaneous metastatic disease burden in Arf6 Q67L mice. Likewise, tail vein injection of melanoma cell lines derived from Arf6 Q67L tumors consistently show a diffuse pattern of pulmonary metastasis compared to controls that show rare, microscopic metastasis. Recently, it has been demonstrated that activation of Akt, but not Pten loss, leads to an aggressive phenotype in melanoma that includes the acquisition of brain metastases. Immunohistochemical analysis of phospho-Akt revealed that Arf6 Q67L is sufficient to induce Akt activation in tumors. In addition, ARF6 is necessary for AKT activation in human melanoma lines. We did not observe brain metastasis in DCT-TVA;Cdkn2alox/lox;BRAFV600E + Arf6 Q67L mice. When we added Ptenlox/lox allele to this genetic background, however, we observed microscopic brain metastases at a low frequency, suggesting that the combination of Pten loss and Arf6 activation reaches a threshold level of Akt activation that is sufficient to cause brain metastasis. Taken together our data indicate that activation of Arf6 is sufficient to potentiate melanoma metastasis, consistent with the proinvasive cellular phenotype attributed to Arf6. In addition, our data suggests a novel signaling mechanism by which Arf6, a small GTPase involved in trafficking, is somehow involved in Akt activation and that this step may be important for the acquisition of metastatic capacity.");

        annotator.annotate(jCas, testTopic);

        final Collection<Disease> diseases = JCasUtil.select(jCas, Disease.class);
        assertThat(diseases).isNotNull().isNotEmpty();
        final List<Disease> topicMatchedDiseases = diseases.stream().filter(d -> d.getComponentId().equals(TopicFieldsCasAnnotator.class.getCanonicalName())).collect(Collectors.toList());
        assertThat(topicMatchedDiseases).hasSize(8);
        for (Disease d : topicMatchedDiseases) {
            assertThat(d.getSpecificType()).isIn(MatchType.DISEASE.name(), MatchType.DISEASE_HYPERNYM.name(), MatchType.DISEASE_SYNONYM.name());
            assertThat(d.getTextualRepresentation()).isIn("melanoma", "membran", "remodel");
            if (d.getTextualRepresentation().equals("melanoma"))
                assertThat(d.getSpecificType().equals(MatchType.DISEASE.name()));
            if (d.getTextualRepresentation().equals("membran"))
                assertThat(d.getSpecificType().equals(MatchType.DISEASE_SYNONYM.name()));
            if (d.getTextualRepresentation().equals("remodeling"))
                assertThat(d.getSpecificType().equals(MatchType.DISEASE_HYPERNYM.name()));
        }

        final Collection<Gene> genes = JCasUtil.select(jCas, Gene.class);
        assertThat(genes).isNotNull().isNotEmpty();
        final List<Gene> topicMatchedGenes = genes.stream().filter(g -> g.getComponentId().equals(TopicFieldsCasAnnotator.class.getCanonicalName())).collect(Collectors.toList());
        for (Gene g : topicMatchedGenes) {
            assertThat(g.getSpecificType()).isIn(MatchType.GENE.name(), MatchType.GENE_AND_VARIANT.name(), MatchType.VARIANT.name());
            assertThat(g.getTextualRepresentation()).isIn("v600e", "braf", "brafv600");
            if (g.getTextualRepresentation().equals("braf"))
                assertThat(g.getSpecificType()).isEqualTo(MatchType.GENE.name());
            if (g.getTextualRepresentation().equals("brafv600"))
                assertThat(g.getSpecificType()).isEqualTo(MatchType.GENE_AND_VARIANT.name());
            if (g.getTextualRepresentation().equals("v600e"))
                assertThat(g.getSpecificType()).isEqualTo(MatchType.VARIANT.name());
        }
    }
}
