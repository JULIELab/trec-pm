package de.julielab.ir.ltr;

import de.julielab.ir.ltr.features.IRScore;
import de.julielab.ir.model.QueryDescription;

import java.io.File;
import java.io.IOException;

public class TreatmentRanker<Q extends QueryDescription> implements Ranker<Q> {
    private IRScore outputScoreType = IRScore.TREATMENT;

    @Override
    public void train(DocumentList<Q> documents) {
        // NOOP
    }

    @Override
    public void load(File modelFile) throws IOException {
        // NOOP
    }

    @Override
    public void save(File modelFile) {
        // NOOP
    }

    @Override
    public DocumentList<Q> rank(DocumentList<Q> documents) {
        DocumentList<Q> ret = new DocumentList<>();
        for (Document<Q> document : documents) {
            // Only add documents with treatments
            if (document.getTreatments().size() == 0) {
                continue;
            }

            // Copy BM25 score
            document.setScore(outputScoreType, document.getIrScore(IRScore.BM25));

            ret.add(document);
        }
        return ret;
    }

    @Override
    public IRScore getOutputScoreType() {
        // TODO pull up
        return outputScoreType;
    }

    @Override
    public void setOutputScoreType(IRScore outputScoreType) {
        // TODO pull up
        this.outputScoreType = outputScoreType;
    }
}
