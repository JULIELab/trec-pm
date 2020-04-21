package at.medunigraz.imi.bst.trec.model;

import de.julielab.ir.model.CovidTopicSet;

public final class TrecCovidTopicSetFactory {

    public static CovidTopicSet topicsRound1() {
        return topics(1);
    }

    public static CovidTopicSet topics(int round) {
        final String topics = String.format("/topics/topics-rnd%d.xml", round);
        return new CovidTopicSet(topics, Challenge.COVID, 1);
    }
}
