package at.medunigraz.imi.bst.trec.query;

import at.medunigraz.imi.bst.trec.expansion.DGIdb;
import at.medunigraz.imi.bst.trec.model.Topic;

import java.util.Set;

public class DrugInteractionQueryDecorator extends DynamicQueryDecorator {

    private static final DGIdb dgidb = new DGIdb();

    public DrugInteractionQueryDecorator(Query decoratedQuery) {
        super(decoratedQuery);
    }

    @Override
    public Topic expandTopic(Topic topic) {
        String[] geneTokens = topic.getGeneTokens();

        for (String token : geneTokens) {
            Set<String> interactions = dgidb.getDrugInteractions(token);
            for (String interaction : interactions) {
                topic.withDrugInteraction(interaction);
            }
        }
        return topic;
    }

}
