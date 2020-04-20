package de.julielab.ir.model;

import at.medunigraz.imi.bst.trec.model.Challenge;
import at.medunigraz.imi.bst.trec.model.QueryDescriptionSet;

import java.util.Collection;

public class CovidTopicSet extends QueryDescriptionSet<CovidTopic> {
    public CovidTopicSet(Collection<CovidTopic> topics) {
        super(topics);
    }

    public CovidTopicSet(String xmlFile, Challenge challenge, int year) {
        super(xmlFile, challenge, year, "topic", CovidTopic::fromElement);
    }
}
