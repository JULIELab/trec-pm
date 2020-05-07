package at.medunigraz.imi.bst.retrieval;

import at.medunigraz.imi.bst.trec.model.Result;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Takes search hits retrieved from ElasticSearch and rearranges them into a new list.
 */
public interface SearchHitReranker {
    List<Result> rerank(Stream<Result> originalSearchHits);

    void setDocumentIdFunction(Function<Result, String> docIdFunction);
}
