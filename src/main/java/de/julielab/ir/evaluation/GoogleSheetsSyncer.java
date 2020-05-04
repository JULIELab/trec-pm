package de.julielab.ir.evaluation;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.retrieval.Retrieval;
import at.medunigraz.imi.bst.trec.experiment.registry.ClinicalTrialsRetrievalRegistry;
import at.medunigraz.imi.bst.trec.experiment.registry.Cord19RetrievalRegistry;
import at.medunigraz.imi.bst.trec.experiment.registry.LiteratureArticlesRetrievalRegistry;
import at.medunigraz.imi.bst.trec.model.*;
import at.medunigraz.imi.bst.trec.search.ElasticClientFactory;
import de.julielab.ir.TrecCacheConfiguration;
import de.julielab.ir.goldstandards.GoogleSheetsGoldStandard;
import de.julielab.ir.goldstandards.TrecQrelGoldStandard;
import de.julielab.ir.ltr.DocumentList;
import de.julielab.ir.ltr.TreatmentRanker;
import de.julielab.ir.model.QueryDescription;
import de.julielab.java.utilities.cache.CacheService;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class GoogleSheetsSyncer {

    private static final Challenge CHALLENGE = Challenge.COVID;
    private static final int YEAR = 2020;
    private static final int SIZE = 5;

    private static final File ABSTRACTS = new File("src/main/resources/internal/gold-standard/gsheets-abstracts-2019.qrels");
    private static final File TRIALS = new File("src/main/resources/internal/gold-standard/gsheets-trials-2019.qrels");
    private static final File CORD19 = new File("src/main/resources/internal/gold-standard/gsheets-covid-2020.qrels");

    public static void main(String[] args) throws IOException {
        CacheService.initialize(new TrecCacheConfiguration());
//        sync(Task.PUBMED, TrecPMTopicSetFactory.topics(YEAR), null);
//        sync(Task.CLINICAL_TRIALS, TrecPMTopicSetFactory.topics(YEAR), null);
        sync(Task.CORD19, TrecCovidTopicSetFactory.topicsRound1(), r -> "https://cord-19.apps.allenai.org/paper/"+r.getId());

        CacheService.getInstance().commitAllCaches();
        ElasticClientFactory.getClient().close();
    }

    private static <Q extends QueryDescription> void sync(Task task, QueryDescriptionSet<Q> topics, Function<Result, String> urlFunction) {
        GoogleSheetsGoldStandard<Q> sheet = download(task, topics);
        upload(sheet, urlFunction);
    }

    private static <Q extends QueryDescription> GoogleSheetsGoldStandard<Q> download(Task task, QueryDescriptionSet<Q> topics) {

        String[] readRange ;
        String[] writeRange;
        File file = null;
        switch (task) {
            case PUBMED:
                readRange = new String[]{"Scientific Abstracts!B:D", "Scientific Abstracts!F:F"};
                writeRange = new String[]{"Scientific Abstracts!B:E"};
                file = ABSTRACTS;
                break;
            case CLINICAL_TRIALS:
                readRange = new String[]{"Clinical Trials!B:D", "Clinical Trials!F:F"};
                writeRange = new String[]{"Clinical Trials!B:E"};
                file = TRIALS;
                break;
            case CORD19:
                readRange = new String[]{"B:D", "F:F"};
                writeRange = new String[]{"B:E", "G:G"};
                file = CORD19;
                break;
            default:
                throw new IllegalArgumentException("Task not supported");
        }

        // Create and load data from a Google Spreadsheet
        GoogleSheetsGoldStandard<Q> sheet = new GoogleSheetsGoldStandard<>(CHALLENGE, task, YEAR, topics, TrecConfig.GSHEETS_SHEETID, readRange, writeRange);

        // Save gold standard to a file
        TrecQrelGoldStandard<Q> qrels = new TrecQrelGoldStandard<>(CHALLENGE, task, YEAR, GoldStandardType.INTERNAL, topics, sheet.getQrelDocuments());
        qrels.writeQrelFile(file);

        return sheet;
    }

    private static <Q extends QueryDescription> void upload(GoogleSheetsGoldStandard<Q> sheet, Function<Result, String> urlFunction) {
        Set<Retrieval> retrievalSet = new LinkedHashSet<>();
        switch (sheet.getTask()) {
            case PUBMED:
                // Get 10x more docs for treatment ranker
                retrievalSet.add(LiteratureArticlesRetrievalRegistry.jlpmtrboost(SIZE * 10));
                break;
            case CLINICAL_TRIALS:
                retrievalSet.add(ClinicalTrialsRetrievalRegistry.jlctphrase(SIZE));
                break;
            case CORD19:
                retrievalSet.add(Cord19RetrievalRegistry.jlbaseRound1());
                break;
            default:
                throw new IllegalArgumentException("Task not supported");
        }

        // Run a set of experiments and aggregate
        for (Retrieval retrieval : retrievalSet) {
            List<ResultList<Q>> resultsPerTopic = retrieval.retrieve(sheet.getQueriesAsList(), sheet.getQueryIdFunction());

            DocumentList<Q> sheetData = sheet.getQrelDocuments();
            for (ResultList<Q> resultList : resultsPerTopic) {
                DocumentList<Q> retrieved = DocumentList.fromRetrievalResultList(resultList, retrieval.getDocIdFunction(), urlFunction);
                DocumentList<Q> ranked = new TreatmentRanker().rank(retrieved);
                // Limit ourselves to SIZE docs per topic
                for (int i = 0; i < SIZE; i++) {
                    sheetData.add(retrieved.get(i));
                    if (i < ranked.size()) {
                        sheetData.add(ranked.get(i));
                    }
                }
            }
        }

        // Upload data
        sheet.sync();
    }
}
