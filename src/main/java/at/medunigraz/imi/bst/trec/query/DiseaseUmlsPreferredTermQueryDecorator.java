package at.medunigraz.imi.bst.trec.query;

import at.medunigraz.imi.bst.retrieval.Query;
import at.medunigraz.imi.bst.trec.model.Topic;
import de.julielab.ir.umls.UmlsSynsetProvider;

public class DiseaseUmlsPreferredTermQueryDecorator extends DynamicQueryDecorator {
    private transient UmlsSynsetProvider umlsSynsetProvider;
    public DiseaseUmlsPreferredTermQueryDecorator(Query decoratedQuery) {
        super(decoratedQuery);
    }

    /**
     * For tests.
     *
     * @param umlsSynsetProvider The synset provider to use.
     */
    void setUmlsSynsetProvider(UmlsSynsetProvider umlsSynsetProvider) {
        this.umlsSynsetProvider = umlsSynsetProvider;
    }

    @Override
    public Topic expandTopic(Topic topic) {
        if (umlsSynsetProvider == null)
            umlsSynsetProvider = UmlsSynsetProvider.getInstance();
        String disease = topic.getDisease();
        final String preferredTerm = umlsSynsetProvider.getPreftermForTerm(disease);
        topic.withDiseasePreferredTerm(preferredTerm);
        return topic;
    }

}
