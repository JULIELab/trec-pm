package de.julielab.ir.evaluation;

import at.medunigraz.imi.bst.trec.PubmedExperimenter;
import at.medunigraz.imi.bst.trec.evaluator.TrecWriter;
import at.medunigraz.imi.bst.trec.experiment.Experiment;
import at.medunigraz.imi.bst.trec.experiment.TrecMetricsCreator;
import at.medunigraz.imi.bst.trec.experiment.TrecPmRetrieval;
import at.medunigraz.imi.bst.trec.model.*;
import at.medunigraz.imi.bst.trec.stats.CSVStatsWriter;
import ciir.umass.edu.learning.RANKER_TYPE;
import ciir.umass.edu.metric.METRIC;
import com.wcohen.ss.TFIDF;
import de.julielab.ir.OriginalDocumentRetrieval;
import de.julielab.ir.TfIdfManager;
import de.julielab.ir.VocabularyRestrictor;
import de.julielab.ir.goldstandards.AggregatedTrecQrelGoldStandard;
import de.julielab.ir.goldstandards.TrecQrelGoldStandard;
import de.julielab.ir.ltr.Document;
import de.julielab.ir.ltr.DocumentList;
import de.julielab.ir.ltr.RankLibRanker;
import de.julielab.ir.ltr.features.FeatureControlCenter;
import de.julielab.ir.ltr.features.IRScore;
import de.julielab.java.utilities.ConfigurationUtilities;
import de.julielab.java.utilities.FileUtilities;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.factory.Sets;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TrecPM1718LitRecallCrossval {

    public static final int CROSSVAL_SIZE = 5;
    private static Logger log = LogManager.getLogger();

    public static void main(String args[]) throws ConfigurationException, IOException {

        final File noClassifierTemplate = new File(
                PubmedExperimenter.class.getResource("/templates/biomedical_articles/hpipubnone.json").getFile());
        final TrecPmRetrieval retrieval = new TrecPmRetrieval().withTarget(Task.PUBMED).withGoldStandard(GoldStandard.OFFICIAL).withYear(2017).withResultsDir("myresultsdir/").withSubTemplate(noClassifierTemplate).withGeneSynonym().withDiseaseSynonym();

        String experimentName = "Base";
        File topicsFile2017 = new File(CSVStatsWriter.class.getResource("/topics/topics2017.xml").getPath());
        final TopicSet topics2017 = new TopicSet(topicsFile2017, Challenge.TREC_PM, Task.PUBMED, 2017);
        File topicsFile2018 = new File(CSVStatsWriter.class.getResource("/topics/topics2018.xml").getPath());
        final TopicSet topics2018 = new TopicSet(topicsFile2018, Challenge.TREC_PM, Task.PUBMED, 2018);

        final TrecQrelGoldStandard<Topic> trecPmLit2017 = new TrecQrelGoldStandard<>(Challenge.TREC_PM, Task.PUBMED, 2017, topics2017.getTopics(), Path.of("src", "main", "resources", "gold-standard", "qrels-treceval-abstracts.2017.txt").toFile(), Path.of("src", "main", "resources", "gold-standard", "sample-qrels-final-abstracts.2017.txt").toFile());
        final TrecQrelGoldStandard<Topic> trecPmLit2018 = new TrecQrelGoldStandard<>(Challenge.TREC_PM, Task.PUBMED, 2018, topics2018.getTopics(), Path.of("src", "main", "resources", "gold-standard", "qrels-treceval-abstracts.2018.txt").toFile(), Path.of("src", "main", "resources", "gold-standard", "qrels-sample-abstracts.2018.txt").toFile());
        final AggregatedTrecQrelGoldStandard<Topic> aggregatedGoldStandard = new AggregatedTrecQrelGoldStandard<>(Path.of("aggregatedQrels", "trecPmLit2017-2018.qrel").toFile(), Path.of("aggregatedQrels", "sampleTrecPmLit2017-2018.qrel").toFile(), trecPmLit2017, trecPmLit2018);

        final List<List<Topic>> topicPartitioning = aggregatedGoldStandard.createStratifiedTopicPartitioning(CROSSVAL_SIZE, Topic::getDisease);


        runRecallExperiment(retrieval, experimentName, new File("recallResults"), aggregatedGoldStandard, topicPartitioning);
    }

    private static void runRecallExperiment(TrecPmRetrieval retrieval, String experimentName, File resultDir, AggregatedTrecQrelGoldStandard<Topic> aggregatedGoldStandard, List<List<Topic>> topicPartitioning) {
        List<Metrics> allESMetrics = new ArrayList<>();
        Map<Topic, Double> allRecall = new HashMap<>();
        List<Double> meanRecallPerRound = new ArrayList<>();
        for (int i = 0; i < CROSSVAL_SIZE; i++) {
            String roundName = experimentName + "-Round" + i;
            List<Topic> test = topicPartitioning.get(i);
            retrieval.withExperimentName(roundName);

            final Experiment<Topic> experiment = new Experiment();
            experiment.setGoldDataset(aggregatedGoldStandard);
            experiment.setTopicSet(new TopicSet(test));
            experiment.setRetrieval(retrieval);
            experiment.setGoldStandard(GoldStandard.OFFICIAL);
            experiment.setCalculateTrecEvalWithMissingResults(false);
            experiment.run();
            allESMetrics.add(experiment.getAllMetrics());

            final Map<Topic, ResultList<Topic>> resultListsByTopic = experiment.getLastResultListSet().stream().collect(Collectors.toMap(ResultList::getTopic, Function.identity()));
            double meanRecall = 0;
            for (Topic t : test) {
                final ResultList<Topic> resultList = resultListsByTopic.get(t);
                final Set<String> foundDocuments = resultList.getResults().stream().map(Result::getId).collect(Collectors.toSet());
                final Set<String> relevantDocuments = aggregatedGoldStandard.getQrelDocumentsForQuery(t).stream().filter(d -> d.getRelevance() > 0).map(Document::getId).collect(Collectors.toSet());
                final MutableSet<String> foundRelevantDocuments = Sets.intersect(foundDocuments, relevantDocuments);
                double recall = foundRelevantDocuments.size() / (double) relevantDocuments.size();
                meanRecall += recall;
                allRecall.put(t, recall);
            }
            meanRecall /= test.size();
            meanRecallPerRound.add(meanRecall);

        }

        for (Metrics m : allESMetrics) {
            System.out.println(m.getInfNDCG());
        }
        System.out.println("Recall:" + allRecall);
        System.out.println("Mean infNDCG: " + allESMetrics.stream().mapToDouble(Metrics::getInfNDCG).average());
        System.out.println("Mean recall: " + allRecall.values().stream().mapToDouble(d -> d).average());

        if (!resultDir.exists())
            resultDir.mkdirs();

        try(BufferedWriter bw = FileUtilities.getWriterToFile(Path.of(resultDir.getAbsolutePath(), experimentName + ".tsv").toFile())) {
            for (Topic t : allRecall.keySet()) {
                double recall = allRecall.get(t);
                bw.write(aggregatedGoldStandard.getQueryIdFunction().apply(t) + "\t" + recall);
                bw.newLine();
            }
            bw.newLine();
            bw.write("avg\t"+allRecall.values().stream().mapToDouble(d -> d).average().getAsDouble());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
