package at.medunigraz.imi.bst.trec.model;

import java.io.Serializable;
import java.util.*;

public class Result implements Serializable {
	private String id;

	private double score;

	private Map<String, Object> sourceFields;

	public Result(String id, double score) {
		this.id = id;
		this.score = score;
	}

	public String getId() {
		return id;
	}

	public double getScore() {
		return score;
	}

	public Map<String, Object> getSourceFields() {
		return sourceFields;
	}

	public void setSourceFields(Map<String, Object> sourceFields) {
		this.sourceFields = sourceFields;
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
	 * @return
	 */
	public Set<String> getUniqueTreatments() {
		return new LinkedHashSet<>(getFocusedTreatmentText());
	}

    public void setScore(double score) {
        this.score = score;
    }
}
