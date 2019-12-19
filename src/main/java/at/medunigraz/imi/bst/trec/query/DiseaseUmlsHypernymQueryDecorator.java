package at.medunigraz.imi.bst.trec.query;

import at.medunigraz.imi.bst.retrieval.Query;
import at.medunigraz.imi.bst.trec.model.Topic;
import de.julielab.ir.umls.UmlsRelationsProvider;
import de.julielab.ir.umls.UmlsSynsetProvider;

import java.util.Collection;
import java.util.Set;

public class DiseaseUmlsHypernymQueryDecorator extends DynamicQueryDecorator {

    private UmlsSynsetProvider synsetProvider;
    private UmlsRelationsProvider relationsProvider;

    public DiseaseUmlsHypernymQueryDecorator(Query decoratedQuery) {
        super(decoratedQuery);
        synsetProvider = UmlsSynsetProvider.getInstance();
        relationsProvider = UmlsRelationsProvider.getInstance();
    }

    /**
     * For tests.
     *
     * @param synsetProvider The synset provider.
     */
    void setSynsetProvider(UmlsSynsetProvider synsetProvider) {
        this.synsetProvider = synsetProvider;
    }

    /**
     * For tests.
     *
     * @param relationsProvider The relations provider.
     */
    void setRelationsProvider(UmlsRelationsProvider relationsProvider) {
        this.relationsProvider = relationsProvider;
    }

    @Override
    public Topic expandTopic(Topic topic) {
        String disease = topic.getDisease();

        final Set<String> cuis = synsetProvider.getCuis(disease);

        for (String cui : cuis) {
            final Set<String> relatives = relationsProvider.getRelatives(cui, UmlsRelationsProvider.Relation.PARENT);
            relatives.stream().map(synsetProvider::getCuiSynset).flatMap(Collection::stream).map(String::toLowerCase).distinct().forEach(topic::withDiseaseHypernym);
        }
        return topic;
    }

}
