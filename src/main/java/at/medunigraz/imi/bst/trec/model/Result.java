package at.medunigraz.imi.bst.trec.model;

import java.io.Serializable;
import java.util.*;

public class Result implements Serializable, Cloneable {
    private String id;

    private String index;

    private double score;

    private Map<String, Object> sourceFields;

    private List<String> treatments = Collections.emptyList();

    public Result(String id, double score) {
        this(id, null, score);
    }

    public Result(String id, String index, double score) {
        this.id = id;
        this.index = index;
        this.score = score;
    }

    @Override
    public Result clone() {
        try {
            Result clone = (Result) super.clone();
            clone.id = id;
            clone.score = score;
            clone.sourceFields = sourceFields;
            clone.treatments = treatments;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }

    public String getIndex() {
        return index;
    }

    public String getId() {
        return id;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public Map<String, Object> getSourceFields() {
        return sourceFields;
    }

    public void setSourceFields(Map<String, Object> sourceFields) {
        this.sourceFields = sourceFields;
        this.treatments = getFocusedTreatmentText();
    }

    public List<String> getFocusedTreatmentCuis() {
        if (sourceFields != null)
            return (List<String>) sourceFields.getOrDefault("focusedTreatmentCuis", Collections.emptyList());
        return Collections.emptyList();
    }

    public List<String> getBroadTreatmentCuis() {
        if (sourceFields != null)
            return (List<String>) sourceFields.getOrDefault("broadTreatmentCuis", Collections.emptyList());
        return Collections.emptyList();
    }

    public List<String> getFocusedTreatmentText() {
        if (sourceFields != null)
            return (List<String>) sourceFields.getOrDefault("focusedTreatmentText", Collections.emptyList());
        return Collections.emptyList();
    }

    public List<String> getBroadTreatmentText() {
        if (sourceFields != null)
            return (List<String>) sourceFields.getOrDefault("broadTreatmentText", Collections.emptyList());
        return Collections.emptyList();
    }

    /**
     * Obtains an unique set of treatments. Intended to be called by `TrecWriter`.
     *
     * @return
     */
    public Set<String> getUniqueTreatments() {
        return new LinkedHashSet<>(getTreatments());
    }

    public List<String> getTreatments() {
        return this.treatments;
    }

    public void setTreatments(List<String> treatments) {
        this.treatments = treatments;
    }

    public void setIndex(String index) {
        this.index = index;
    }
}
