package at.medunigraz.imi.bst.trec.model;

public final class TrecPMTopicSetFactory {

    public static TopicSet topics2017() {
        return topics(2017);
    }

    public static TopicSet topics2018() {
        return topics(2018);
    }

    public static TopicSet topics2019() {
        return topics(2019);
    }

    public static TopicSet topics(int year) {
        final String topics = String.format("/topics/topics%d.xml", year);
        return new TopicSet(topics, Challenge.TREC_PM, year);
    }
}
