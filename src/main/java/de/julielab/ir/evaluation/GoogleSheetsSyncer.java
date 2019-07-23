package de.julielab.ir.evaluation;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.retrieval.Retrieval;
import at.medunigraz.imi.bst.trec.ClinicalTrialsExperimenter;
import at.medunigraz.imi.bst.trec.experiment.TrecPmRetrieval;
import at.medunigraz.imi.bst.trec.model.*;
import de.julielab.ir.goldstandards.GoogleSheetsGoldStandard;
import de.julielab.ir.goldstandards.TrecQrelGoldStandard;
import de.julielab.ir.ltr.DocumentList;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class GoogleSheetsSyncer {

    private static final Challenge CHALLENGE = Challenge.TREC_PM;
    private static final int YEAR = 2019;
    private static final int SIZE = 5;

    private static final File TOPICS = new File(GoogleSheetsSyncer.class.getResource("/topics/topics2019.xml").getPath());
    private static final File ABSTRACTS = new File("src/main/resources/gold-standard/gsheets-abstracts-2019.qrels");
    private static final File TRIALS = new File("src/main/resources/gold-standard/gsheets-trials-2019.qrels");

    private static final File NONE_TEMPLATE = new File(
            ClinicalTrialsExperimenter.class.getResource("/templates/biomedical_articles/hpipubnone.json").getFile());

    private static final File IMPROVED_TEMPLATE = new File(
            ClinicalTrialsExperimenter.class.getResource("/templates/clinical_trials/hpictboost.json").getFile());
    private static final File PHRASE_TEMPLATE = new File(
            ClinicalTrialsExperimenter.class.getResource("/templates/clinical_trials/hpictphrase.json").getFile());

    public static void main(String[] args) {
        sync(Task.PUBMED);
        sync(Task.CLINICAL_TRIALS);
    }

    private static void sync(Task task) {
        GoogleSheetsGoldStandard<Topic> sheet = download(task);
        upload(sheet);
    }

    private static GoogleSheetsGoldStandard<Topic> download(Task task) {
        List<Topic> topics = new TopicSet(TOPICS, CHALLENGE, YEAR).getTopics();

        // TODO Move to GoldStandardBuilder (#17)
        String[] readRange = null;
        String writeRange = null;
        File file = null;
        switch (task) {
            case PUBMED:
                readRange = new String[]{"Scientific Abstracts!B:D", "Scientific Abstracts!F:F"};
                writeRange = "Scientific Abstracts!B:E";
                file = ABSTRACTS;
                break;
            case CLINICAL_TRIALS:
                readRange = new String[]{"Clinical Trials!B:D", "Clinical Trials!F:F"};
                writeRange = "Clinical Trials!B:E";
                file = TRIALS;
                break;
            default:
                throw new IllegalArgumentException("Task not supported");
        }

        // Create and load data from a Google Spreadsheet
        GoogleSheetsGoldStandard<Topic> sheet = new GoogleSheetsGoldStandard<>(CHALLENGE, task, YEAR, topics, TrecConfig.GSHEETS_SHEETID, readRange, writeRange);

        // Save gold standard to a file
        TrecQrelGoldStandard<Topic> qrels = new TrecQrelGoldStandard<>(CHALLENGE, task, YEAR, topics, sheet.getQrelDocuments());
        qrels.writeQrelFile(file);

        return sheet;
    }

    private static void upload(GoogleSheetsGoldStandard<Topic> sheet) {
        // TODO Experiment Registry (#29)
        Set<Retrieval> retrievalSet = new LinkedHashSet<>();
        switch (sheet.getTask()) {
            case PUBMED:
                // hpipubnone
                retrievalSet.add(new TrecPmRetrieval().withSize(SIZE).withYear(2019).withTarget(sheet.getTask())
                        .withSubTemplate(NONE_TEMPLATE).withWordRemoval().withGeneSynonym().withDiseasePreferredTerm()
                        .withGeneDescription().withDiseaseSynonym());
                // hpipubcommon
                retrievalSet.add(new TrecPmRetrieval().withSize(SIZE).withYear(2019).withTarget(sheet.getTask())
                        .withSubTemplate(NONE_TEMPLATE).withWordRemoval().withGeneSynonym().withDiseasePreferredTerm()
                        .withDiseaseSynonym());
                break;
            case CLINICAL_TRIALS:
                // hpictall
                retrievalSet.add(new TrecPmRetrieval().withSize(SIZE).withYear(2019).withTarget(sheet.getTask())
                        .withSubTemplate(IMPROVED_TEMPLATE).withWordRemoval().withSolidTumor().withDiseasePreferredTerm()
                        .withDiseaseSynonym().withGeneSynonym().withGeneDescription().withGeneFamily());
                // hpictphrase
                retrievalSet.add(new TrecPmRetrieval().withSize(SIZE).withYear(2019).withTarget(sheet.getTask())
                        .withSubTemplate(PHRASE_TEMPLATE).withWordRemoval().withSolidTumor().withDiseasePreferredTerm()
                        .withDiseaseSynonym().withGeneSynonym().withGeneFamily());
                break;
            default:
                throw new IllegalArgumentException("Task not supported");
        }

        // Run a set of experiments and aggregate
        for (Retrieval retrieval : retrievalSet) {
            List<ResultList<Topic>> resultsPerTopic = retrieval.retrieve(sheet.getQueriesAsList(), sheet.getQueryIdFunction());

            DocumentList<Topic> sheetData = sheet.getQrelDocuments();
            for (ResultList<Topic> resultList : resultsPerTopic) {
                sheetData.addAll(resultList.toDocumentList());
            }
        }

        // Upload data
        sheet.sync();
    }
}
