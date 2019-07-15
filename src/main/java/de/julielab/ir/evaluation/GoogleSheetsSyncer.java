package de.julielab.ir.evaluation;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.trec.model.Challenge;
import at.medunigraz.imi.bst.trec.model.Task;
import at.medunigraz.imi.bst.trec.model.Topic;
import at.medunigraz.imi.bst.trec.model.TopicSet;
import de.julielab.ir.goldstandards.GoogleSheetsGoldStandard;
import de.julielab.ir.goldstandards.TrecQrelGoldStandard;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoogleSheetsSyncer {

    private static final Challenge CHALLENGE = Challenge.TREC_PM;
    private static final int YEAR = 2019;

    private static final File TOPICS = new File(GoogleSheetsSyncer.class.getResource("/topics/topics2019.xml").getPath());
    private static final File ABSTRACTS = new File("src/main/resources/gold-standard/gsheets-abstracts-2019.qrels");
    private static final File TRIALS = new File("src/main/resources/gold-standard/gsheets-trials-2019.qrels");

    // TODO Move to GoldStandardBuilder (#17)
    private static final Map<Task, Pair<String, File>> TASK_MAP = new HashMap<>();
    static {
        TASK_MAP.put(Task.PUBMED, new ImmutablePair<>("Scientific Abstracts!B:E", ABSTRACTS));
        TASK_MAP.put(Task.CLINICAL_TRIALS, new ImmutablePair<>("Clinical Trials!B:E", TRIALS));
    }

    public static void main(String[] args) {
        sync(Task.PUBMED);
        sync(Task.CLINICAL_TRIALS);
    }

    private static void sync(Task task) {
        GoogleSheetsGoldStandard<Topic> sheet = download(task);
        upload(sheet);
    }

    private static GoogleSheetsGoldStandard<Topic> download(Task task) {
        List<Topic> topics = new TopicSet(TOPICS, CHALLENGE, task, YEAR).getTopics();

        Pair<String, File> taskDescription = TASK_MAP.get(task);

        // Create and load data from a Google Spreadsheet
        GoogleSheetsGoldStandard<Topic> sheet = new GoogleSheetsGoldStandard<>(CHALLENGE, task, YEAR, topics, TrecConfig.GSHEETS_SHEETID, taskDescription.getLeft());

        // Save gold standard to a file
        TrecQrelGoldStandard<Topic> qrels = new TrecQrelGoldStandard<>(CHALLENGE, task, YEAR, topics, sheet.getQrelDocuments());
        qrels.writeQrelFile(taskDescription.getRight());

        return sheet;
    }

    private static void upload(GoogleSheetsGoldStandard<Topic> sheet) {
        sheet.sync();
    }
}
