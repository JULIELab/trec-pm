package de.julielab.ir.evaluation;

import at.medunigraz.imi.bst.trec.PubmedExperimenter;
import at.medunigraz.imi.bst.trec.evaluator.TrecEval;
import at.medunigraz.imi.bst.trec.experiment.Experiment;
import at.medunigraz.imi.bst.trec.experiment.ExperimentsBuilder;
import at.medunigraz.imi.bst.trec.experiment.TrecPmRetrieval;
import at.medunigraz.imi.bst.trec.model.*;
import at.medunigraz.imi.bst.trec.stats.CSVStatsWriter;
import de.julielab.ir.goldstandards.AggregatedGoldStandard;
import de.julielab.ir.goldstandards.TrecQrelGoldStandard;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public class TrecPM1718LitCrossval {
    public static void main(String args[]) {
        File topicsFile2017 = new File(CSVStatsWriter.class.getResource("/topics/topics2017.xml").getPath());
        final TopicSet topics2017 = new TopicSet(topicsFile2017, Challenge.TREC_PM, Task.PUBMED, 2017);
        File topicsFile2018 = new File(CSVStatsWriter.class.getResource("/topics/topics2018.xml").getPath());
        final TopicSet topics2018 = new TopicSet(topicsFile2018, Challenge.TREC_PM, Task.PUBMED, 2018);

        final TrecQrelGoldStandard<Topic> trecPmLit2017 = new TrecQrelGoldStandard<>(Challenge.TREC_PM, Task.PUBMED, 2017, topics2017.getTopics(), Path.of("src","main","resources","gold-standard","qrels-treceval-abstracts.2017.txt").toFile());
        final TrecQrelGoldStandard<Topic> trecPmLit2018 = new TrecQrelGoldStandard<>(Challenge.TREC_PM, Task.PUBMED, 2018, topics2018.getTopics(),Path.of("src","main","resources","gold-standard","qrels-treceval-abstracts.2018.txt").toFile());
        final AggregatedGoldStandard<Topic> aggregatedGoldStandard = new AggregatedGoldStandard<>(trecPmLit2017, trecPmLit2018);

        final List<List<Topic>> topicPartitioning = aggregatedGoldStandard.createStratifiedTopicPartitioning(5, Topic::getDisease);

        final File noClassifierTemplate = new File(
                PubmedExperimenter.class.getResource("/templates/biomedical_articles/hpipubnone.json").getFile());
        final TrecPmRetrieval retrieval = new TrecPmRetrieval().withTarget(Task.PUBMED).withGoldStandard(GoldStandard.OFFICIAL).withYear(2017).withResultsDir("myresultsdir").withSubTemplate(noClassifierTemplate).withGeneSynonym();;

        final List<Result> query = retrieval.retrieve(topics2017.getTopics().get(0));
        for (Result r : query) {
            System.out.println(r.getId() + " " + r.getScore());
        }
        final TrecEval trecEval = new TrecEval(trecPmLit2017.getQrelFile(), new File("myresult.txt"));
        System.out.println(trecEval.getNDCG());
         //train and eval
    }
}
