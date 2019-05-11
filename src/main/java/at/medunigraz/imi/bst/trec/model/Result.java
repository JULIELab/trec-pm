package at.medunigraz.imi.bst.trec.model;

import java.io.Serializable;

public class Result implements Serializable {
	private String id;

	private double score;

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
}
