package at.medunigraz.imi.bst.pmclassifier.apps;

import at.medunigraz.imi.bst.pmclassifier.*;
import at.medunigraz.imi.bst.pmclassifier.lucene.LuceneClassifier;
import cc.mallet.types.FeatureSelection;
import cc.mallet.types.InfoGain;
import cc.mallet.types.InstanceList;
import de.julielab.jcore.ae.topicindexing.TopicIndexer;
import de.julielab.jcore.ae.topicindexing.TopicModelProvider;
import de.julielab.jcore.types.DocumentTopics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.DoubleArray;
import org.elasticsearch.common.util.set.Sets;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.*;

public class CrossVal {
    private static final Logger LOG = LogManager.getLogger();
    private final static int randomSeed = 1;


    public static void performCrossVal(String jsongsdocs, String annotatedGs) throws DataReadingException {
        PMClassifier classifier = new LuceneClassifier();
        int numFolds = 10;

        Map<String, Document> documents = DataReader.readDocuments(new File(jsongsdocs));
        LOG.info("Got {} documents for training", documents.size());
        //inferTopics(documents.values());
        InstancePreparator ip = InstancePreparator.getInstance();
        classifier.setInstancePreparator(ip);

        ip.trainTfIdf(documents.values());

        DataReader.addPMLabels(new File(annotatedGs), documents);
        if (LOG.isInfoEnabled()) {
            long pm = documents.values().stream().filter(d -> d.getPmLabel().equalsIgnoreCase("PM")).count();
            long notpm = documents.values().stream().filter(d -> d.getPmLabel().equalsIgnoreCase("Not PM")).count();
            LOG.info("Got {} PM and {} Not PM documents.", pm, notpm);
        }
        List<Document> pmList = documents.values().stream().filter(d -> !d.getPmLabel().equalsIgnoreCase("Not PM")).
                sorted(Comparator.comparing(Document::getId)).
                collect(toList());
        List<Document> notPmList = documents.values().stream().filter(d -> d.getPmLabel().equalsIgnoreCase("Not PM")).
                sorted(Comparator.comparing(Document::getId)).
                collect(toList());

        List<List<Document>> partitions = makeStratifiedPartitions(pmList, notPmList, numFolds);

        int corrAllover = 0;
        Set<Document> onceRight = new HashSet<>();
        for (int fold = 0; fold < numFolds; fold++) {
            int currentFold = fold;
            Map<String, Document> train = IntStream.range(0, numFolds).filter(i -> i != currentFold).mapToObj(partitions::get).flatMap(Collection::stream).collect(toMap(Document::getId, d -> d));
            Map<String, Document> test = partitions.get(fold).stream().collect(toMap(Document::getId, d -> d));

            //InstanceList ilist = ip.createClassificationInstances(train);
            LOG.info("Training on " + train.size() + " documents");
            classifier.train(train);

            LOG.info("Testing on " + test.size() + " documents");
            int corr = 0;
            for (Document document : test.values()) {
               // String predict = classifier.predictProbabiltyForPM(document) > .5 ? "PM" : "Not PM";
                String predict = classifier.predict(document);
                if (predict.equalsIgnoreCase(document.getPmLabel())) {
                    ++corr;
                    onceRight.add(document);
                }
            }
            LOG.info("Evaluation for fold " + fold + ":");
            LOG.info("Total: " + test.size());
            LOG.info("Correct: " + corr);
            LOG.info("That is " + (corr / (double) test.size()) * 100 + "%");


            corrAllover += corr;
        }

        LOG.info("Allover eval:");
        LOG.info("Total: " + documents.size());
        LOG.info("Correct: " + corrAllover);
        LOG.info("That is " + (corrAllover / (double) documents.size()) * 100 + "%");

        final HashSet<Document> alldocs = new HashSet<>(documents.values());
        final Set<Document> alwaysWrong = Sets.difference(alldocs, onceRight);
        //alwaysWrong.forEach(System.out::println);
    }

