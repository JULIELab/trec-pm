package at.medunigraz.imi.bst;

import de.julielab.ir.model.QueryDescription;
import de.julielab.ir.model.QueryDescriptionAttribute;

import java.util.Arrays;
import java.util.List;

public class TestTopic extends QueryDescription {
    @QueryDescriptionAttribute
    private String query;
    @QueryDescriptionAttribute
    private List<String> stopFilteredTermList;
    @QueryDescriptionAttribute
    private String[] stopFilteredTermArray;
    @QueryDescriptionAttribute
    private double weight;

    public String getQuery() {
        return query;
    }

    public TestTopic withQuery(String query) {
        this.query = query;
        return this;
    }

    public TestTopic withStopFilteredTermList(String... words) {
        stopFilteredTermList = Arrays.asList(words);
        return this;
    }

    public TestTopic withStopFilteredTermArray(String... words) {
        stopFilteredTermArray = words;
        return this;
    }


    @Override
    public TestTopic getCleanCopy() {
        return new TestTopic().withQuery(query);
    }

    public TestTopic withWeight(double weight) {
        this.weight = weight;
        return this;
    }
}
