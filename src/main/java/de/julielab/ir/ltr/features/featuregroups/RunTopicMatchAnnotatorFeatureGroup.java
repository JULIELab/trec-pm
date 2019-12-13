package de.julielab.ir.ltr.features.featuregroups;


import at.medunigraz.imi.bst.trec.model.Topic;
import at.medunigraz.imi.bst.trec.query.*;
import de.julielab.ir.ltr.Document;
import de.julielab.ir.ltr.features.FeatureGroup;
import de.julielab.ir.ltr.features.FeatureValueAssigner;
import de.julielab.ir.ltr.features.TopicFieldsCasAnnotator;
import de.julielab.ir.model.QueryDescription;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>This feature group only exist to add annotations to a document's CAS object via
 * {@link TopicFieldsCasAnnotator} to be used in {@link TopicMatchFeatureGroup}.</p>
 * <p>To this end, this feature group has the exact same name as {@link TopicMatchFeatureGroup},
 * namely {@link TopicMatchFeatureGroup#GROUP_NAME}. In this way, both feature groups will
 * always be activated or deactivated. When we don't use the features, we don't need to
 * run the annotator.</p>
 */
public class RunTopicMatchAnnotatorFeatureGroup extends FeatureGroup {
    public static final long serialVersionUID = -4602232149056363215L;
    private final static Logger log = LoggerFactory.getLogger(RunTopicMatchAnnotatorFeatureGroup.class);
    private TopicFieldsCasAnnotator topicFieldsAnnotator;
    private final DiseaseUmlsSynonymQueryDecorator decorator;

    public RunTopicMatchAnnotatorFeatureGroup() {
        // This pipe should only run when it is actually required. So let it belong to
        // the same group as the pipe that will eventually use the annotations
        super(TopicMatchFeatureGroup.GROUP_NAME);
        topicFieldsAnnotator = new TopicFieldsCasAnnotator();

        DummyElasticSearchQuery<QueryDescription> dummy = new DummyElasticSearchQuery<>();
        decorator = new DiseaseUmlsSynonymQueryDecorator(new GeneSynonymQueryDecorator(new GeneDescriptionQueryDecorator(new WordRemovalQueryDecorator(new ConditionalCancerQueryDecorator(dummy)))));

    }


    @Override
    protected void addFeatures() {
       FeatureValueAssigner annotation = inst -> {
            final Document document = (Document) inst.getSource();
            final CAS cas = document.getCas();
            try {
                Topic topic = (Topic) document.getQueryDescription();
                decorator.query(topic);
                topicFieldsAnnotator.annotate(cas.getJCas(), topic);
            } catch (CASException e) {
                log.error("Could not get the JCas from the CAS", e);
            }
        };
        addFeature("ANNOTATION", annotation);
    }
}
