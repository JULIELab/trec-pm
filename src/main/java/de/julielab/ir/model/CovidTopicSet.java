package de.julielab.ir.model;

import at.medunigraz.imi.bst.trec.model.Challenge;
import at.medunigraz.imi.bst.trec.model.QueryDescriptionSet;

import java.util.Collection;
import java.util.function.Supplier;

public class CovidTopicSet extends QueryDescriptionSet<CovidTopic> {
    public CovidTopicSet(Collection<CovidTopic> topics) {
        super(topics);
    }

    public CovidTopicSet() {
    }

    public CovidTopicSet(String xmlFile, Challenge challenge, int round) {
        super(xmlFile, challenge, round, "topic", CovidTopic::fromElement);
    }

    @Override
    public Supplier<QueryDescriptionSet<CovidTopic>> getSupplier() {
        return CovidTopicSet::new;
    }
}
