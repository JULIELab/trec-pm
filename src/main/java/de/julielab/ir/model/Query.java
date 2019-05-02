package de.julielab.ir.model;

import at.medunigraz.imi.bst.trec.model.Challenge;
import at.medunigraz.imi.bst.trec.model.Task;
import de.julielab.ir.goldstandards.AtomicGoldStandard;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A query from a dataset for which exist relevance-labeled result documents for evaluation.
 */
public class Query {
    protected Challenge challenge;
    protected Task task;
    protected int year;
    protected int number;

    public Query() {
    }

    public Query(Challenge challenge, Task task, int year, int number) {

        this.challenge = challenge;
        this.task = task;
        this.year = year;
        this.number = number;
    }

    public Challenge getChallenge() {
        return challenge;
    }

    public void setChallenge(Challenge challenge) {
        this.challenge = challenge;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
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
        Query query = (Query) o;
        return year == query.year &&
                number == query.number &&
                challenge == query.challenge &&
                task == query.task;
    }

    @Override
    public int hashCode() {

        return Objects.hash(challenge, task, year, number);
    }

    /**
     * <p>
     * Returns 'challenge-task-year-number'.
     * </p>
     * <p>this method is compatible with {@link AtomicGoldStandard#getDatasetId()} in the sense that that a topic
     * belonging to a specific gold standard always has a cross dataset ID that begins with the ID of its dataset
     * as given by the gold standard dataset ID method.</p>
     *
     * @return A string including the challenge, the task, the year and the topic number for this topic, excluding null elements.
     */
    public String getCrossDatasetId() {
        return Stream.of(challenge, task, year, number).filter(Objects::nonNull).map(String::valueOf).collect(Collectors.joining("-"));
    }
}
