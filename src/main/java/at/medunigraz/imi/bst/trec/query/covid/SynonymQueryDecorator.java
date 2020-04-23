package at.medunigraz.imi.bst.trec.query.covid;

import at.medunigraz.imi.bst.retrieval.Query;
import at.medunigraz.imi.bst.trec.model.Topic;
import at.medunigraz.imi.bst.trec.query.DynamicQueryDecorator;
import de.julielab.ir.model.CovidTopic;
import de.julielab.ir.umls.UmlsRelationsProvider;
import de.julielab.ir.umls.UmlsSynsetProvider;
import org.elasticsearch.common.util.set.Sets;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SynonymQueryDecorator extends DynamicQueryDecorator<CovidTopic> {

    private transient UmlsSynsetProvider synsetProvider;
    private transient UmlsRelationsProvider relationsProvider;

    public SynonymQueryDecorator(Query decoratedQuery) {
        super(decoratedQuery);
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
    public CovidTopic expandTopic(CovidTopic topic) {
        String query = topic.getQuery();
        String question = topic.getQuestion();
        if (synsetProvider == null) {
            synsetProvider = UmlsSynsetProvider.getInstance();
            relationsProvider = UmlsRelationsProvider.getInstance();
        }

        final Set<String> cuisQuery = Arrays.stream(query.split("\\s+")).map(synsetProvider::getCuis).flatMap(Collection::stream).collect(Collectors.toSet());
        final Set<String> cuisQuestion = Arrays.stream(question.split("\\s+")).map(synsetProvider::getCuis).flatMap(Collection::stream).collect(Collectors.toSet());

        Set<String> querySynonyms = cuisQuery.stream().map(synsetProvider::getCuiSynset).flatMap(Collection::stream).map(String::toLowerCase).collect(Collectors.toSet());
        Set<String> questionSynonyms = cuisQuestion.stream().map(synsetProvider::getCuiSynset).flatMap(Collection::stream).map(String::toLowerCase).collect(Collectors.toSet());

        topic.setMandatoryBoW(Sets.union(querySynonyms, questionSynonyms));

        return topic;
    }


}
