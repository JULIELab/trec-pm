package at.medunigraz.imi.bst.trec.experiment;

import at.medunigraz.imi.bst.retrieval.Retrieval;
import at.medunigraz.imi.bst.trec.model.Gene;
import at.medunigraz.imi.bst.trec.model.Topic;
import at.medunigraz.imi.bst.trec.query.*;

public class TrecPmRetrieval extends Retrieval<TrecPmRetrieval, Topic> {

    public TrecPmRetrieval(String indexName) {
        super(indexName);
    }

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

    public TrecPmRetrieval withDiseaseExpander() {
        query = new DiseaseExpanderQueryDecorator(query);
        return this;
    }

    public TrecPmRetrieval withDiseasePreferredTerm() {

        query = new DiseasePreferredTermQueryDecorator(query);
        return this;
    }

    public TrecPmRetrieval withDiseaseSynonym() {

        query = new DiseaseUmlsSynonymQueryDecorator(query);
        return this;
    }

    public TrecPmRetrieval withGeneSynonym() {

        query = new GeneSynonymQueryDecorator(query);
        return this;
    }

    public TrecPmRetrieval withGeneDescription() {

        query = new GeneDescriptionQueryDecorator(query);
        return this;
    }

    public TrecPmRetrieval withDiseaseHypernym() {

        query = new DiseaseHypernymQueryDecorator(query);
        return this;
    }

    public TrecPmRetrieval withSolidTumor() {

        query = new SolidTumorQueryDecorator(query);
        return this;
    }

    public TrecPmRetrieval withGeneFamily() {

        query = new GeneFamilyQueryDecorator(query);
        return this;
    }

    public TrecPmRetrieval withDrugInteraction() {

        query = new DrugInteractionQueryDecorator(query);
        return this;
    }

    public TrecPmRetrieval withResistantDrugs() {

        query = new ResistantDrugsQueryDecorator(query);
        return this;
    }
}
