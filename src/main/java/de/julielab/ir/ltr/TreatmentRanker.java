package de.julielab.ir.ltr;

import de.julielab.ir.ltr.features.IRScore;
import de.julielab.ir.model.QueryDescription;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TreatmentRanker<Q extends QueryDescription> implements Ranker<Q> {
    private static final File STOPLIST_FILE = new File(TreatmentRanker.class.getResource("/treatment-stoplist.txt").getFile());
    private List<String> stoplist;

    private IRScore outputScoreType = IRScore.TREATMENT;

    private static List<String> loadStoplist(File file) {
        try {
            return FileUtils.readLines(file, "UTF-8");
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read stoplist file.");
        }
    }

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
            List<String> treatments = document.getTreatments();
            treatments = stoplistFilter(treatments);

            // Only add documents if there are any remaining treatments
            if (treatments.size() == 0) {
                continue;
            }

            // Copy BM25 score
            document.setScore(outputScoreType, document.getIrScore(IRScore.BM25));
            document.setTreatments(treatments);

            ret.add(document);
        }
        return ret;
    }

    private List<String> stoplistFilter(List<String> treatments) {
        // Lazy initialization
        if (stoplist == null) {
            this.stoplist = loadStoplist(STOPLIST_FILE);
        }

        List<String> ret = new ArrayList<>();
        for (String treatment : treatments) {
            if (!stoplist.contains(treatment)) {
                ret.add(treatment);
            }
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
