package de.julielab.ir.goldstandards;

import at.medunigraz.imi.bst.trec.model.*;

import java.io.File;

public final class TrecPMGoldStandardFactory {

    public static TrecQrelGoldStandard<Topic> trialsOfficial2017() {
        // No sample qrels for CT in 2017
        return gs(Task.CLINICAL_TRIALS, 2017, GoldStandardType.OFFICIAL,
                new File(TrecPMGoldStandardFactory.class.getResource("/gold-standard/qrels-treceval-clinical_trials.2017.txt").getPath()));
    }

    public static TrecQrelGoldStandard<Topic> trialsOfficial2018() {
        return gs(Task.CLINICAL_TRIALS, 2018, GoldStandardType.OFFICIAL,
                new File(TrecPMGoldStandardFactory.class.getResource("/gold-standard/qrels-sample-ct.2018.txt").getPath()));
    }

    public static AggregatedTrecQrelGoldStandard<Topic> trialsOfficialAggregated() {
        return new AggregatedTrecQrelGoldStandard<>(trialsOfficial2017(), trialsOfficial2018());
    }

    public static TrecQrelGoldStandard<Topic> trialsInternal2019() {
        return gs(Task.CLINICAL_TRIALS, 2019, GoldStandardType.INTERNAL,
                new File(TrecPMGoldStandardFactory.class.getResource("/internal/gold-standard/gsheets-trials-2019.qrels").getPath()));
    }

    public static TrecQrelGoldStandard<Topic> pubmedOfficial2017() {
        return gs(Task.PUBMED, 2017, GoldStandardType.OFFICIAL,
                new File(TrecPMGoldStandardFactory.class.getResource("/gold-standard/sample-qrels-final-abstracts.2017.txt").getPath()));
    }

    public static TrecQrelGoldStandard<Topic> pubmedOfficial2018() {
        return gs(Task.PUBMED, 2018, GoldStandardType.OFFICIAL,
                new File(TrecPMGoldStandardFactory.class.getResource("/gold-standard/qrels-sample-abstracts.2018.txt").getPath()));
    }

    public static AggregatedTrecQrelGoldStandard<Topic> pubmedOfficialAggregated() {
        return new AggregatedTrecQrelGoldStandard<>(pubmedOfficial2017(), pubmedOfficial2018());
    }

    public static TrecQrelGoldStandard<Topic> pubmedInternal2019() {
        return gs(Task.PUBMED, 2019, GoldStandardType.INTERNAL,
                new File(TrecPMGoldStandardFactory.class.getResource("/internal/gold-standard/gsheets-abstracts-2019.qrels").getPath()));
    }

    /**
     * Private helper method.
     *
     * @param task
     * @param year
     * @param type  Kept for eventual use in the `TrecQrelGoldStandard` class.
     * @param qrels
     * @return
     */
    private static TrecQrelGoldStandard<Topic> gs(Task task, int year, GoldStandardType type, File qrels) {
        return new TrecQrelGoldStandard<>(Challenge.TREC_PM, task, year, type, TrecPMTopicSetFactory.topics(year).getTopics(), qrels);
    }
}
