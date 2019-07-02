package de.julielab.ir.ltr;

import de.julielab.ir.model.QueryDescription;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * A list of documents they either represent labeled data for training or unlabeled data to be ranked.
 */
public class DocumentList<Q extends QueryDescription> extends ArrayList<Document<Q>> {
    public DocumentList<Q> getSubsetWithUniqueDocumentIds() {
        Set<String> seenIds = new HashSet<>();
        final DocumentList<Q> ret = new DocumentList<>();
        for (Document doc : this) {
            if (seenIds.add(doc.getId()))
                ret.add(doc);
        }
        return ret;
    }
}
