package at.medunigraz.imi.bst.trec.experiment;

import at.medunigraz.imi.bst.retrieval.Retrieval;
import at.medunigraz.imi.bst.trec.model.Result;
import at.medunigraz.imi.bst.trec.query.covid.NarrativeSynonymDecorator;
import at.medunigraz.imi.bst.trec.query.covid.SynonymQueryDecorator;
import at.medunigraz.imi.bst.trec.query.covid.WordRemovalQueryDecorator;
import de.julielab.ir.model.CovidTopic;

import java.util.function.Function;

public class Cord19Retrieval extends Retrieval<Cord19Retrieval, CovidTopic> {

    public Cord19Retrieval(String indexName) {
        super(indexName);
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
}
