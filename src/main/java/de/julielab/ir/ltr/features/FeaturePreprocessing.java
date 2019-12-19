package de.julielab.ir.ltr.features;

import at.medunigraz.imi.bst.retrieval.Retrieval;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import com.wcohen.ss.TFIDF;
import de.julielab.ir.OriginalDocumentRetrieval;
import de.julielab.ir.TfIdfManager;
import de.julielab.ir.VocabularyRestrictor;
import de.julielab.ir.ltr.DocumentList;
import de.julielab.ir.ltr.features.featuregroups.TfidfFeatureGroup;
import de.julielab.ir.model.QueryDescription;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class FeaturePreprocessing<Q extends QueryDescription> {
    private static final Logger log = LogManager.getLogger();
    private final String xmiTableName;

    private String docIdIndexField;
    private int vocabularyCutoff;
    private Map<IRScoreFeatureKey, Retrieval<?, Q>> retrievals;

    private List<File> canonicalDbConnectionFiles;

    public FeaturePreprocessing(String docIdIndexField, int vocabularyCutoff, String xmiTableName) {
        this.docIdIndexField = docIdIndexField;
        this.vocabularyCutoff = vocabularyCutoff;
        this.xmiTableName = xmiTableName;
    }

    public void setRetrievals(LinkedHashMap<IRScoreFeatureKey, Retrieval<?, Q>> retrievals) {
        this.retrievals = retrievals;
    }

    public void preprocessTrain(DocumentList<Q> trainDocs, String runId) {
        if (FeatureControlCenter.getInstance().isSimilarityFeatureGroupActive()) {
            for (IRScoreFeatureKey featureKey : retrievals.keySet())
                retrievals.get(featureKey).setIrScoresToDocuments(trainDocs, docIdIndexField, featureKey);
        }
        Set<String> tfIdfVocabulary = null;
        TFIDF trainTfIdf = null;
        if (FeatureControlCenter.getInstance().isTfIdfActive()) {
            log.debug("Learning TFIDF from training documents.");
            Pair<TFIDF, Set<String>> tfidfAndVocab = learnTfidf(trainDocs, runId);
            trainTfIdf = tfidfAndVocab.getLeft();
            tfIdfVocabulary = tfidfAndVocab.getRight();
        }
        FeatureControlCenter.getInstance().createFeatures(trainDocs, trainTfIdf, tfIdfVocabulary, xmiTableName);
    }

    private Pair<TFIDF, Set<String>> learnTfidf(DocumentList<Q> trainDocs, String runId) {
        TFIDF trainTfIdf;
        final List<String> trainDocumentText = OriginalDocumentRetrieval.getInstance().getDocumentText(trainDocs.getSubsetWithUniqueDocumentIds(), xmiTableName).collect(Collectors.toList());
        String vocabularyId = getTfIdfId(runId);
        Set<String> tfIdfVocabulary = VocabularyRestrictor.getInstance().calculateVocabulary(vocabularyId, trainDocumentText.stream(), VocabularyRestrictor.Restriction.TFIDF, vocabularyCutoff);
        if (!TfIdfManager.getInstance().hasModelForKey(vocabularyId))
            trainTfIdf = TfIdfManager.getInstance().trainAndSetTfIdf(vocabularyId, trainDocumentText.stream());
        else
            trainTfIdf = TfIdfManager.getInstance().getTrainedTfIdf(vocabularyId);
        return new ImmutablePair<>(trainTfIdf, tfIdfVocabulary);
    }

    @NotNull
    private String getTfIdfId(String runId) {
        String vocabularyId = "DB:" + OriginalDocumentRetrieval.getInstance().getDatabaseUrl() + "-Cutoff:" + vocabularyCutoff;
        if (runId != null && !runId.isBlank())
            vocabularyId += "-Run:" + runId;
        return vocabularyId;
    }

    public void preprocessTest(DocumentList<Q> testDocs, Pipe trainPipe, DocumentList<Q> trainDocs, String runId) {
        if (FeatureControlCenter.getInstance().isSimilarityFeatureGroupActive()) {
            for (IRScoreFeatureKey featureKey : retrievals.keySet())
                retrievals.get(featureKey).setIrScoresToDocuments(testDocs, docIdIndexField, featureKey);
        }

        TFIDF trainTfIdf;
        if (FeatureControlCenter.getInstance().isTfIdfActive()) {
            final String tfIdfId = getTfIdfId(runId);
            if (TfIdfManager.getInstance().hasModelForKey(tfIdfId))
                trainTfIdf = TfIdfManager.getInstance().getTrainedTfIdf(tfIdfId);
            else {
                log.debug("Training TFIDF from training documents");
                trainTfIdf = learnTfidf(trainDocs, runId).getLeft();
            }
            Optional<Pipe> tfIdfFgOpt = ((SerialPipes) trainPipe).pipes().stream().filter(TfidfFeatureGroup.class::isInstance).findAny();
            if (!tfIdfFgOpt.isPresent())
                throw new IllegalStateException("TFIDF vocabulary feature is active but the given training pipe does not contain the TfidfFeatureGroup.");
            else
                ((TfidfFeatureGroup) tfIdfFgOpt.get()).setTfidf(trainTfIdf);
        }
        FeatureControlCenter.getInstance().createFeatures(testDocs, trainPipe, canonicalDbConnectionFiles, xmiTableName);
    }

    public void setCanonicalDbConnectionFiles(List<File> canonicalDbConnectionFiles) {
        this.canonicalDbConnectionFiles = canonicalDbConnectionFiles;
    }
}
