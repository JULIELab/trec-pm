package de.julielab.jcore.ae.pmclassifier;

import at.medunigraz.imi.bst.pmclassifier.Document;
import at.medunigraz.imi.bst.pmclassifier.MalletClassifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.julielab.jcore.types.*;
import de.julielab.jcore.utility.JCoReTools;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

@TypeCapability(inputs = {"de.julielab.jcore.types.Gene", "de.julielab.jcore.types.Organism", "de.julielab.jcore.types.Title", "de.julielab.jcore.types.AbstractText", "de.julielab.jcore.types.MeshHeading"})
public class PMClassificationAnnotator extends JCasAnnotator_ImplBase {
    public static final String PARAM_PM_MODEL = "PmModel";
    private final static Logger log = LoggerFactory.getLogger(PMClassificationAnnotator.class);
    private ObjectMapper om;
    @ConfigurationParameter(name = PARAM_PM_MODEL)
    private String pmModel;
    private MalletClassifier mc;

    private static Document createClassifierInput(JCas aJCas) {
        Collection<Title> titles = JCasUtil.select(aJCas, Title.class);
        Collection<AbstractText> abstractTexts = JCasUtil.select(aJCas, AbstractText.class);
        Collection<MeshHeading> meshHeadings = JCasUtil.select(aJCas, MeshHeading.class);
        Collection<Gene> genes = JCasUtil.select(aJCas, Gene.class);
        Collection<Organism> organisms = JCasUtil.select(aJCas, Organism.class);

        String title = titles.stream().map(Annotation::getCoveredText).collect(Collectors.joining(" "));
        String abstractText = abstractTexts.stream().map(Annotation::getCoveredText).collect(Collectors.joining(" "));
        List<String> allMesh = meshHeadings.stream().map(h -> h.getDescriptorName()).collect(Collectors.toList());
        List<String> meshMajor = meshHeadings.stream().filter(h -> h.getDescriptorNameMajorTopic()).map(h -> h.getDescriptorName()).collect(Collectors.toList());
        List<String> meshMinor = meshHeadings.stream().filter(h -> !h.getDescriptorNameMajorTopic()).map(h -> h.getDescriptorName()).collect(Collectors.toList());
        List<String> geneNames = genes.stream().map(Annotation::getCoveredText).map(String::toLowerCase).collect(Collectors.toList());
        List<String> organismNames = organisms.stream().map(Annotation::getCoveredText).map(String::toLowerCase).collect(Collectors.toList());

        Document document = new Document();
        document.setTitle(title);
        document.setAbstractText(abstractText);
        document.setMeshTags(allMesh);
        document.setMeshMinor(meshMinor);
        document.setMeshTagsMajor(meshMajor);
        document.setGenes(geneNames);
        document.setOrganisms(organismNames);

        return document;
    }

    /**
     * This method is called a single time by the framework at component
     * creation. Here, descriptor parameters are read and initial setup is done.
     */
    @Override
    public void initialize(final UimaContext aContext) throws ResourceInitializationException {
        if (log.isInfoEnabled()) {
            log.info("Component configuration:");
            for (String configName : aContext.getConfigParameterNames())
                log.info("{}: {}", configName, aContext.getConfigParameterValue(configName));
        }
        pmModel = (String) aContext.getConfigParameterValue(PARAM_PM_MODEL);
        mc = new MalletClassifier();
        try {
            mc.readClassifier(pmModel);
        } catch (IOException | ClassNotFoundException e) {
            throw new ResourceInitializationException(e);
        }

        om = new ObjectMapper();
    }

    /**
     * This method is called for each document going through the component. This
     * is where the actual work happens.
     */
    @Override
    public void process(final JCas aJCas) throws AnalysisEngineProcessException {
        Document classifierInput = createClassifierInput(aJCas);
        final double probabiltyForPM = mc.predictProbabiltyForPM(classifierInput);
        AutoDescriptor ad;
        try {
            ad = JCasUtil.selectSingle(aJCas, AutoDescriptor.class);
        } catch (IllegalArgumentException e) {
            ad = new AutoDescriptor(aJCas);
            ad.addToIndexes();
        }
        DocumentClass documentClass = new DocumentClass(aJCas);
        documentClass.setClassname(probabiltyForPM > .5 ? "PM" : "Not PM");
        FSArray newArray = JCoReTools.addToFSArray(ad.getDocumentClasses(), documentClass);
        ad.setDocumentClasses(newArray);
    }

}
