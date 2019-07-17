package at.medunigraz.imi.bst.trec.model;

import de.julielab.ir.ltr.Document;
import de.julielab.ir.ltr.DocumentList;
import de.julielab.ir.model.QueryDescription;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ResultList<T extends QueryDescription> {
	private T topic;
	
	private List<Result> results = new ArrayList<>();
	
	public ResultList(T topic) {
		this.topic = topic;
	}
	
	public boolean add(Result result) {
		return results.add(result);
	}

	public boolean addAll(Collection<Result> results) {
		return this.results.addAll(results);
	}

	public T getTopic() {
		return topic;
	}
	
	public List<Result> getResults() {
		return results;
	}

	/**
	 * Converts this ResultList into a DocumentList object.
	 * @return
	 */
	public DocumentList<T> toDocumentList() {
		DocumentList<T> documentList = new DocumentList<>();
		for (Result result : results) {
			final Document<T> document = new Document<>();
			document.setQueryDescription(topic);
			document.setId(result.getId());
			documentList.add(document);
		}
		return documentList;
	}
}
