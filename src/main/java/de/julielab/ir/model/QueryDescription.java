package de.julielab.ir.model;

import at.medunigraz.imi.bst.trec.model.Challenge;
import de.julielab.ir.goldstandards.AtomicGoldStandard;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A query from a dataset for which exist relevance-labeled result documents for evaluation.
 */
public abstract class QueryDescription {
    protected Challenge challenge;
    protected int year;
    protected int number;
    protected Set<String> appliedDecorators;

    public QueryDescription() {
        this.appliedDecorators = new LinkedHashSet<>();
    }

    public QueryDescription(Challenge challenge, int year, int number) {
        this();
        this.challenge = challenge;
        this.year = year;
        this.number = number;
    }

    public void addAppliedDecorator(String uniqueDecoratorName) {
        if (!appliedDecorators.add(uniqueDecoratorName))
            throw new IllegalArgumentException("The query decorator with name '" + uniqueDecoratorName + "' has already been applied to this query.");
    }

    public boolean hasDecoraterBeenApplied(String uniqueDecoratorName) {
        return appliedDecorators.contains(uniqueDecoratorName);
    }

    public Challenge getChallenge() {
        return challenge;
    }

    public void setChallenge(Challenge challenge) {
        this.challenge = challenge;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QueryDescription query = (QueryDescription) o;
        return year == query.year &&
                number == query.number &&
                challenge == query.challenge;
    }

    @Override
    public int hashCode() {

        return Objects.hash(challenge, year, number);
    }

    /**
     * <p>
     * Returns 'challenge-year-number'.
     * </p>
     * <p>this method is compatible with {@link AtomicGoldStandard#getDatasetId()} in the sense that that a topic
     * belonging to a specific gold standard always has a cross dataset ID that begins with the ID of its dataset
     * as given by the gold standard dataset ID method.</p>
     *
     * @return A string including the challenge, the year and the topic number for this topic, excluding null elements.
     */
    public String getCrossDatasetId() {
        return Stream.of(challenge, year, number).filter(Objects::nonNull).map(String::valueOf).collect(Collectors.joining("-"));
    }

    /**
     * <p>Returns a structured description of the original gold standard query as much as structure was defined
     * by the gold standard authors.</p>
     * <p>For queries consisting of well defined fields or query parts, those parts are returned, like
     * <tt>the disease to be queried</tt> or <tt>the gene of the topic</tt>.</p>
     *
     * @return The query attributes.
     */
    public abstract Map<String, String> getAttributes();

    /**
     * Returns a copy of this query description without any expansions or other modifications.
     * @return An copy of this description without modifications.
     */
    public abstract QueryDescription getCleanCopy();
}
