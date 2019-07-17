package de.julielab.ir.ltr.features;

import at.medunigraz.imi.bst.trec.model.Topic;
import at.medunigraz.imi.bst.trec.model.TopicGene;
import at.medunigraz.imi.bst.trec.model.TopicSet;
import com.roklenarcic.util.strings.StringMap;
import com.roklenarcic.util.strings.WholeWordLongestMatchMap;
import de.julielab.ir.ltr.features.featurenames.MatchType;
import de.julielab.jcore.types.ConceptMention;
import de.julielab.jcore.types.Disease;
import de.julielab.jcore.types.Gene;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * <p>This class takes a UIMA CAS and a {@link Topic} and finds different kinds of topic field occurrences in the document for subsequent feature generation.</p>
 * <p>Note that this class is not a proper UIMA annotator which would extend one of UIMA's annotator classes. We just use the CAS structure for annotation.</p>
 */
public class TopicFieldsCasAnnotator {
    private final static Logger log = LoggerFactory.getLogger(TopicFieldsCasAnnotator.class);
    private final StringMap<Map<String, TopicMatch>> matcher;
    private Set<String> knownTopicNumbers = new HashSet<>();

    public TopicFieldsCasAnnotator(Iterable<Topic> topics) {
        Map<String, Map<String, TopicMatch>> dictionary = new LinkedHashMap<>();
        for (Topic topic : topics) {
            addDiseaseToDictionary(topic, dictionary);
            addTopicGenesToDictionary(topic, dictionary);
            knownTopicNumbers.add(topic.getCrossDatasetId());
        }
        matcher = new WholeWordLongestMatchMap<>(dictionary.keySet(), dictionary.values(), false);
    }

    /**
     * <p>Initialization. Adds the disease name, its synonyms and hypernyms to the dictionary.</p>
     *
     * @param topic      The topic.
     * @param dictionary The dictionary being built.
     */
    private void addDiseaseToDictionary(Topic topic, Map<String, Map<String, TopicMatch>> dictionary) {
        FeatureUtils fu = FeatureUtils.getInstance();
        String normDisease = fu.normalizeString(topic.getDisease());
        addToDictionary(normDisease, new TopicMatch(topic, MatchType.DISEASE), dictionary, topic);
        for (String diseaseSynonym : topic.getDiseaseSynonyms()) {
            final String normDiseaseSynonym = fu.normalizeString(diseaseSynonym);
            addToDictionary(normDiseaseSynonym, new TopicMatch(topic, MatchType.DISEASE_SYNONYM), dictionary, topic);
        }
        for (String diseaseHypernym : topic.getDiseaseHypernyms()) {
            final String normDiseaseSynonym = fu.normalizeString(diseaseHypernym);
            addToDictionary(normDiseaseSynonym, new TopicMatch(topic, MatchType.DISEASE_HYPERNYM), dictionary, topic);
        }
    }

    /**
     * <p>Initialization. Adds the gene with and without variations to the dictionary.</p>
     *
     * @param topic      The topic.
     * @param dictionary The dictionary being built.
     */
    private void addTopicGenesToDictionary(Topic topic, Map<String, Map<String, TopicMatch>> dictionary) {
        FeatureUtils fu = FeatureUtils.getInstance();
        for (TopicGene gene : topic.getGenes()) {
            String normGene = fu.normalizeString(gene.getGeneSymbol());
            addToDictionary(normGene, new TopicMatch(topic, MatchType.GENE), dictionary, topic);

            if (gene.getMutation() != null) {
                // we don't normalize the variant because we will compare with the PointMutations created
                // by the MutationFinder which already should be the normalization.
                addToDictionary(gene.getMutation(), new TopicMatch(topic, MatchType.VARIANT), dictionary, topic);
                addToDictionary(gene.getMutation().substring(0, gene.getMutation().length() - 1), new TopicMatch(topic, MatchType.VARIANT), dictionary, topic);

                final String normVariant = fu.normalizeString(gene.getMutation());
                addToDictionary(normGene + normVariant, new TopicMatch(topic, MatchType.GENE_AND_VARIANT), dictionary, topic);

                String variantWoSuffix = normVariant.substring(0, normVariant.length() - 1);
                addToDictionary(normGene + variantWoSuffix, new TopicMatch(topic, MatchType.GENE_AND_VARIANT), dictionary, topic);
            }
        }
    }

