package de.julielab.ir.evaluation;

import at.medunigraz.imi.bst.trec.PubmedExperimenter;
import at.medunigraz.imi.bst.trec.experiment.Experiment;
import at.medunigraz.imi.bst.trec.experiment.TrecPmRetrieval;
import at.medunigraz.imi.bst.trec.model.*;
import at.medunigraz.imi.bst.trec.stats.CSVStatsWriter;
import ciir.umass.edu.learning.RANKER_TYPE;
import ciir.umass.edu.metric.METRIC;
import com.wcohen.ss.TFIDF;
import de.julielab.ir.OriginalDocumentRetrieval;
import de.julielab.ir.TfIdfManager;
import de.julielab.ir.goldstandards.AggregatedTrecQrelGoldStandard;
import de.julielab.ir.goldstandards.TrecQrelGoldStandard;
import de.julielab.ir.ltr.DocumentList;
import de.julielab.ir.ltr.RankLibRanker;
import de.julielab.ir.ltr.features.FeatureControlCenter;
import de.julielab.java.utilities.ConfigurationUtilities;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class TrecPM1718LitCrossval {

    public static final int CROSSVAL_SIZE = 5;
    private static Logger log = LogManager.getLogger();

    public static void main(String args[]) throws ConfigurationException {

        RANKER_TYPE rType = RANKER_TYPE.COOR_ASCENT;
        METRIC trainMetric = METRIC.NDCG;
        int k = 10;

        FeatureControlCenter.initialize(ConfigurationUtilities.createEmptyConfiguration());

        File topicsFile2017 = new File(CSVStatsWriter.class.getResource("/topics/topics2017.xml").getPath());
        final TopicSet topics2017 = new TopicSet(topicsFile2017, Challenge.TREC_PM, Task.PUBMED, 2017);
        File topicsFile2018 = new File(CSVStatsWriter.class.getResource("/topics/topics2018.xml").getPath());
        final TopicSet topics2018 = new TopicSet(topicsFile2018, Challenge.TREC_PM, Task.PUBMED, 2018);

        final TrecQrelGoldStandard<Topic> trecPmLit2017 = new TrecQrelGoldStandard<>(Challenge.TREC_PM, Task.PUBMED, 2017, topics2017.getTopics(), Path.of("src", "main", "resources", "gold-standard", "qrels-treceval-abstracts.2017.txt").toFile(), Path.of("src", "main", "resources", "gold-standard", "sample-qrels-final-abstracts.2017.txt").toFile());
        final TrecQrelGoldStandard<Topic> trecPmLit2018 = new TrecQrelGoldStandard<>(Challenge.TREC_PM, Task.PUBMED, 2018, topics2018.getTopics(), Path.of("src", "main", "resources", "gold-standard", "qrels-treceval-abstracts.2018.txt").toFile(), Path.of("src", "main", "resources", "gold-standard", "qrels-sample-abstracts.2018.txt").toFile());
        final AggregatedTrecQrelGoldStandard<Topic> aggregatedGoldStandard = new AggregatedTrecQrelGoldStandard<>(Path.of("aggregatedQrels", "trecPmLit2017-2018.qrel").toFile(), Path.of("aggregatedQrels", "sampleTrecPmLit2017-2018.qrel").toFile(), trecPmLit2017, trecPmLit2018);

        final List<List<Topic>> topicPartitioning = aggregatedGoldStandard.createStratifiedTopicPartitioning(CROSSVAL_SIZE, Topic::getDisease);

        final File noClassifierTemplate = new File(
                PubmedExperimenter.class.getResource("/templates/biomedical_articles/hpipubnone.json").getFile());
        final TrecPmRetrieval retrieval = new TrecPmRetrieval().withTarget(Task.PUBMED).withGoldStandard(GoldStandard.OFFICIAL).withYear(2017).withResultsDir("myresultsdir/").withSubTemplate(noClassifierTemplate).withGeneSynonym().withDiseaseSynonym();

        List<Double> rankLibScores = new ArrayList<>();
        List<Metrics> allMetrics = new ArrayList<>();
        int[] features = null;
        final int numfeatures = 50;
        features = new int[numfeatures];
        for (int i = 0; i < numfeatures; i++)
            features[i] = i;
        for (int i = 0; i < CROSSVAL_SIZE; i++) {
            log.info("Crossval round {}", i);
            int thisround = i;
            List<Topic> test = topicPartitioning.get(i);
            final List<Topic> train = IntStream.range(0, CROSSVAL_SIZE).filter(round -> round != thisround).mapToObj(topicPartitioning::get).flatMap(Collection::stream).collect(Collectors.toList());
            final DocumentList<Topic> testDocs = aggregatedGoldStandard.getQrelDocumentsForQueries(test);
            final DocumentList<Topic> trainDocs = aggregatedGoldStandard.getQrelDocumentsForQueries(train);

            final Stream<String> trainDocumentText = OriginalDocumentRetrieval.getInstance().getDocumentText(trainDocs);
            final TFIDF trainTfIdf = TfIdfManager.getInstance().trainAndSetTfIdf("train" + i, trainDocumentText);

            FeatureControlCenter.getInstance().createFeatures(trainDocs, trainTfIdf);
            FeatureControlCenter.getInstance().createFeatures(testDocs, trainTfIdf);

            final RankLibRanker<Topic> ranker = new RankLibRanker<>(rType, features, trainMetric, k, null);
            ranker.train(trainDocs);
            final DocumentList<Topic> result = ranker.rank(testDocs);
            final double rankLibScore = ranker.score(result, METRIC.NDCG, 10);
            rankLibScores.add(rankLibScore);

            retrieval.withExperimentName("pmround" + i);
            final Experiment experiment = new Experiment();
            experiment.setGoldDataset(aggregatedGoldStandard);
            experiment.setTopicSet(new TopicSet(test));
            experiment.setRetrieval(retrieval);
            experiment.setGoldStandard(GoldStandard.OFFICIAL);
            experiment.setCalculateTrecEvalWithMissingResults(false);
            experiment.run();

            allMetrics.add(experiment.getAllMetrics());
        }

        for (Metrics m : allMetrics) {
            System.out.println(m.getInfNDCG());
        }
        System.out.println("Mean: " + allMetrics.stream().mapToDouble(Metrics::getInfNDCG).average());
    }
}
