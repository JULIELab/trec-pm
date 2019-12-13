package at.medunigraz.imi.bst.trec.query;

import at.medunigraz.imi.bst.retrieval.Query;
import at.medunigraz.imi.bst.trec.model.Topic;
import de.julielab.ir.umls.UmlsSynsetProvider;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class DiseaseUmlsSynonymQueryDecorator extends DynamicQueryDecorator {

    private transient UmlsSynsetProvider umlsSynsetProvider;

    public DiseaseUmlsSynonymQueryDecorator(Query decoratedQuery) {
        super(decoratedQuery);
    }

    /**
     * For tests.
     *
     * @param umlsSynsetProvider The synset provider to use.
     */
    void setUmlsSynsetProvider(UmlsSynsetProvider umlsSynsetProvider) {
        this.umlsSynsetProvider = umlsSynsetProvider;
    }

    @Override
    public Topic expandTopic(Topic topic) {
        if (umlsSynsetProvider == null)
            umlsSynsetProvider = UmlsSynsetProvider.getInstance();
        String disease = topic.getDisease();
        final Set<String> uniqueLowercasedSynonyms = umlsSynsetProvider.getSynsets(disease).stream().flatMap(Collection::stream).map(String::toLowerCase).filter(s -> !s.equalsIgnoreCase(disease)).distinct().collect(Collectors.toSet());
        for (String synonym : uniqueLowercasedSynonyms) {
            topic.withDiseaseSynonym(synonym);
        }
        return topic;
    }

}
