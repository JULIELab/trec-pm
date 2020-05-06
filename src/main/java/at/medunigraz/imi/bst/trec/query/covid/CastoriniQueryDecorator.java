package at.medunigraz.imi.bst.trec.query.covid;

import at.medunigraz.imi.bst.retrieval.Query;
import at.medunigraz.imi.bst.retrieval.QueryDecorator;
import at.medunigraz.imi.bst.trec.model.Result;
import de.julielab.ir.model.CovidTopic;
import de.julielab.ir.nlp.NLPSentence;
import de.julielab.ir.nlp.NLPToken;
import de.julielab.ir.nlp.PosTaggingService;
import de.julielab.java.utilities.FileUtilities;
import de.julielab.java.utilities.IOStreamUtilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Checking in how far we can reproduce experiments from https://github.com/castorini/anserini/blob/master/docs/experiments-covid.md
 */
public class CastoriniQueryDecorator extends QueryDecorator<CovidTopic> {

    private static final Set<String> DOMAIN_STOPWORDS = new HashSet<>();

    public CastoriniQueryDecorator(Query<CovidTopic> decoratedQuery) {
        super(decoratedQuery);
        readStopwords();
    }

    @Override
    public List<Result> query(CovidTopic topic) {
        String query = topic.getQuery();
        String question = topic.getQuestion();
        Set<String> unfilteredWords = new HashSet<>();
        unfilteredWords.addAll(filter(query));
        unfilteredWords.addAll(filter(question));
        topic.setMandatoryBoW(unfilteredWords);
        return decoratedQuery.query(topic);
    }

    private Set<String> filter(String query) {
        Set<String> ret = new HashSet<>();
        NLPSentence s = PosTaggingService.getInstance().tag(query);
        for (NLPToken t : s) {
            if (!DOMAIN_STOPWORDS.contains(t.getToken()))
                ret.add(t.getToken());
        }
        return ret;
     }

    private void readStopwords() {
        try (BufferedReader r = IOStreamUtilities.getReaderFromInputStream(FileUtilities.findResource("/galago_inquery.txt"))) {
            r.lines().filter(l -> !l.startsWith("#")).map(String::trim).forEach(DOMAIN_STOPWORDS::add);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
