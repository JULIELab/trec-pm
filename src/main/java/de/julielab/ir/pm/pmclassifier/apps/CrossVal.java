package de.julielab.ir.pm.pmclassifier.apps;

import de.julielab.ir.pm.pmclassifier.*;
import org.elasticsearch.common.util.set.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class CrossVal {
    private static final Logger LOG = LoggerFactory.getLogger(CrossVal.class);
    private final static int randomSeed = 1;


    public static void performCrossVal(PMClassifier classifier, String jsongsdocs, String annotatedGs) throws DataReadingException {
        int numFolds = 10;

        Map<String, Document> documents = DataReader.readDocuments(new File(jsongsdocs));
        LOG.info("Got {} documents for training", documents.size());
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
        List<Double> foldResults = new ArrayList<>();
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
            final double acc = (corr / (double) test.size()) * 100;
            LOG.info("That is " + acc + "%");
            foldResults.add(acc);


            corrAllover += corr;
        }

        LOG.info("All fold results: {}", foldResults);
        LOG.info("Allover eval:");
        LOG.info("Total: " + documents.size());
        LOG.info("Correct: " + corrAllover);
        LOG.info("That is " + (corrAllover / (double) documents.size()) * 100 + "%");

        final HashSet<Document> alldocs = new HashSet<>(documents.values());
        final Set<Document> alwaysWrong = Sets.difference(alldocs, onceRight);
        //alwaysWrong.forEach(System.out::println);
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
