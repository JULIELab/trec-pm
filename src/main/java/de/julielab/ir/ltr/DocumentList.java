package de.julielab.ir.ltr;

import at.medunigraz.imi.bst.trec.model.Result;
import at.medunigraz.imi.bst.trec.model.ResultList;
import de.julielab.ir.ltr.features.IRScore;
import de.julielab.ir.model.QueryDescription;

import java.util.*;

/**
 * A list of documents they either represent labeled data for training or unlabeled data to be ranked.
 */
public class DocumentList<Q extends QueryDescription> extends ArrayList<Document<Q>> {
    public DocumentList(Collection<? extends Document<Q>> c) {
        super(c);
    }

    public DocumentList() {
    }

    public static <Q extends QueryDescription> DocumentList<Q> fromRetrievalResultList(ResultList<Q> list) {
        final DocumentList<Q> documents = new DocumentList<>();
        for (Result r : list.getResults()) {
            final Document<Q> doc = new Document<>();
            doc.setId(r.getId());
            doc.setScore(IRScore.BM25, r.getScore());
            doc.setQueryDescription(list.getTopic());
            documents.add(doc);
        }
        return documents;
    }

    public DocumentList<Q> getSubsetWithUniqueDocumentIds() {
        Set<String> seenIds = new LinkedHashSet<>();
        final DocumentList<Q> ret = new DocumentList<>();
        for (Document doc : this) {
            if (seenIds.add(doc.getId()))
                ret.add(doc);
        }
        return ret;
    }

    public DocumentList<Q> getSubsetWithUniqueTopicDocumentIds() {
        Set<String> seenIds = new LinkedHashSet<>();
        final DocumentList<Q> ret = new DocumentList<>();
        for (Document doc : this) {
            if (seenIds.add(doc.getQueryDescription().getNumber() + "-" + doc.getId())) {
                ret.add(doc);
            }
        }
        return ret;
    }

    public void sortByScore(IRScore score) {
        Collections.sort(this, (d1, d2) -> Double.compare(d2.getIrScore(score), d1.getIrScore(score)));
    }
}
