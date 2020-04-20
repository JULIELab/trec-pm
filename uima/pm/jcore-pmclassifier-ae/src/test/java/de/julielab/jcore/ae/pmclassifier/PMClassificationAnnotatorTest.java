package de.julielab.jcore.ae.pmclassifier;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.InvalidXMLException;
import org.junit.Test;

import java.io.IOException;

public class PMClassificationAnnotatorTest {
    @Test
    public void test() throws ResourceInitializationException, IOException, InvalidXMLException {
        AnalysisEngine engine = AnalysisEngineFactory.createEngine("de.julielab.jcore.ae.pmclassifier.desc.jcore-pmclassifier-ae", PMClassificationAnnotator.PARAM_PM_MODEL, "/malletPmClassifier.mod.gz");
    }
}