    /**
     * Initialization. Helper method to add dictionary entries to find the topic fields in the CAS.
     *
     * @param keyword    The new dictionary entry.
     * @param tm         The value that matches with the given keyword should return.
     * @param dictionary The dictionary to add the new entry to.
     * @param topic      The topic.
     */
    private void addToDictionary(String keyword, TopicMatch tm, Map<String, Map<String, TopicMatch>> dictionary, Topic topic) {
        dictionary.compute(keyword, (k, v) -> v == null ? new HashMap<>() : v).put(topic.getCrossDatasetId(), tm);
    }

    /**
     * <p>This is this classes main method. It uses the dictionary built in the constructor and applied it to the CAS.</p>
     * <p>
     * New annotations of the types {@link Disease} or {@link Gene} are created on topic disease or gene
     * dictionary matches, respectively. The {@link ConceptMention#getSpecificType()} feature is set to the
     * respective entry from {@link MatchType}. The {@link de.julielab.jcore.types.Annotation#getComponentId()}
     * is set to the canonical name of this class.
     * </p>
     *
     * @param jCas  The document CAS.
     * @param topic The topic of the document. Only dictionary matches regarding this topic will be added to the CAS.
     */
    public void annotate(JCas jCas, Topic topic) {
        if (!knownTopicNumbers.contains(topic.getCrossDatasetId()))
            throw new IllegalStateException("The topic " + topic.getCrossDatasetId() + " should be matched to a document but this class was not initialized with this topic.");
        List<TopicMatch> matches = new ArrayList<>();
        final String haystack = FeatureUtils.getInstance().normalizeString(jCas.getDocumentText());
        matcher.match(haystack, (key, start, end, value) -> {
            if (value.containsKey(topic.getCrossDatasetId())) {
                final TopicMatch topicMatch;
                try {
                    topicMatch = value.get(topic.getCrossDatasetId()).clone();
                    topicMatch.setKeyword(key.substring(start, end));
                    topicMatch.setBegin(start);
                    topicMatch.setEnd(end);
                    matches.add(topicMatch);
                } catch (CloneNotSupportedException e) {
                    log.error("Cloning the TopicMatch object failed", e);
                }
            }
            return true;
        });
        for (TopicMatch tm : matches) {
            ConceptMention cm;
            switch (tm.getMatchType()) {
                case DISEASE:
                    cm = new Disease(jCas, tm.getBegin(), tm.getEnd());
                    break;
                case GENE:
                    cm = new Gene(jCas, tm.getBegin(), tm.getEnd());
                    break;
                case VARIANT:
                    cm = new Gene(jCas, tm.getBegin(), tm.getEnd());
                    break;
                case GENE_AND_VARIANT:
                    cm = new Gene(jCas, tm.getBegin(), tm.getEnd());
                    break;
                default:
                    throw new IllegalStateException("Unhandled match type " + tm.getMatchType());
            }
            cm.setSpecificType(tm.getMatchType().name());
            cm.setComponentId(getClass().getCanonicalName());
            cm.addToIndexes();
        }
    }


    /**
     * <p>
     * This is a helper class to contain all information associated with a dictionary entry: What is the
     * topic the keyword was from, which field or fields contributed to the match?
     * </p>
     * <p>After a successful match, objects of this class are cloned and set the actually hit keyword and the
     * begin and end offsets of the match. The cloning avoids changes in the original object.</p>
     */
    private class TopicMatch implements Cloneable {
        private Topic topic;
        private MatchType matchType;
        private String keyword;
        private int begin;
        private int end;

        public TopicMatch(Topic topic, MatchType matchType) {

            this.topic = topic;
            this.matchType = matchType;
        }

        @Override
        protected TopicMatch clone() throws CloneNotSupportedException {
            TopicMatch clone = (TopicMatch) super.clone();
            return clone;
        }

        public int getBegin() {
            return begin;
        }

        public void setBegin(int begin) {
            this.begin = begin;
        }

        public int getEnd() {
            return end;
        }

        public void setEnd(int end) {
            this.end = end;
        }

        public TopicMatch setKeyword(String keyword) {
            this.keyword = keyword;
            return this;
        }

        public Topic getTopic() {
            return topic;
        }

        public MatchType getMatchType() {

            return matchType;
        }

        @Override
        public String toString() {
            return "TopicMatch{" +
                    "topic=" + topic.getCrossDatasetId() +
                    ", matchType=" + matchType +
                    ", keyword='" + keyword + '\'' +
                    '}';
        }
    }
}
