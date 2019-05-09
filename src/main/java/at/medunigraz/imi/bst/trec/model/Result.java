package at.medunigraz.imi.bst.trec.model;

import java.io.Serializable;

public class Result implements Serializable {
	private String id;

	private float score;

	public Result(String id, float score) {
		this.id = id;
		this.score = score;
	}

	public String getId() {
		return id;
	}

	public float getScore() {
		return score;
	}
}
