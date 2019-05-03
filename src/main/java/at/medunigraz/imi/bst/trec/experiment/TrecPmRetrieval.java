package at.medunigraz.imi.bst.trec.experiment;

import at.medunigraz.imi.bst.retrieval.Retrieval;
import at.medunigraz.imi.bst.trec.model.Gene;
import at.medunigraz.imi.bst.trec.query.DiseaseReplacerQueryDecorator;
import at.medunigraz.imi.bst.trec.query.GeneExpanderQueryDecorator;
import at.medunigraz.imi.bst.trec.query.WordRemovalQueryDecorator;

public class TrecPmRetrieval extends Retrieval<TrecPmRetrieval> {


    public TrecPmRetrieval withWordRemoval() {
        query = new WordRemovalQueryDecorator(query);
        return this;
    }

    public TrecPmRetrieval withGeneExpansion(Gene.Field[] expandTo) {
        query = new GeneExpanderQueryDecorator(expandTo, query);
        return this;
    }

    public TrecPmRetrieval withDiseaseReplacer() {
        query = new DiseaseReplacerQueryDecorator(query);
        return this;
    }
}
