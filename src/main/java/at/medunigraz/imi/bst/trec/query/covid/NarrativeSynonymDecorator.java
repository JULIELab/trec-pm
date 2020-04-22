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

    public NarrativeSynonymDecorator(Query<CovidTopic> decoratedQuery) {
        super(decoratedQuery);
    }


    @Override
    public CovidTopic expandTopic(CovidTopic topic) {
        List<Set<String>> mandatorySynonyms = filterWords(topic.getMandatoryBoW());
        topic.setMandatorySynonymWords(mandatorySynonyms);
        if (topic.getOptionalBoW() != null) {
            List<Set<String>> optionalSynonyms = filterWords(topic.getOptionalBoW());
            topic.setOptionalSynonymWords(optionalSynonyms);
        }

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
        switch (word.toLowerCase()) {
            case "coronavirus":
                return true;
            case "animal":
                return true;
            case "virus":
                return true;
            case "covid19":
                return true;
            case "covid-19":
                return true;
            case "sars-cov-2":
                return true;
            case "sars-cov2":
                return true;
            case "2019-ncov":
                return true;
            default:
                return false;
        }
    }

    private Set<String> getSynonyms(String word) {
        // return synonyms of this word like coronavirus -> Covid19
        Set<String> synonyms = new HashSet<>();
        switch (word.toLowerCase()) {
            case "coronavirus":
                synonyms.add("COVID19");
                synonyms.add("Covid19");
                synonyms.add("COVID-19");
                synonyms.add("Covid-19");
                synonyms.add("SARS-CoV-2");
                synonyms.add("SARS-CoV2");
                synonyms.add("2019-nCoV");
                break;
            case "virus":
                synonyms.add("SARS-CoV-2");
                synonyms.add("SARS-CoV2");
                synonyms.add("2019-nCoV");
                break;
            case "animal":
                synonyms.add("mouse");
                break;
            case "covid19":
                synonyms.add("covid-19");
                synonyms.add("2019-ncov");
                break;
            case "covid-19":
                synonyms.add("covid19");
                synonyms.add("2019-ncov");
                break;
            case "sars-cov-2":
                synonyms.add("2019-ncov");
                synonyms.add("sars-cov2");
                break;
            case "sars-cov2":
                synonyms.add("2019-ncov");
                synonyms.add("sars-cov-2");
                break;
            case "2019-ncov":
                synonyms.add("sars-cov-2");
                synonyms.add("sars-cov2");
                break;
        }
        return synonyms;
    }


}
