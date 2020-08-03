package at.medunigraz.imi.bst.trec.experiment;

import at.medunigraz.imi.bst.retrieval.Retrieval;
import at.medunigraz.imi.bst.trec.query.covid.*;
import de.julielab.ir.model.CovidTopic;

public class Cord19Retrieval extends Retrieval<Cord19Retrieval, CovidTopic> {

    public Cord19Retrieval(String indexName) {
        super(indexName);
    }

    public Cord19Retrieval(String[] indexNames) {
        super(indexNames);
    }

    public Cord19Retrieval withWordRemoval() {
        query = new WordRemovalQueryDecorator(query);
        return this;
    }

    public Cord19Retrieval withNarrativeSynonymDecorator() {
        query = new NarrativeSynonymDecorator(query);
        return this;
    }

    public Cord19Retrieval withQueryQuestionSynonyms() {
        query = new SynonymQueryDecorator(query);
        return this;
    }

    public Cord19Retrieval withQueryQuestionBoW() {
        query = new CastoriniQueryDecorator(query);
        return this;
    }

    public Cord19Retrieval withRound5Decorator() {
        query = new CovidRnd5Decorator(query);
        return this;
    }
}
