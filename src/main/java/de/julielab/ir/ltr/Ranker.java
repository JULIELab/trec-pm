package de.julielab.ir.ltr;

import de.julielab.ir.model.QueryDescription;

import java.io.File;
import java.io.IOException;

/**
 * Abstraction layer for all algorithms that rank a list of documents.
 */
public interface Ranker<Q extends QueryDescription> {

    /**
     * <p>Learns a ranker from the input documents.</p>
     * <p>The input documents are not expected to be in a specific order. All documents of a gold standard, including
     * all different queries, are to be passed here.</p>
     * @param documents The training documents.
     */
    void train(DocumentList<Q> documents);

    void load(File modelFile) throws IOException;

    void save(File modelFile);

    /**
     * <p>Ranks the input documents by creating a new DocumentList and filling it in the new ranking order.</p>
     * <p>The input documents are not expected to be ordered in any way. Multiple query results may be
     * merged in the single list.</p>
     * <p>The output list will be grouped by query ID. Within each query ID group, the documents will have the order
     * the ranker produced. The order of the query IDs is not defined.</p>
     * @param documents The documents to rerank.
     * @return The reranked result list.
     */
    DocumentList<Q> rank(DocumentList<Q> documents);

}
