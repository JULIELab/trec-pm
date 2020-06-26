package at.medunigraz.imi.bst.trec.query.covid;

import at.medunigraz.imi.bst.retrieval.Query;
import at.medunigraz.imi.bst.retrieval.QueryDecorator;
import at.medunigraz.imi.bst.trec.model.Result;
import de.julielab.ir.model.CovidTopic;
import de.julielab.ir.nlp.NLPSentence;
import de.julielab.ir.nlp.NLPToken;
import de.julielab.ir.nlp.PosTaggingService;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * This decorator is applied to the original CovidTopic. Thus, {@link CovidTopic#getMandatoryBoW()} should be empty.
 */
public class WordRemovalQueryDecorator extends QueryDecorator<CovidTopic> {

    private static final Set<String> DOMAIN_STOPWORDS = new HashSet<>();

    private static final Set<String> STOPWORDS = new HashSet<>();
    static {
        try {
            List<String> words = Files.readAllLines(Paths.get("resources/stopwords.txt"));
            for (String word : words)
                STOPWORDS.add(word);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static final String TOKEN_SEPARATOR = " ";


    static {
        // replace by covid-related stop words
        DOMAIN_STOPWORDS.add("range");
        DOMAIN_STOPWORDS.add("types");
        DOMAIN_STOPWORDS.add("type");
        DOMAIN_STOPWORDS.add("seeking");
        DOMAIN_STOPWORDS.add("information");
        DOMAIN_STOPWORDS.add("studie");
        DOMAIN_STOPWORDS.add("study");
        DOMAIN_STOPWORDS.add("assays");
        DOMAIN_STOPWORDS.add("paper");
        DOMAIN_STOPWORDS.add("papers");
        DOMAIN_STOPWORDS.add("number");
        DOMAIN_STOPWORDS.add("proportion");
        DOMAIN_STOPWORDS.add("outcomes");
        DOMAIN_STOPWORDS.add("outcome");
        DOMAIN_STOPWORDS.add("people");
        DOMAIN_STOPWORDS.add("molecules");
        DOMAIN_STOPWORDS.add("patient");
        DOMAIN_STOPWORDS.add("evidence");
        DOMAIN_STOPWORDS.add("harm");
        DOMAIN_STOPWORDS.add("science");
        DOMAIN_STOPWORDS.add("value");
        DOMAIN_STOPWORDS.add("benefit");
        DOMAIN_STOPWORDS.add("cell");
        DOMAIN_STOPWORDS.add("protein");
        DOMAIN_STOPWORDS.add("molecule");
        DOMAIN_STOPWORDS.add("step");
        DOMAIN_STOPWORDS.add("does");
        DOMAIN_STOPWORDS.add("long");
        DOMAIN_STOPWORDS.add("etc.)");
        DOMAIN_STOPWORDS.add("etc)");
        DOMAIN_STOPWORDS.add("looking");
        DOMAIN_STOPWORDS.add("way");
        DOMAIN_STOPWORDS.add("activities");
        DOMAIN_STOPWORDS.add("lead");
        DOMAIN_STOPWORDS.add("sign");
        DOMAIN_STOPWORDS.add("confirm");
        DOMAIN_STOPWORDS.add("data");
        DOMAIN_STOPWORDS.add("measure");
        DOMAIN_STOPWORDS.add("kind");
        DOMAIN_STOPWORDS.add("factor");
        DOMAIN_STOPWORDS.add("group");
        DOMAIN_STOPWORDS.add("hand");
        DOMAIN_STOPWORDS.add("everyday");
        DOMAIN_STOPWORDS.add("object");
        DOMAIN_STOPWORDS.add("health");
        DOMAIN_STOPWORDS.add("care");
        DOMAIN_STOPWORDS.add("lack");
        DOMAIN_STOPWORDS.add("detect");
        DOMAIN_STOPWORDS.add("result");
        //DOMAIN_STOPWORDS.add("coronavirus");
    }

    public WordRemovalQueryDecorator(Query<CovidTopic> decoratedQuery) {
        super(decoratedQuery);
        readStopwords();
    }

    @Override
    public List<Result> query(CovidTopic topic) {
        String query = topic.getQuery();
        String question = topic.getQuestion();
        String narrative = topic.getNarrative();

        Set<String> mandatoryWords = new HashSet<>();
        Set<String> optionalWords = new HashSet<>();

        String[] queryTokens = query.split(" ");
        NLPSentence questionTokens = PosTaggingService.getInstance().tag(question);
        NLPSentence narrativeTokens = PosTaggingService.getInstance().tag(narrative);

        for (String queryToken : queryTokens)
            mandatoryWords.add(queryToken.toLowerCase());

        for (NLPToken questionToken : questionTokens){
            if (questionToken.getPosTag().equals("NN"))// || questionToken.getPosTag().equals("NNS"))
                mandatoryWords.add(questionToken.getToken().toLowerCase().replace(".", ""));
            else if (questionToken.getPosTag().equals("NNS"))
                if (questionToken.getToken().toLowerCase().endsWith("ies"))
                    mandatoryWords.add(questionToken.getToken().toLowerCase().replace(".", "").replaceFirst("ies$","y"));
                else
                    mandatoryWords.add(questionToken.getToken().toLowerCase().replace(".", "").replaceFirst("s$",""));
        }

        for (NLPToken narrativeToken : narrativeTokens) {
            if (narrativeToken.getPosTag().equals("NN"))// || narrativeToken.getPosTag().equals("NNS"))
                optionalWords.add(narrativeToken.getToken().toLowerCase().replace(".", ""));
            else if (narrativeToken.getPosTag().equals("NNS"))
                if (narrativeToken.getToken().toLowerCase().endsWith("ies"))
                    optionalWords.add(narrativeToken.getToken().toLowerCase().replace(".", "").replaceFirst("ies$","y"));
                else
                    optionalWords.add(narrativeToken.getToken().toLowerCase().replace(".", "").replaceFirst("s$",""));
        }
        // filter word list
        mandatoryWords.removeAll(STOPWORDS);
        mandatoryWords.removeAll(DOMAIN_STOPWORDS);
        optionalWords.removeAll(STOPWORDS);
        optionalWords.removeAll(DOMAIN_STOPWORDS);

        Set<String> filteredMandatoryWords = filterWords(mandatoryWords);
        Set<String> filteredOptionalWords = filterWords(optionalWords);

        filteredOptionalWords.removeAll(filteredMandatoryWords);

//        System.out.println(filteredMandatoryWords);
//        System.out.println(filteredOptionalWords);

        topic.setMandatoryBoW(filteredMandatoryWords);
        topic.setOptionalBoW(filteredMandatoryWords);

        return decoratedQuery.query(topic);
    }

    private void readStopwords(){
        //TODO: read stopwords from file
    }


    @NotNull
    private Set<String> filterWords(Set<String> narrative) {
        Set<String> synonyms = new HashSet<String>(narrative);
        Iterator<String> narIt = narrative.iterator();
        while (narIt.hasNext()) {
            String word =  narIt.next();
            synonyms.addAll(getSynonyms(word));
            if (removeFromBoW(word)) {
                synonyms.remove(word);
                narIt.remove();
            }
        }

        return synonyms;
    }


    private boolean removeFromBoW(String word) {
        // check those words that should be removed from the query and be replaced by synonyms like coronavirus
        switch (word.toLowerCase()) {
            case "coronavirus":
            case "animal":
            case "virus":
            case "cov2":
            case "cov-2":
            case "ncov":
            case "sars":
            case "covid":
            case "covid-19":
                return true;
            /*case "covid19":
                return true;
            case "covid-19":
                return true;
            case "sars-cov-2":
                return true;
            case "sars-cov2":
                return true;
            case "2019-ncov":
                return true;*/
            default:
                return false;
        }
    }

    private Set<String> getSynonyms(String word) {
        // return synonyms of this word like coronavirus -> Covid19
        Set<String> synonyms = new HashSet<>();
        switch (word.toLowerCase()) {
            /*case "coronavirus":
            case "covid":
                synonyms.add("covid19");
                synonyms.add("covid-19");
                synonyms.add("Covid19");
                synonyms.add("Covid-19");
                synonyms.add("SARS-CoV-2");
                synonyms.add("SARS-CoV2");
                synonyms.add("2019-nCoV");
                break;*/
            /*case "virus":
            case "cov2":
            case "ncov":
                synonyms.add("sars-cov-2");
                //synonyms.add("sars-cov2");
                //synonyms.add("2019-ncov");
                break;*/
            case "animal":
                synonyms.add("mouse");
                break;
                /*case "covid19":
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
                break;*/
        }
        return synonyms;
    }



}
