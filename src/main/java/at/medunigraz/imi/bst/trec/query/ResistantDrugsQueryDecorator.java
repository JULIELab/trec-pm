package at.medunigraz.imi.bst.trec.query;

import at.medunigraz.imi.bst.retrieval.Query;
import at.medunigraz.imi.bst.trec.expansion.Cosmic;
import at.medunigraz.imi.bst.trec.expansion.DGIdb;
import at.medunigraz.imi.bst.trec.model.Topic;
import at.medunigraz.imi.bst.trec.model.TopicGene;

import java.util.Set;

public class ResistantDrugsQueryDecorator extends DynamicQueryDecorator {

    private static final Cosmic cosmic = Cosmic.getInstance();

    public ResistantDrugsQueryDecorator(Query decoratedQuery) {
        super(decoratedQuery);
    }

    @Override
    public Topic expandTopic(Topic topic) {
        final TopicGene[] genes = topic.getGenes();
        for (TopicGene gene : genes) {
            final Set<String> drugs = cosmic.getDrugsThatGeneMutationConfersResistanceAgainst(gene);
            for (String drug : drugs)
                topic.withResistantDrug(drug);
        }
        return topic;
    }

}
