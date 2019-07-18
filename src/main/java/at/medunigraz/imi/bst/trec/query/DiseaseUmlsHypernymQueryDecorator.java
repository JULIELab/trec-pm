package at.medunigraz.imi.bst.trec.query;

import at.medunigraz.imi.bst.retrieval.Query;
import at.medunigraz.imi.bst.trec.model.Topic;
import de.julielab.ir.umls.UmlsRelationsProvider;
import de.julielab.ir.umls.UmlsSynset;
import de.julielab.ir.umls.UmlsSynsetProvider;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class DiseaseUmlsHypernymQueryDecorator extends DynamicQueryDecorator {

    public DiseaseUmlsHypernymQueryDecorator(Query decoratedQuery) {
        super(decoratedQuery);
    }

    @Override
    public Topic expandTopic(Topic topic) {
        String disease = topic.getDisease();

        final UmlsSynsetProvider synsetProvider = UmlsSynsetProvider.getInstance();
        final Set<String> cuis = synsetProvider.getCuis(disease);
        final UmlsRelationsProvider relationsProvider = UmlsRelationsProvider.getInstance();

        for (String cui : cuis) {
            final Set<String> relatives = relationsProvider.getRelatives(cui, UmlsRelationsProvider.Relation.PARENT);
            relatives.stream().map(synsetProvider::getCuiSynset).flatMap(Collection::stream).forEach(topic::withDiseaseHypernym);
        }
        return topic;
    }

}
