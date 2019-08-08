package de.julielab.ir.evaluation;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.retrieval.Retrieval;
import at.medunigraz.imi.bst.trec.experiment.registry.ClinicalTrialsRetrievalRegistry;
import at.medunigraz.imi.bst.trec.experiment.registry.LiteratureArticlesRetrievalRegistry;
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

    private static final File ABSTRACTS = new File("src/main/resources/internal/gold-standard/gsheets-abstracts-2019.qrels");
    private static final File TRIALS = new File("src/main/resources/internal/gold-standard/gsheets-trials-2019.qrels");

    public static void main(String[] args) {
        sync(Task.PUBMED);
        sync(Task.CLINICAL_TRIALS);
    }

    private static void sync(Task task) {
        GoogleSheetsGoldStandard<Topic> sheet = download(task);
        upload(sheet);
    }

    private static GoogleSheetsGoldStandard<Topic> download(Task task) {
        final List<Topic> topics = TrecPMTopicSetFactory.topics(YEAR).getTopics();

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
        TrecQrelGoldStandard<Topic> qrels = new TrecQrelGoldStandard<>(CHALLENGE, task, YEAR, GoldStandardType.INTERNAL, topics, sheet.getQrelDocuments());
        qrels.writeQrelFile(file);

        return sheet;
    }

    private static void upload(GoogleSheetsGoldStandard<Topic> sheet) {
        Set<Retrieval> retrievalSet = new LinkedHashSet<>();
        switch (sheet.getTask()) {
            case PUBMED:
                retrievalSet.add(LiteratureArticlesRetrievalRegistry.jlpmcommon2(SIZE));
                break;
            case CLINICAL_TRIALS:
                retrievalSet.add(ClinicalTrialsRetrievalRegistry.jlctphrase(SIZE));
                break;
            default:
                throw new IllegalArgumentException("Task not supported");
        }

        // Run a set of experiments and aggregate
        for (Retrieval retrieval : retrievalSet) {
            List<ResultList<Topic>> resultsPerTopic = retrieval.retrieve(sheet.getQueriesAsList(), sheet.getQueryIdFunction());

            DocumentList<Topic> sheetData = sheet.getQrelDocuments();
            for (ResultList<Topic> resultList : resultsPerTopic) {
                sheetData.addAll(DocumentList.fromRetrievalResultList(resultList));
            }
        }

        // Upload data
        sheet.sync();
    }
}
