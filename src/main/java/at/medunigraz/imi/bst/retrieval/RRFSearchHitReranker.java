package at.medunigraz.imi.bst.retrieval;

import at.medunigraz.imi.bst.trec.model.Result;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RRFSearchHitReranker implements SearchHitReranker{
    private Function<Result, String> docIdFunction = r -> r.getId();
    private static final int k = 60;

    @Override
    public List<Result> rerank(Stream<Result> originalSearchHits) {
        Map<String, List<Result>> index2hit = originalSearchHits.collect(Collectors.groupingBy(Result::getIndex));
        Map<String, Map<String, Integer>> rankLists = new HashMap<>();
        for (String key : index2hit.keySet()) {
            Map<String, Integer> rankList = rankLists.compute(key, (k, v) -> v != null ? v : new HashMap<>());
            List<Result> hits4key = index2hit.get(key);
            for (int i = 0; i < hits4key.size(); ++i) {
                rankList.put(docIdFunction.apply(hits4key.get(i)), i);
            }
        }
        List<Result> newRanking = index2hit.keySet().stream().map(index2hit::get).flatMap(Collection::stream).collect(Collectors.toList());
        Collections.sort(newRanking, (h1,h2)->{
            String id1 = docIdFunction.apply(h1);
            String id2 = docIdFunction.apply(h2);
            double h1score = rankLists.keySet().stream().map(key -> rankLists.get(key).get(id1)).filter(Objects::nonNull).mapToDouble(rank -> 1d / (k + rank)).sum();
            double h2score = rankLists.keySet().stream().map(key -> rankLists.get(key).get(id2)).filter(Objects::nonNull).mapToDouble(rank -> 1d / (k + rank)).sum();
            return Double.compare(h2score, h1score);
        });
        return newRanking;
    }

    @Override
    public void setDocumentIdFunction(Function<Result, String> docIdFunction) {
        this.docIdFunction = docIdFunction;
    }
}
