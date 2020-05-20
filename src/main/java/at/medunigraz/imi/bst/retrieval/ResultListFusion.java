package at.medunigraz.imi.bst.retrieval;

import at.medunigraz.imi.bst.trec.model.ResultList;
import de.julielab.ir.model.QueryDescription;

import java.util.List;

/**
 * Fuses multiple result lists into one.
 */
public interface ResultListFusion {
    <Q extends QueryDescription> List<ResultList<Q>> fuseMultipleTopics(List<List<ResultList<Q>>> resultlistLists);
    <Q extends QueryDescription> ResultList<Q> fuse(List<ResultList<Q>> resultlists);
}
