package at.medunigraz.imi.bst.trec.query;

import at.medunigraz.imi.bst.lexigram.Lexigram;
import at.medunigraz.imi.bst.retrieval.Query;
import at.medunigraz.imi.bst.trec.model.Topic;
import de.julielab.ir.umls.UmlsSynsetProvider;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DiseaseUmlsSynonymQueryDecorator extends DynamicQueryDecorator {

    public DiseaseUmlsSynonymQueryDecorator(Query decoratedQuery) {
        super(decoratedQuery);
    }

    @Override
    public Topic expandTopic(Topic topic) {
        String disease = topic.getDisease();
        final Set<String> uniqueLowercasedSynonyms = UmlsSynsetProvider.getInstance().getSynsets(disease).stream().flatMap(Collection::stream).map(String::toLowerCase).filter(s -> !s.equalsIgnoreCase(disease)).distinct().collect(Collectors.toSet());
        for (String synonym : uniqueLowercasedSynonyms) {
            topic.withDiseaseSynonym(synonym);
        }
        return topic;
    }

}
