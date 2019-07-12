package de.julielab.ir.ltr.features;

import at.medunigraz.imi.bst.trec.model.Topic;
import com.roklenarcic.util.strings.WholeWordLongestMatchSet;
import org.apache.uima.jcas.JCas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>This class takes a UIMA CAS and a {@link Topic} and finds different kinds of topic field occurrences in the document for subsequent feature generation.</p>
 * <p>Note that this class is not a proper UIMA annotator which would extend one of UIMA's annotator classes. We just use the CAS structure for annotation.</p>
 */
public class TopicFieldsCasAnnotator {
    public void annotate(JCas jCas, Topic topic) {
        final WholeWordLongestMatchSet matcher = new WholeWordLongestMatchSet(Arrays.asList(FeatureUtils.getInstance().normalizeString(topic.getDisease())), true);
        List<String> matches = new ArrayList<>();
        matcher.match(FeatureUtils.getInstance().normalizeString(jCas.getDocumentText()), (match, start, end) -> matches.add(match));
    }
}
