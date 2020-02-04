package de.julielab.ir.goldstandards;

import at.medunigraz.imi.bst.trec.model.*;

import java.io.File;
import java.io.IOException;

public final class TrecPMGoldStandardFactory {

    public static TrecQrelGoldStandard<Topic> trialsOfficial2017() {
        // No sample qrels for CT in 2017
        return gs(Task.CLINICAL_TRIALS, 2017, GoldStandardType.OFFICIAL,
                "/gold-standard/qrels-treceval-clinical_trials.2017.txt", new File("config", "costosys-ct1718.xml"));
    }

    public static TrecQrelGoldStandard<Topic> trialsOfficial2018() {
        return gs(Task.CLINICAL_TRIALS, 2018, GoldStandardType.OFFICIAL,
                "/gold-standard/qrels-sample-ct.2018.txt", new File("config", "costosys-ct1718.xml"));
    }

    public static TrecQrelGoldStandard<Topic> trialsOfficial2019() {
        return gs(Task.CLINICAL_TRIALS, 2019, GoldStandardType.OFFICIAL,
                "/gold-standard/qrels-sampleval-trials.2019.txt", new File("config", "costosys-ct1718.xml"));
    }

    public static AggregatedTrecQrelGoldStandard<Topic> trialsOfficialAggregated() {
        return new AggregatedTrecQrelGoldStandard<>(trialsOfficial2017(), trialsOfficial2018());
    }

    public static TrecQrelGoldStandard<Topic> trialsInternal2019() {
        return gs(Task.CLINICAL_TRIALS, 2019, GoldStandardType.INTERNAL,
                "/internal/gold-standard/gsheets-trials-2019.qrels", new File("config", "costosys-ct19.xml"));
    }

    public static TrecQrelGoldStandard<Topic> pubmedOfficial2017() {
        return gs(Task.PUBMED, 2017, GoldStandardType.OFFICIAL,
                "/gold-standard/sample-qrels-final-abstracts.2017.txt", new File("config", "costosys-pm1718.xml"));
    }

    public static TrecQrelGoldStandard<Topic> pubmedOfficial2018() {
        return gs(Task.PUBMED, 2018, GoldStandardType.OFFICIAL,
                "/gold-standard/qrels-sample-abstracts.2018.txt", new File("config", "costosys-pm1718.xml"));
    }

    public static TrecQrelGoldStandard<Topic> pubmedOfficial2019() {
        return gs(Task.PUBMED, 2019, GoldStandardType.OFFICIAL,
                "/gold-standard/qrels-sampleval-abstracts.2019.txt", new File("config", "costosys-pm19.xml"));
    }

    public static AggregatedTrecQrelGoldStandard<Topic> pubmedOfficialAggregated() {
        return new AggregatedTrecQrelGoldStandard<>(pubmedOfficial2017(), pubmedOfficial2018());
    }

    public static TrecQrelGoldStandard<Topic> pubmedInternal2019() {
        return gs(Task.PUBMED, 2019, GoldStandardType.INTERNAL,
                "/internal/gold-standard/gsheets-abstracts-2019.qrels", new File("config", "costosys-pm19.xml"));
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
    private static TrecQrelGoldStandard<Topic> gs(Task task, int year, GoldStandardType type, String qrels, File documentDbConfiguration) {
        TrecQrelGoldStandard<Topic> gs = new TrecQrelGoldStandard<>(Challenge.TREC_PM, task, year, type, TrecPMTopicSetFactory.topics(year).getTopics(), qrels);
        try {
            if (documentDbConfiguration != null)
                gs.setDocumentDatabaseConfiguration(documentDbConfiguration);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return gs;
    }
}
