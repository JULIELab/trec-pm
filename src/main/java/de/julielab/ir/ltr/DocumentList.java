package de.julielab.ir.ltr;

import at.medunigraz.imi.bst.trec.model.Result;
import at.medunigraz.imi.bst.trec.model.ResultList;
import de.julielab.ir.ltr.features.IRScore;
import de.julielab.ir.ltr.features.IRScoreFeatureKey;
import de.julielab.ir.ltr.features.TrecPmQueryPart;
import de.julielab.ir.model.QueryDescription;

import java.util.*;
import java.util.stream.Collectors;

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
            doc.setScore(new IRScoreFeatureKey(IRScore.BM25, TrecPmQueryPart.FULL), r.getScore());
            doc.setSourceFields(r.getSourceFields());
            doc.setTreatments(r.getTreatments());
            doc.setQueryDescription(list.getTopic());
            documents.add(doc);
        }
        return documents;
    }

    public DocumentList<Q> getQuerySubset(Collection<Q> queries) {
        Set<String> queryIds = queries.stream().map(QueryDescription::getCrossDatasetId).collect(Collectors.toSet());
        return stream().filter(d -> queryIds.contains(d.getQueryDescription().getCrossDatasetId())).collect(Collectors.toCollection(DocumentList::new));
    }

    /**
     * <p>Returns each text document of this list exactly once.</p>
     * <p>Since a {@link Document} foremost represents a query-document pair, an original text document might
     * appear for multiple queries. This method serves to get a list of documents such that each text document
     * ID present in this list appears exactly once in the return value.</p>
     * @return A reduced <code>DocumentList</code> such that each underlying text document is represented exactly once.
     */
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

    public void sortByScore(IRScoreFeatureKey score) {
        Collections.sort(this, (d1, d2) -> Double.compare(d2.getIrScore(score), d1.getIrScore(score)));
    }
}
