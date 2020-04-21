package at.medunigraz.imi.bst.trec.experiment;

import at.medunigraz.imi.bst.retrieval.Retrieval;
import de.julielab.ir.model.CovidTopic;

public class Cord19Retrieval extends Retrieval<Cord19Retrieval, CovidTopic> {

    public Cord19Retrieval(String indexName) {
        super(indexName);
    }

}
