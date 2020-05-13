package de.julielab.ir.goldstandards;

import at.medunigraz.imi.bst.trec.model.Challenge;
import at.medunigraz.imi.bst.trec.model.GoldStandardType;
import at.medunigraz.imi.bst.trec.model.Task;
import de.julielab.ir.model.CovidTopic;
import de.julielab.ir.model.CovidTopicSet;

public class TrecCovidGoldStandardFactory {
    public static TrecQrelGoldStandard<CovidTopic> round1() {
        final String topics = String.format("/topics/topics-rnd%d-topic%d.xml", 1,19);
        CovidTopicSet covidTopics = new CovidTopicSet(topics, Challenge.COVID, 1);
        return new TrecQrelGoldStandard<>(Challenge.COVID, Task.CORD19, 1, GoldStandardType.OFFICIAL, covidTopics, "/gold-standard/qrels-covid19-rnd1.txt");
    }
}