    private static void inferTopics(Collection<Document> values) {
        try {
            AnalysisEngine sentenceDetector = AnalysisEngineFactory.createEngine(
                    "de.julielab.jcore.ae.jsbd.desc.jcore-jsbd-ae-biomedical-english");
            AnalysisEngine tokenizer = AnalysisEngineFactory.createEngine(
                    "de.julielab.jcore.ae.jtbd.desc.jcore-jtbd-ae-biomedical-english");
            AnalysisEngine posTagger = AnalysisEngineFactory.createEngine(
                    "de.julielab.jcore.ae.opennlp.postag.desc.jcore-opennlp"
                            + "-postag-ae-biomedical-english");
            AnalysisEngine bioLemmatizer = AnalysisEngineFactory.createEngine(
                    "de.julielab.jcore.ae.biolemmatizer.desc.jcore-biolemmatizer-ae");
            AnalysisEngineDescription desc = AnalysisEngineFactory.createEngineDescription("de.julielab.jcore.ae.topicindexing.desc.jcore-topic-indexing-ae",
                    TopicIndexer.PARAM_TOPIC_MODEL_CONFIG, "uima/topicmodels/nt100-a1.0-b0.1-gs.xml",
                    TopicIndexer.PARAM_NUM_DISPLAYED_TOPIC_WORDS, 0,
                    TopicIndexer.PARAM_STORE_IN_MODEL_INDEX, false);
            ExternalResourceFactory.createDependencyAndBind(desc, TopicIndexer.RESOURCE_KEY_MODEL_FILE_NAME, TopicModelProvider.class, new File("uima/topicmodels/nt100-a1.0-b0.1-gs.mod.gz").toURI().toURL().toString());
            AnalysisEngine topicIndexer = AnalysisEngineFactory.createEngine(desc);
            JCas jCas = JCasFactory.createJCas("de.julielab.jcore.types.jcore-document-meta-pubmed-types",
                    "de.julielab.jcore.types.extensions.jcore-document-meta-extension-types",
                    "de.julielab.jcore.types.jcore-document-structure-pubmed-types",
                    "de.julielab.jcore.types.jcore-morpho-syntax-types");

            values.stream().forEach(d -> {
                jCas.setDocumentText(d.getTitle() + " " + d.getAbstractText());
                try {
                    sentenceDetector.process(jCas);
                    tokenizer.process(jCas);
                    posTagger.process(jCas);
                    bioLemmatizer.process(jCas);
                    topicIndexer.process(jCas);
                    DocumentTopics documentTopics = JCasUtil.selectSingle(jCas, DocumentTopics.class);
                    DoubleArray weights = documentTopics.getWeights();
                    double[] doubles = weights.toArray();
                    d.setTopicWeight(doubles);
                    jCas.reset();
                } catch (AnalysisEngineProcessException e) {
                    throw new RuntimeException(e);
                }


            });
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UIMAException e) {
            e.printStackTrace();
        }
    }

    private static List<List<Document>> makeStratifiedPartitions(List<Document> pmList, List<Document> notPmList, int numFolds) {
        List<List<Document>> ret = new ArrayList<>(numFolds);
        for (int i = 0; i < numFolds; i++)
            ret.add(new ArrayList<>());

        List<Document> pmListShuffled = new ArrayList<>(pmList);
        List<Document> notPmListShuffled = new ArrayList<>(notPmList);

        Collections.shuffle(pmListShuffled, new Random(randomSeed));
        Collections.shuffle(notPmList, new Random(randomSeed));

        for (int i = 0; i < Math.max(pmList.size(), notPmList.size()); i++) {
            int mod = i % numFolds;
            if (i < pmList.size())
                ret.get(mod).add(pmList.get(i));
            if (i < notPmList.size())
                ret.get(mod).add(notPmList.get(i));
        }
        return ret;
    }
}
