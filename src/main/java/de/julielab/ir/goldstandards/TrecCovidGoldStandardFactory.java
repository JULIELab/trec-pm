package de.julielab.ir.goldstandards;

import at.medunigraz.imi.bst.trec.model.Challenge;
import at.medunigraz.imi.bst.trec.model.GoldStandardType;
import at.medunigraz.imi.bst.trec.model.Task;
import at.medunigraz.imi.bst.trec.model.TrecCovidTopicSetFactory;
import de.julielab.ir.model.CovidTopic;

public class TrecCovidGoldStandardFactory {
    public static TrecQrelGoldStandard<CovidTopic> round1() {
        return new TrecQrelGoldStandard<>(Challenge.COVID, Task.CORD19, 1, GoldStandardType.OFFICIAL, TrecCovidTopicSetFactory.topicsRound1(), "/gold-standard/qrels-covid19-rnd1.txt");
    }
    public static TrecQrelGoldStandard<CovidTopic> round2() {
        return new TrecQrelGoldStandard<>(Challenge.COVID, Task.CORD19, 2, GoldStandardType.OFFICIAL, TrecCovidTopicSetFactory.topicsRound2(), "/gold-standard/qrels-covid19-rnd2.txt");
    }

    /**
     * The qrels used to score the round 3 submissions. Thus, the cord19_uids in this gold standard match the documents
     * of the respective challenge data.
     * @return The gold standard.
     */
    public static TrecQrelGoldStandard<CovidTopic> round3() {
        return new TrecQrelGoldStandard<>(Challenge.COVID, Task.CORD19, 3, GoldStandardType.OFFICIAL, TrecCovidTopicSetFactory.topicsRound2(), "/gold-standard/qrels-covid_d3_j2.5-3.txt");
    }

    /**
     * A cumulative gold standard up to round 3. It contains the complete set of relevance judgments from rounds
     * 1 to 3 mapped to the cord19_uids that were valid at round 3. So this gold standard can be used to evaluate
     * on the complete data between rounds 1 and 3, including documents that were left out from rounds 2 and 3
     * due to residual evaluation.
     * @return The gold standard.
     */
    public static TrecQrelGoldStandard<CovidTopic> round3Cumulative() {
        return new TrecQrelGoldStandard<>(Challenge.COVID, Task.CORD19, 3, GoldStandardType.OFFICIAL, TrecCovidTopicSetFactory.topicsRound2(), "/gold-standard/qrels-covid_d3_j0.5-3.txt");
    }
}
