package de.julielab.ir.model;

import at.medunigraz.imi.bst.trec.model.Challenge;
import de.julielab.ir.goldstandards.AtomicGoldStandard;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A query from a dataset for which exist relevance-labeled result documents for evaluation.
 */
public abstract class QueryDescription {
    protected Challenge challenge;
    protected int year;
    @QueryDescriptionAttribute
    protected int number;
    protected String index;

    public QueryDescription(Challenge challenge, int year, int number) {
        this();
        this.challenge = challenge;
        this.year = year;
        this.number = number;
    }

    public QueryDescription() {

    }

    /**
     * The index in which to search this query.
     *
     * @return The index with the data for this query.
     */
    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
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

    public <Q extends QueryDescription> Q withNumber(int number) {
        this.number = number;
        return (Q) this;
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
    public Map<String, Object> getAttributes() {
        Map<String, Object> ret = new HashMap<>();
        Stream<Field> fields = Stream.concat(Arrays.stream(getClass().getDeclaredFields()), Arrays.stream(getClass().getSuperclass().getDeclaredFields()));
        for (Field f : (Iterable<Field>) () -> fields.iterator()) {
            QueryDescriptionAttribute annotation = f.getAnnotation(QueryDescriptionAttribute.class);
            try {
                if (annotation != null) {
                    f.setAccessible(true);
                    String fieldName = f.getName();
                    Object o = f.get(this);
                    ret.put(fieldName, o);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return ret;
    }

    /**
     * <p>Returns a structured description of the original gold standard query as much as structure was defined
     * by the gold standard authors.</p>
     * <p>For queries consisting of well defined fields or query parts, those parts are returned, like
     * <tt>the disease to be queried</tt> or <tt>the gene of the topic</tt>.
     * </p>
     * <p>This method returns all field values as String via {@link String#valueOf(Object)}.</p>
     *
     * @return The query attributes.
     */
    public Map<String, String> getFlattenedAttributes() {
        Map<String, String> ret = new HashMap<>();
        Stream<Field> fields = Stream.concat(Arrays.stream(getClass().getDeclaredFields()), Arrays.stream(getClass().getSuperclass().getDeclaredFields()));
        for (Field f : (Iterable<Field>) () -> fields.iterator()) {
            QueryDescriptionAttribute annotation = f.getAnnotation(QueryDescriptionAttribute.class);
            try {
                if (annotation != null) {
                    f.setAccessible(true);
                    Object o = f.get(this);
                    String fieldName = f.getName();
                    ret.put(fieldName, String.valueOf(o));
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return ret;
    }

    /**
     * Returns a copy of this query description without any expansions or other modifications.
     *
     * @return An copy of this description without modifications.
     */
    public abstract <Q extends QueryDescription> Q getCleanCopy();
}
