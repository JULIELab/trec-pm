package at.medunigraz.imi.bst.trec.experiment;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.retrieval.Query;
import at.medunigraz.imi.bst.retrieval.ResultListFusion;
import at.medunigraz.imi.bst.retrieval.Retrieval;
import at.medunigraz.imi.bst.trec.evaluator.TrecWriter;
import at.medunigraz.imi.bst.trec.model.*;
import de.julielab.ir.goldstandards.GoldStandard;
import de.julielab.ir.ltr.Document;
import de.julielab.ir.ltr.DocumentList;
import de.julielab.ir.ltr.Ranker;
import de.julielab.ir.model.QueryDescription;
import de.julielab.java.utilities.FileUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Experiment<Q extends QueryDescription> {

    private static final Logger LOG = LoggerFactory.getLogger(Experiment.class);
    private ResultListFusion resultFusion;
    private List<Retrieval<?, Q>> retrievals;
    private GoldStandard goldStandard;
    private String statsDir = "stats/";
    private String resultsDir = "results/";
    private QueryDescriptionSet<Q> topicSet;
    private int k = TrecConfig.SIZE;
    private List<ResultList<Q>> lastResultListSet;
    // This ranker will be applied to retrieved results, if it is present.
    private Ranker<Q> reRanker;
    private Map<String, Metrics> metricsByTopic;
    private String[] requestedMetrics;
    private boolean writeInspectionFile;
    private int inspectionOutputPerTopic = 10;

    public void setInspectionOutputPerTopic(int inspectionOutputPerTopic) {
        this.inspectionOutputPerTopic = inspectionOutputPerTopic;
    }

    private Function<Result, String> inspectionResultColumnGenerator;

    /**
     * Build an Experiment using the topics provided by the gold standard.
     *
     * @param goldStandard
     * @param retrieval
     */
    public Experiment(GoldStandard<Q> goldStandard, Retrieval retrieval) {
        this(goldStandard, null, goldStandard.getQueriesAsList(), retrieval);
    }

    /**
     * Build an Experiment using the topics provided by the gold standard.
     *
     * @param goldStandard
     * @param retrievals
     */
    public Experiment(GoldStandard<Q> goldStandard, ResultListFusion resultFusion, QueryDescriptionSet<Q> topics, Retrieval... retrievals) {
        if (retrievals.length > 1 && resultFusion == null)
            throw new IllegalArgumentException("Multiple retrievals were passed but no result fusion.");
        this.goldStandard = goldStandard;
        this.resultFusion = resultFusion;
        this.retrievals = Arrays.asList(retrievals);
        this.topicSet = topics;
    }

    /**
     * Build an Experiment using the topics provided.
     *
     * @param goldStandard
     * @param retrieval
     * @param topics
     */
    public Experiment(GoldStandard<Q> goldStandard, Retrieval retrieval, QueryDescriptionSet<Q> topics) {
        this(goldStandard, null, topics, retrieval);
    }

    public void setWriteInspectionFile(boolean writeInspectionFile) {
        this.writeInspectionFile = writeInspectionFile;
    }

    /**
     * If an inspection file is written (see {@link #setWriteInspectionFile(boolean)}), each {@link Result} instance
     * results to one row. This function determines the information to be extracted from each <tt>Result</tt>. The return
     * value should be a String of tab-separated column values.
     * @param inspectionResultColumnGenerator
     */
    public void setInspectionResultColumnGenerator(Function<Result, String> inspectionResultColumnGenerator) {
        this.inspectionResultColumnGenerator = inspectionResultColumnGenerator;
    }

    /**
     * <p>Sets a ranker to be applied to the retrieved documents.</p>
     *
     * @param reRanker The ranker
     */
    public void setReRanker(Ranker<Q> reRanker) {
        this.reRanker = reRanker;
    }

    public List<Retrieval<?, Q>> getRetrievals() {
        return retrievals;
    }

    public void setRetrievals(List<Retrieval<?, Q>> retrievals) {
        this.retrievals = retrievals;
    }

    public String getStatsDir() {
        return statsDir;
    }

    public void setStatsDir(String statsDir) {
        this.statsDir = statsDir.endsWith(File.separator) ? statsDir : statsDir + File.separator;
    }

    public void setResultsDir(String resultsDir) {
        this.resultsDir = resultsDir.endsWith(File.separator) ? resultsDir : resultsDir + File.separator;
    }

    public String getExperimentId() {
        return retrievals.stream().map(Retrieval::getExperimentId).collect(Collectors.joining("-"));
    }

    public QueryDescriptionSet<Q> getTopicSet() {
        return topicSet;
    }

    public void setTopicSet(QueryDescriptionSet<Q> topicSet) {
        this.topicSet = topicSet;
    }

    /**
     * <p>For the trec_eval script, specify if non-existing result entries should count as 0 in the 'all' performance values.</p>
     * <p>The sample_eval.pl script does not allow a setting here and always works as if this setting would be set to <tt>false</tt>.</p>
     *
     * @return Whether or not to calculate the evaluation scores including or excluding missing result documents.
     */
    public boolean isCalculateTrecEvalWithMissingResults() {
        // If we are querying just a subset of the GS, we won't get metrics for all topics and thus need to set -c to false.
        if (goldStandard == null || topicSet.size() < goldStandard.getQueriesAsList().size()) {
            return false;
        }
        return true;
    }

    public int getK() {
        return k;
    }

    /**
     * <p>The number of top documents to calculate scores for with trec_eval. Defaults to 1000.</p>
     *
     * @param k The number of the top documents.
     */
    public void setK(int k) {
        this.k = k;
    }


    public List<ResultList<Q>> getLastResultListSet() {
        return lastResultListSet;
    }

    public List<DocumentList<Q>> getLastResultAsDocumentLists() {
        List<DocumentList<Q>> lastDocumentLists = new ArrayList<>();
        for (ResultList<Q> list : lastResultListSet) {
            final DocumentList<Q> documents = DocumentList.fromRetrievalResultList(list);
            lastDocumentLists.add(documents);
        }
        return lastDocumentLists;
    }

    public DocumentList<Q> getLastResultAsSingleDocumentList() {
        return lastResultListSet.stream().map(DocumentList::fromRetrievalResultList).flatMap(Collection::stream).collect(Collectors.toCollection(DocumentList::new));
    }

    public Map<String, Metrics> getMetricsByTopic() {
        return metricsByTopic;
    }

    public Metrics run() {
        final String experimentId = getExperimentId();
        final String longExperimentId = retrievals.stream().map(retrieval -> experimentId + " with decorators " + retrieval.getQuery().getName()).collect(Collectors.joining("-"));

        LOG.info("Running collection " + longExperimentId + "...");

        List<List<ResultList<Q>>> allResultLists = new ArrayList<>(retrievals.size());
        for (Retrieval<?, Q> retrieval : retrievals) {
            List<ResultList<Q>> resultListSet = retrieval.retrieve(topicSet);
            allResultLists.add(resultListSet);
        }
        List<ResultList<Q>> finalResultList = new ArrayList<>();
        if (resultFusion != null)
            finalResultList = resultFusion.fuseMultipleTopics(allResultLists);
        else
            finalResultList = allResultLists.get(0);

        lastResultListSet = finalResultList;
        if (reRanker != null)
            lastResultListSet = rerank(lastResultListSet);
        if (writeInspectionFile)
            writeInspectionFile(lastResultListSet, experimentId);

        File output = writeResults(lastResultListSet, experimentId);
        int k = this.k;
        boolean calculateTrecEvalWithMissingResults = isCalculateTrecEvalWithMissingResults();
        String statsDir = this.statsDir;

        TrecMetricsCreator trecMetricsCreator = new TrecMetricsCreator(experimentId, longExperimentId, output, getQrelFile(), k, calculateTrecEvalWithMissingResults, statsDir, goldStandard != null ? goldStandard.getType() : GoldStandardType.UNKNOWN, getSampleQrelFile());
        trecMetricsCreator.setRequestedMetrics(requestedMetrics);
        Metrics allMetrics = trecMetricsCreator.computeMetrics();
        metricsByTopic = trecMetricsCreator.getMetricsPerTopic();

        return allMetrics;

        // TODO Experiment API #53
//        System.out.println(allMetrics.getInfNDCG() + ";" + longExperimentId);
    }

    private void writeInspectionFile(List<ResultList<Q>> lastResultListSet, String experimentId) {
        if (inspectionResultColumnGenerator == null)
            throw new IllegalStateException("The inspection file row generator is not set, cannot create the inspection file.");
//        if (goldStandard == null) {
//            return;
//        }
        File inspectionFile = new File("inspections", experimentId + ".txt");
        if (!inspectionFile.getParentFile().exists())
            inspectionFile.getParentFile().mkdirs();
        try (BufferedWriter bw = FileUtilities.getWriterToFile(inspectionFile)) {
            Function<QueryDescription, String> queryIdFunction = goldStandard != null ? goldStandard.getQueryIdFunction() : d -> String.valueOf(d.getNumber());
            for (ResultList<Q> resultList : lastResultListSet) {
                Q topic = resultList.getTopic();
                Map<String, Document> id2doc = Collections.emptyMap();
                if (goldStandard != null) {
                    Stream<Document> stream = goldStandard.getQrelDocumentsForQuery(topic).stream();
                    id2doc = stream.collect(Collectors.toMap(d -> d.getId(), Function.identity()));
                } else {
                    id2doc = DocumentList.fromRetrievalResultList(resultList).stream().collect(Collectors.toMap(d -> d.getId(), Function.identity()));
                }
                int numWrittenForTopic = 0;
                for (Result r : resultList.getResults()) {
                    bw.write(queryIdFunction.apply(topic));
                    bw.write("\t");
                    Document document = id2doc.get(retrievals.get(0).getDocIdFunction().apply(r));
                    if (document != null)
                        bw.write(String.valueOf(document.getRelevance()));
                    else
                        bw.write("-1");
                    bw.write("\t");
                    String row = inspectionResultColumnGenerator.apply(r);
                    bw.write(row);
                    bw.newLine();
                    if (numWrittenForTopic >= inspectionOutputPerTopic && inspectionOutputPerTopic > 0)
                        break;
                    ++numWrittenForTopic;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File writeResults(List<ResultList<Q>> resultLists, String experimentId) {
        File resultsDir = new File(this.resultsDir);
        if (!resultsDir.exists())
            resultsDir.mkdir();
        File output = new File(resultsDir.getAbsolutePath(), experimentId + ".trec_results");
        final String runName = experimentId;  // TODO generate from experimentID, but respecting TREC syntax
        TrecWriter tw = new TrecWriter(output, runName);

        Function<QueryDescription, String> queryIdFunction = goldStandard != null ? goldStandard.getQueryIdFunction() : qd -> String.valueOf(qd.getNumber());
        tw.write(resultLists, retrievals.get(0).getDocIdFunction(), queryIdFunction);
        tw.close();
        return output;
    }

    private List<ResultList<Q>> rerank(List<ResultList<Q>> resultLists) {
        if (reRanker == null)
            return resultLists;
        List<ResultList<Q>> ret = new ArrayList<>();
        for (ResultList<Q> resultList : resultLists) {
            final Map<String, Result> resultsById = resultList.getResults().stream().collect(Collectors.toMap(Result::getId, Function.identity()));
            final DocumentList<Q> documents = DocumentList.fromRetrievalResultList(resultList);
            final DocumentList<Q> reRankedDocuments = reRanker.rank(documents);
            ResultList<Q> rl = new ResultList<>(resultList.getTopic());
            reRankedDocuments.stream().map(d -> {
                Result newRes = new Result(d.getId(), d.getIrScore(reRanker.getOutputScoreType()));

                // Preserve the original sourceFields, but set eventual treatments optimized by the ranker.
                // TODO Unify `ResultList` and `DocumentList` (#31)
                newRes.setSourceFields(resultsById.get(d.getId()).getSourceFields());
                newRes.setTreatments(d.getTreatments());

                return newRes;
            }).forEach(rl::add);
            ret.add(rl);
        }
        return ret;
    }

    private File getQrelFile() {
        File qrelFile = new File("qrels", String.format("%s.qrels", getExperimentId()));
        try {
            if (goldStandard != null)
                goldStandard.writeQrelFile(qrelFile);
            else {
                if (!qrelFile.getParentFile().exists())
                    qrelFile.getParentFile().mkdirs();
                qrelFile.createNewFile();
            }
        } catch (IOException e) {
            LOG.error("Could not create Qrel file at {}", qrelFile, e);
            throw new IllegalStateException(e);
        }
        return qrelFile;
    }

    private File getSampleQrelFile() {
        if (goldStandard != null && goldStandard.isSampleGoldStandard()) {
            final File sampleQrelFile = new File("qrels", String.format("sample-%s.qrels", getExperimentId()));
            goldStandard.writeSampleQrelFile(sampleQrelFile);
            return sampleQrelFile;
        }
        return null;
    }

    public List<Query> getDecorators() {
        return retrievals.stream().map(Retrieval::getQuery).collect(Collectors.toList());
    }

    public void setGoldStandard(GoldStandard goldStandard) {
        this.goldStandard = goldStandard;
    }

    public void setRequestedMetrics(String[] requestedMetrics) {
        this.requestedMetrics = requestedMetrics;
    }
}
