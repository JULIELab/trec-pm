package at.medunigraz.imi.bst.trec.query.covid;

import at.medunigraz.imi.bst.retrieval.Query;
import at.medunigraz.imi.bst.retrieval.QueryDecorator;
import at.medunigraz.imi.bst.trec.model.Result;
import at.medunigraz.imi.bst.trec.model.Topic;
import at.medunigraz.imi.bst.trec.query.DynamicQueryDecorator;
import de.julielab.ir.model.CovidTopic;
import de.julielab.ir.umls.UmlsRelationsProvider;
import de.julielab.ir.umls.UmlsSynsetProvider;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class NarrativeSynonymDecorator extends DynamicQueryDecorator<CovidTopic> {

    public NarrativeSynonymDecorator(Query decoratedQuery) {
        super(decoratedQuery);
    }


    @Override
    public CovidTopic expandTopic(CovidTopic topic) {
        List<Set<String>> mandatorySynonyms = filterWords(topic.getMandatoryBoW());
        topic.setMandatorySynonymWords(mandatorySynonyms);
        List<Set<String>> optionalSynonyms = filterWords(topic.getOptionalBoW());
        topic.setOptionalSynonymWords(optionalSynonyms);
        return topic;
    }

    @NotNull
    private List<Set<String>> filterWords(Set<String> narrative) {
        List<Set<String>> synonyms = new ArrayList<>();
        Iterator<String> narIt = narrative.iterator();
        while (narIt.hasNext()) {
            String word =  narIt.next();
            synonyms.add(getSynonyms(word));
            if (removeFromBoW(word))
                narIt.remove();
        }
        return synonyms;
    }


    private boolean removeFromBoW(String word) {
        // check those words that should be removed from the query and be replaced by synonyms like coronavirus
        if (word.equals("coronavirus"))
            return true;
        else if (word.equals("animal"))
            return true;
        else if (word.equals("virus"))
            return true;
        else
            return false;
    }

    private Set<String> getSynonyms(String word) {
        // return synonyms of this word like coronavirus -> Covid19
        Set<String> synonyms = new HashSet<>();
        if (word.equals("coronavirus")) {
            synonyms.add("COVID19");
            synonyms.add("Covid19");
            synonyms.add("COVID-19");
            synonyms.add("Covid-19");
            synonyms.add("SARS-CoV-2");
            synonyms.add("SARS-CoV2");
            synonyms.add("2019-nCoV");
        } else if (word.equals("virus")){
            synonyms.add("SARS-CoV-2");
            synonyms.add("SARS-CoV2");
            synonyms.add("2019-nCoV");
        } else if (word.equals("animal")){
            synonyms.add("mouse");
        }
        return synonyms;
    }


}
