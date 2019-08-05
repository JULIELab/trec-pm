package de.julielab.ir.ltr;

import de.julielab.ir.ltr.features.IRScore;
import de.julielab.ir.ltr.features.IRScoreFeatureKey;
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
     *
     * @param documents The training documents.
     */
    void train(DocumentList<Q> documents);

    /**
     * <p>Learns a ranker from the input documents.</p>
     * <p>The input documents are not expected to be in a specific order. All documents of a gold standard, including
     * all different queries, are to be passed here.</p>
     * <p>When <tt>doValidation</tt> is set to <tt>true</tt>, a validation set is taken from the documents. The
     * separation occurs on <em>query</em> level. I.e. the documents will be grouped by query and then the groups
     * will be separated.</p>
     *
     * @param documents    The training documents.
     * @param doValidation Whether to split a fraction of documents for validation or not.
     * @param fraction     If <tt>doValidation</tt> is <tt>true</tt>, this defines the fraction of <em>queries</em> to be
     *                     used for training from the complete set of queries found in the given documents while
     *                     the rest is used for validation. Thus, this
     *                     parameter must be in [0,1).
     * @param randomSeed   The random seed used to draw validation queries from the documents.
     */
    void train(DocumentList<Q> documents, boolean doValidation, float fraction, int randomSeed);

    void load(File modelFile) throws IOException;

    void save(File modelFile);

    /**
     * <p>Ranks the input documents by creating a new DocumentList and filling it in the new ranking order.</p>
     * <p>The input documents are not expected to be ordered in any way. Multiple query results may be
     * merged in the single list.</p>
     * <p>The output list will be grouped by query ID. Within each query ID group, the documents will have the order
     * the ranker produced. The order of the query IDs is not defined.</p>
     *
     * @param documents The documents to rerank.
     * @return The reranked result list.
     */
    DocumentList<Q> rank(DocumentList<Q> documents);

    /**
     * Returns the score type that is set on ranked documents.
     *
     * @return
     */
    IRScoreFeatureKey getOutputScoreType();

    /**
     * Specifies the score type that is set on ranked documents.
     *
     * @param outputScoreType The score enum that should be set on the documents with the ranking score.
     *                        Can be used to override an existing score.
     */
    void setOutputScoreType(IRScoreFeatureKey outputScoreType);

}
