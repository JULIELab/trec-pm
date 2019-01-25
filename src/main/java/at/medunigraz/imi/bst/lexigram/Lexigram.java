package at.medunigraz.imi.bst.lexigram;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.trec.expansion.CachedWebRequester;
import at.medunigraz.imi.bst.trec.model.Topic;
import at.medunigraz.imi.bst.trec.model.TopicSet;
import at.medunigraz.imi.bst.trec.stats.CSVStatsWriter;
import at.medunigraz.imi.bst.trec.utils.JsonUtils;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Lexigram {

    private static final Logger LOG = LogManager.getLogger();

    private static final String ENDPOINT = "https://api.lexigram.io/v1/lexigraph/";

    private static final CachedWebRequester REQUESTER = new CachedWebRequester("cache/lexigramV2.ser");

    private static final List<String> NOISE = new ArrayList<>();
    static {
        NOISE.add("classification");
        NOISE.add("international");
        NOISE.add("no oncology");
        NOISE.add("subtype");
        NOISE.add("morphology");
        NOISE.add(" - category");
        NOISE.add("ca - ");
    }

    public static boolean isAPIKeyLoaded() {
        final int MIN_API_KEY_LENGTH = 20;
        return TrecConfig.LEXIGRAM_APIKEY.length() > MIN_API_KEY_LENGTH;
    }

    /**
     * Retrieves the preferred term ("label") of the best-matching concept.
     * If no match, it returns itself
     *
     * @param label
     * @return
     */
    public static String getPreferredTerm(String label) {
        Optional<String> search = search(label);
        if (!search.isPresent()) {
            return label;
        }

        return concept(search.get()).label;
    }

    /**
     * Possibly the most useful function: Given a string, it searches for the best-matching concept and adds all synonyms
     * If no match, it returns a list with itself
     *
     * @param label
     * @return
     */
    public static List<String> addSynonymsFromBestConceptMatch(String label) {
        Optional<String> search = search(label);
        if (!search.isPresent()) {
            return Collections.singletonList(label);
        }

        List<String> keywordAndSynonyms = new ArrayList<>();
        keywordAndSynonyms.add(label);

        Concept concept = concept(search.get());
        keywordAndSynonyms.add(concept.label);
        keywordAndSynonyms.addAll(concept.synonyms);

        return cleanUpList(keywordAndSynonyms);
    }

    /**
     * Given a string, it searches for the best matching concept and returns all its synonyms.
     * If no match is found, an empty list is returned.
     *
     * @param label
     * @return
     */
    public static List<String> getSynonymsFromBestConceptMatch(String label) {
        List<String> ret = new ArrayList<>();

        Optional<String> search = search(label);
        if (!search.isPresent()) {
            return ret;
        }

        Concept concept = concept(search.get());
        ret.addAll(concept.synonyms);

        return cleanUpList(ret);
    }

    /**
     * Given a string, it searches for the best matching concept and returns all its ancestors.
     * If no match is found, an empty list is returned.
     *
     * @param label
     * @return
     */
    public static List<String> getAncestorsFromBestConceptMatch(String label) {
        List<String> ret = new ArrayList<>();

        Optional<String> search = search(label);
        if (!search.isPresent()) {
            return ret;
        }

        return cleanUpList(ancestors(search.get()));
    }

    private static List<String> ancestors(String conceptId) {
        // TODO pagination
        JSONObject body = get(ENDPOINT + "concepts/" + conceptId + "/ancestors");

        List<String> ancestors = new ArrayList<>();
        JSONArray results = body.getJSONArray("results");
        for(int i = 0; i < results.length(); i++) {
            ancestors.add(cleanUpString(results.getJSONObject(i).getString("label")));
        }

        return ancestors;
    }

    private static Concept concept(String conceptId) {
        /* Get info (label and synonyms) of concept */
        JSONObject body = get(ENDPOINT + "concepts/" + conceptId);

        Concept concept = new Concept();
        concept.label = cleanUpString(body.getString("label"));

        JSONArray synonymsJson = body.getJSONArray("synonyms");
        for(int i = 0; i < synonymsJson.length(); i++) {
            concept.synonyms.add(cleanUpString(synonymsJson.get(i).toString()));
        }

        return concept;
    }

    private static Optional<String> search(String label) {
        JSONObject body = get(ENDPOINT + "search?q="+ URLEncoder.encode(label));
        JSONArray results = body.getJSONArray("conceptSearchHits");
        if (results.length() == 0) {
            return Optional.empty();
        }

        /* Get first concept (most relevant search hit) */
        JSONObject item = results.getJSONObject(0);
        JSONObject concept = item.getJSONObject("concept");
        return Optional.of(concept.getString("id"));
    }

    private static List<String> cleanUpList(List<String> labels) {
        Set<String> cleanLabels = new HashSet<>();
        cleanLabels.addAll(labels.stream().map(Lexigram::cleanUpString).collect(Collectors.toSet()));

        List<String> cleanLabelsArrayList = new ArrayList<>();
        cleanLabelsArrayList.addAll(cleanLabels);
        return cleanLabelsArrayList;
    }

    private static String cleanUpString(String label) {
        String cleanLabel = label;

        // Lowercase everything
        cleanLabel = cleanLabel.toLowerCase();

        // Remove stuff in parentheses
        cleanLabel = cleanLabel.replaceAll("\\(.*\\)", "");

        // Remove stuff in square brackets
        cleanLabel = cleanLabel.replaceAll("\\[.*\\]", "");

        // Remove stuff after comma
        cleanLabel = cleanLabel.split(",")[0];

        // Remove noise
        for (String stopword : NOISE) {
            cleanLabel = cleanLabel.replace(stopword, "");
        }

        // Remove extra whitespace due to cleaning
        cleanLabel = cleanLabel.replaceAll(" {2,}", " ");

        // Remove whitespace at the end
        return cleanLabel.trim();
    }

    private static JSONObject get(String url) {
        return new JSONObject(REQUESTER.get(url, TrecConfig.LEXIGRAM_APIKEY));
    }

    /**
     * Auxiliary main method to dump all expansions into a file for easier debug.
     *
     * @param args
     */
    public static void main(String[] args) {
        final File topicsFile = new File(CSVStatsWriter.class.getResource("/topics/topics2018.xml").getPath());

        TopicSet topicSet = new TopicSet(topicsFile);
        JSONObject output = createDump(topicSet);

        System.out.println(JsonUtils.prettify(output));
    }

    private static JSONObject createDump(TopicSet topicSet) {
        JSONObject output = new JSONObject();

        Set<String> diseaseSet = new HashSet<>();

        for (Topic topic : topicSet.getTopics()) {
            String disease = topic.getDisease();

            // Do not repeat diseases
            if (diseaseSet.contains(disease)) {
                continue;
            }
            diseaseSet.add(disease);

            String preferredTerm = getPreferredTerm(disease);
            List<String> synonyms = getSynonymsFromBestConceptMatch(disease);
            List<String> ancestors = getAncestorsFromBestConceptMatch(disease);

            JSONObject diseaseJson = new JSONObject()
                    .put("preferredTerm", preferredTerm)
                    .put("synonyms", synonyms)
                    .put("ancestors", ancestors);

            output.put(disease, diseaseJson);
        }

        return output;
    }
}
