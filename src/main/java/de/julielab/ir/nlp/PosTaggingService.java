package de.julielab.ir.nlp;

import de.julielab.jcore.types.Sentence;
import de.julielab.jcore.types.Token;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.stream.Collectors;

public class PosTaggingService {
    private final static Logger log = LoggerFactory.getLogger(PosTaggingService.class);
    private static PosTaggingService instance;
    private AnalysisEngine tokenizer;
    private AnalysisEngine posTagger;
    private AnalysisEngine lemmatizer;
    private JCas jCas;

    private PosTaggingService() {
        try {
            jCas = JCasFactory.createJCas("de.julielab.jcore.types.jcore-morpho-syntax-types");
            tokenizer = AnalysisEngineFactory.createEngine("de.julielab.jcore.ae.jtbd.desc.jcore-jtbd-ae-biomedical-english");
            posTagger = AnalysisEngineFactory.createEngine("de.julielab.jcore.ae.opennlp.postag.desc.jcore-opennlp-postag-ae-biomedical-english");
            lemmatizer = AnalysisEngineFactory.createEngine("de.julielab.jcore.ae.biolemmatizer.desc.jcore-biolemmatizer-ae");
        } catch (UIMAException | IOException e) {
            log.error("Could not initialize PosTaggingService", e);
            throw new IllegalStateException(e);
        }
    }

    public static PosTaggingService getInstance() {
        if (instance == null)
            instance = new PosTaggingService();
        return instance;
    }

    public NLPSentence tag(String sentence) {
        jCas.setDocumentText(sentence);
        try {
            new Sentence(jCas, 0, sentence.length()).addToIndexes();
            tokenizer.process(jCas);
            posTagger.process(jCas);
            lemmatizer.process(jCas);
            return JCasUtil.select(jCas, Token.class).stream().map(t -> new NLPToken(t.getCoveredText(), t.getLemma() != null ? t.getLemma().getValue() : null, t.getPosTag(0).getValue())).collect(Collectors.toCollection(NLPSentence::new));
        } catch (AnalysisEngineProcessException e) {
            log.error("Could not PoS-tag sentence {}", sentence, e);
            throw new IllegalStateException(e);
        } finally {
            jCas.reset();
        }
    }
}
