package de.julielab.ir.ltr.features;

import at.medunigraz.imi.bst.retrieval.Retrieval;
import com.wcohen.ss.TFIDF;
import de.julielab.ir.OriginalDocumentRetrieval;
import de.julielab.ir.TfIdfManager;
import de.julielab.ir.VocabularyRestrictor;
import de.julielab.ir.ltr.DocumentList;
import de.julielab.ir.model.QueryDescription;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class FeaturePreprocessing<Q extends QueryDescription> {
    private final String xmiTableName;

    private String docIdIndexField;
    private int vocabularyCutoff;
    private Map<IRScoreFeatureKey, Retrieval<?, Q>> retrievals;

    public FeaturePreprocessing(String docIdIndexField, int vocabularyCutoff, String xmiTableName) {
        this.docIdIndexField = docIdIndexField;
        this.vocabularyCutoff = vocabularyCutoff;
        this.xmiTableName = xmiTableName;
    }

    public void setRetrievals(Map<IRScoreFeatureKey, Retrieval<?, Q>> retrievals) {
        this.retrievals = retrievals;
    }

    public void preprocessTrain(DocumentList<Q> trainDocs, String runId) {
        for (IRScoreFeatureKey featureKey : retrievals.keySet())
            retrievals.get(featureKey).setIrScoresToDocuments(trainDocs, docIdIndexField, featureKey);
        Set<String> tfIdfVocabulary = null;
        TFIDF trainTfIdf = null;
        if (FeatureControlCenter.getInstance().isTfIdfActive()) {
            final List<String> trainDocumentText = OriginalDocumentRetrieval.getInstance().getDocumentText(trainDocs.getSubsetWithUniqueDocumentIds(), xmiTableName).collect(Collectors.toList());
            String vocabularyId = getTfIdfId(runId);
            tfIdfVocabulary = VocabularyRestrictor.getInstance().calculateVocabulary(vocabularyId, trainDocumentText.stream(), VocabularyRestrictor.Restriction.TFIDF, vocabularyCutoff);
            if (!TfIdfManager.getInstance().hasModelForKey(vocabularyId))
                trainTfIdf = TfIdfManager.getInstance().trainAndSetTfIdf(vocabularyId, trainDocumentText.stream());
        }
        FeatureControlCenter.getInstance().createFeatures(trainDocs, trainTfIdf, tfIdfVocabulary, xmiTableName);
    }

    @NotNull
    private String getTfIdfId(String runId) {
        String vocabularyId = "DB:" + OriginalDocumentRetrieval.getInstance().getDatabaseUrl() + "-Cutoff:" + vocabularyCutoff;
        if (runId != null && !runId.isBlank())
            vocabularyId += "-Run:" + runId;
        return vocabularyId;
    }

    public void preprocessTest(DocumentList<Q> testDocs, String runId) {
        for (IRScoreFeatureKey featureKey : retrievals.keySet())
            retrievals.get(featureKey).setIrScoresToDocuments(testDocs, docIdIndexField, featureKey);

        Set<String> tfIdfVocabulary = null;
        TFIDF trainTfIdf = null;
        if (FeatureControlCenter.getInstance().isTfIdfActive()) {
            final String tfIdfId = getTfIdfId(runId);
            tfIdfVocabulary = VocabularyRestrictor.getInstance().getVocabulary(tfIdfId);
            trainTfIdf = TfIdfManager.getInstance().getTrainedTfIdf(tfIdfId);
        }
        FeatureControlCenter.getInstance().createFeatures(testDocs, trainTfIdf, tfIdfVocabulary, xmiTableName);
    }
}
