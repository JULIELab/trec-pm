package at.medunigraz.imi.bst.retrieval;

import at.medunigraz.imi.bst.trec.model.Result;
import at.medunigraz.imi.bst.trec.model.ResultList;
import de.julielab.ir.model.QueryDescription;

import java.util.*;
import java.util.function.Function;

public class RRFResultListFusion implements ResultListFusion {
    private Function<Result, String> docIdFunction;

    public RRFResultListFusion() {
    }

    public RRFResultListFusion(Function<Result, String> docIdFunction) {
        this.docIdFunction = docIdFunction;
    }

    @Override
    public <Q extends QueryDescription> List<ResultList<Q>> fuseMultipleTopics(List<List<ResultList<Q>>> resultlistLists) {
        // Organize the result lists by topic. We will then fuse the lists for each topic together, leaving us with
        // one result list per topic.
        Map<Q, List<ResultList<Q>>> topic2lists = new HashMap<>();
        for (List<ResultList<Q>> resultLists : resultlistLists) {
            for (ResultList<Q> resultList : resultLists)
                topic2lists.compute(resultList.getTopic(), (k, v) -> v != null ? v : new ArrayList<>()).add(resultList);
        }
        List<ResultList<Q>> ret = new ArrayList<>(topic2lists.size());
        for (QueryDescription q : topic2lists.keySet()) {
            ResultList<Q> fusedList4Topic = fuse(topic2lists.get(q));
            ret.add(fusedList4Topic);
        }
        return ret;
    }

    @Override
    public <Q extends QueryDescription> ResultList<Q> fuse(List<ResultList<Q>> resultlists) {
        if (resultlists.stream().map(ResultList::getTopic).distinct().count() > 1)
            throw new IllegalArgumentException("Trying to merge result lists for different topics.");
        List<Map<String, Double>> rankScoreMaps = new ArrayList<>();
        Map<String, Result> retrievedDocIds = new HashMap<>();
        // Compute the reciprocal rank scores
        for (ResultList<Q> list : resultlists) {
            Map<String, Double> reciprocalScores = new HashMap<>();
            rankScoreMaps.add(reciprocalScores);
            List<Result> results = list.getResults();
            for (int i = 0; i < results.size(); i++) {
                Result result = results.get(i);
                String docId = docIdFunction != null ? docIdFunction.apply(result) : result.getId();
                double score = 1d / (60 + i);
                reciprocalScores.put(docId, score);
                // this will override the value for a specific docId most of the time. We are just interested
                // in retaining any one result object for each doc ID.
                retrievedDocIds.put(docId, result);
            }
        }
        // Fuse the lists by effectively creating a new one
        ResultList<Q> fusedList = new ResultList<>(resultlists.get(0).getTopic(), resultlists.stream().mapToInt(rl -> rl.getResults().size()).max().getAsInt());
        for (String docId : retrievedDocIds.keySet()) {
            double score = 0;
            for (Map<String, Double> reciprocalScores : rankScoreMaps) {
                Double scoreFromList = reciprocalScores.get(docId);
                if (scoreFromList != null)
                    score += scoreFromList;
            }
            Result result = retrievedDocIds.get(docId);
            Result resultClone = result.clone();
            resultClone.setScore(score);
            resultClone.setIndex(null);
            fusedList.add(resultClone);
        }
        Collections.sort(fusedList.getResults(), Comparator.comparingDouble(Result::getScore).reversed());
        return fusedList;
    }
}
