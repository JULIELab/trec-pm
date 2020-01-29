package at.medunigraz.imi.bst.trec.query;

import at.medunigraz.imi.bst.retrieval.Query;
import at.medunigraz.imi.bst.trec.model.Topic;
import de.julielab.ir.umls.UmlsSynsetProvider;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collection;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DiseaseUmlsSynonymQueryDecorator extends DynamicQueryDecorator {

    private transient UmlsSynsetProvider umlsSynsetProvider;
    private transient Matcher parenthesisM;

    public DiseaseUmlsSynonymQueryDecorator(Query decoratedQuery) {
        super(decoratedQuery);
        init();
    }

    @NotNull
    private void init() {
        parenthesisM = Pattern.compile("[(\\[{][^]})]+[]})]").matcher("");
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
        final Set<String> uniqueLowercasedSynonyms = umlsSynsetProvider.getSynsets(disease).stream()
                .flatMap(Collection::stream)
                .map(String::toLowerCase)
                .map(s -> parenthesisM.reset(s).replaceAll(""))
                .map(String::trim)
                .filter(s -> !s.equalsIgnoreCase(disease))
                .collect(Collectors.toSet());
        for (String synonym : uniqueLowercasedSynonyms) {
            if (!topic.getDiseaseSynonyms().contains(synonym))
                topic.withDiseaseSynonym(synonym);
        }
        return topic;
    }

    private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException {
        aInputStream.defaultReadObject();
        init();
    }


}
