package at.medunigraz.imi.bst.retrieval;

import at.medunigraz.imi.bst.trec.model.Result;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
public class RRFSearchHitRerankerTest {

    @Test
    public void rerank() {
        // trivial test to check the most simple case - a single list, nothing should change.
        List<Result> simpleList = new ArrayList<>();
        int i = 0;
        simpleList.add(new Result("2", "index1", 0d));
        simpleList.add(new Result("3", "index1", 0d));
        simpleList.add(new Result("1", "index1", 0d));

        RRFSearchHitReranker reranker = new RRFSearchHitReranker();
        List<Result> newlist = reranker.rerank(simpleList.stream());
        assertThat(newlist).isEqualTo(simpleList);
    }

    @Test
    public void rerank2() {
        List<Result> simpleList = new ArrayList<>();
        int i = 0;
        simpleList.add(new Result("1", "index1", 0d));
        simpleList.add(new Result("2", "index1", 0d));
        simpleList.add(new Result("3", "index1", 0d));
        simpleList.add(new Result("3", "index2", 0d));
        simpleList.add(new Result("2", "index2", 0d));
        simpleList.add(new Result("1", "index2", 0d));

        RRFSearchHitReranker reranker = new RRFSearchHitReranker();
        List<Result> newlist = reranker.rerank(simpleList.stream());
        assertThat(newlist).extracting(Result::getId).containsExactly("1", "3", "3", "1", "2", "2");
    }


}