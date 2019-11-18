package de.julielab.ir.ltr.features.featuregroups;

import at.medunigraz.imi.bst.trec.model.Topic;
import at.medunigraz.imi.bst.trec.model.TopicGene;
import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import de.julielab.ir.ltr.Document;
import de.julielab.ir.ltr.features.FeatureGroup;
import de.julielab.ir.ltr.features.FeatureValueAssigner;
import de.julielab.ir.ltr.features.TopicFieldsCasAnnotator;
import de.julielab.jcore.types.Disease;
import de.julielab.jcore.types.Gene;
import de.julielab.jcore.types.PointMutation;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TopicMatchFeatureGroup extends FeatureGroup {
    public static final String MATCH_TOPIC_DISEASE = "MATCH_TOPIC_DISEASE";
    public static final String MATCH_TOPIC_GENE = "MATCH_TOPIC_GENE";
    public static final String MATCH_TOPIC_VARIANT = "MATCH_TOPIC_VARIANT";
    public static final String GROUP_NAME = "TOPIC_MATCH";
    private final static Logger log = LoggerFactory.getLogger(TopicMatchFeatureGroup.class);

    public TopicMatchFeatureGroup() {
        super(GROUP_NAME);
    }


    @Override
    protected void addFeatures() {
        addMatchTopicDisease();
        addMatchTopicGene();
        addMatchGeneVariant();
    }

    private void addMatchGeneVariant() {
        FeatureValueAssigner featureValueAssigner = instance -> {
            final Token token = (Token) instance.getData();
            final Document<Topic> document = (Document<Topic>) instance.getSource();
            final Set<String> topicVariants = Arrays.stream(document.getQueryDescription().getGenes()).map(TopicGene::getMutation).collect(Collectors.toSet());
            try {
                Multiset<String> foundVariants = HashMultiset.create();
                final JCas jCas = document.getCas().getJCas();
                for (PointMutation g : jCas.<PointMutation>getAnnotationIndex(PointMutation.type)) {
                    if (topicVariants.contains(g.getSpecificType()))
                        foundVariants.add(g.getSpecificType());
                }

                for (String variant : foundVariants.elementSet()) {
                    token.setFeatureValue(MATCH_TOPIC_VARIANT, foundVariants.count(variant));
                }
            } catch (CASException e) {
                log.error("Could not get the JCas from the CAS", e);
            }
        };
        addFeature(MATCH_TOPIC_VARIANT, featureValueAssigner);
    }


    private void addMatchTopicGene() {
        FeatureValueAssigner featureValueAssigner = instance -> {
            final Token token = (Token) instance.getData();
            final Document<Topic> document = (Document<Topic>) instance.getSource();
            try {
                Multiset<String> matchTypes = HashMultiset.create();
                final JCas jCas = document.getCas().getJCas();
                for (Gene g : jCas.<Gene>getAnnotationIndex(Gene.type)) {
                    if (g.getComponentId().equals(TopicFieldsCasAnnotator.class.getCanonicalName()))
                        // The specific type is a name from the MatchType enum
                        matchTypes.add(g.getSpecificType());
                }
                // For each match type (like GENE or GENE_AND_VARIANT)
                for (String matchType : matchTypes.elementSet()) {
                    token.setFeatureValue(matchType, matchTypes.count(matchType));
                }
            } catch (CASException e) {
                log.error("Could not get the JCas from the CAS", e);
            }
        };
        addFeature(MATCH_TOPIC_GENE, featureValueAssigner);
    }

    private void addMatchTopicDisease() {
        FeatureValueAssigner featureValueAssigner = instance -> {
            final Token token = (Token) instance.getData();
            final Document<Topic> document = (Document<Topic>) instance.getSource();
            try {
                Multiset<String> matchTypes = HashMultiset.create();
                final JCas jCas = document.getCas().getJCas();
                for (Disease d : jCas.<Disease>getAnnotationIndex(Disease.type)) {
                    if (d.getComponentId().equals(TopicFieldsCasAnnotator.class.getCanonicalName()))
                        // The specific type is a name from the MatchType enum
                        matchTypes.add(d.getSpecificType());
                }
                // For each match type (like DISEASE or DISEASE_SYNONYM)
                for (String matchType : matchTypes.elementSet()) {
                    token.setFeatureValue(matchType, matchTypes.count(matchType));
                }
            } catch (CASException e) {
                log.error("Could not get the JCas from the CAS", e);
            }
        };
        addFeature(MATCH_TOPIC_DISEASE, featureValueAssigner);
    }
}
