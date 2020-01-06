package at.medunigraz.imi.bst.retrieval;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.trec.evaluator.TrecWriter;
import at.medunigraz.imi.bst.trec.model.Challenge;
import at.medunigraz.imi.bst.trec.model.Result;
import at.medunigraz.imi.bst.trec.model.ResultList;
import at.medunigraz.imi.bst.trec.model.Task;
import de.julielab.ir.es.SimilarityParameters;
import de.julielab.ir.ltr.Document;
import de.julielab.ir.ltr.DocumentList;
import de.julielab.ir.ltr.features.IRScore;
import de.julielab.ir.ltr.features.IRScoreFeatureKey;
import de.julielab.ir.ltr.features.TrecPmQueryPart;
import de.julielab.ir.model.QueryDescription;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Retrieval<T extends Retrieval, Q extends QueryDescription> implements Serializable {
    protected Query<Q> query;
    private Logger log = LogManager.getLogger();
    private ElasticSearchQuery<Q> esQuery;
    private String resultsDir;
    private String experimentName;
    private int size = TrecConfig.SIZE;
    private String indexName;
    private List<Retrieval<T, Q>> negativeBoosts;
    private IRScoreFeatureKey scoreKey;
    private String indexSuffix;

    public Retrieval(String indexName) {
        this(indexName, new IRScoreFeatureKey(IRScore.BM25, TrecPmQueryPart.FULL));
    }

    public Retrieval(String indexName, IRScoreFeatureKey featureKey) {
        this.indexName = indexName;
        this.esQuery = new ElasticSearchQuery(size, indexName);
        this.query = esQuery;
        this.negativeBoosts = new ArrayList<>();
    }

    /**
     * @param indexName
     * @param resultSize
     * @deprecated Use {@link #withSize(int)} instead.
     */
    public Retrieval(String indexName, int resultSize) {
        this(indexName);
        esQuery.setSize(resultSize);
    }

    public void addNegativeBoost(Retrieval<T, Q> negativeBoostRetrieval) {
        negativeBoosts.add(negativeBoostRetrieval);
    }

    public T withExperimentName(String name) {
        this.experimentName = name;
        return (T) this;
    }

    public T withIndexSuffix(String suffix) {
        indexSuffix = suffix;
        esQuery.setIndexSuffix(suffix);
        return (T) this;
    }

    public T withStoredFields(String... fields) {
        esQuery.setStoredFields(fields);
        return (T) this;
    }

    public T withStoredFields(Challenge challenge, Task task, int year) {
        esQuery.setStoredFields(StoredFieldsRegistry.getStoredFields(challenge, task, year));
        return (T) this;
    }

    public T withDecorator(Query decorator) {
        query = decorator;
        return (T) this;
    }

    public T withTemplate(String template) {
        query = new TemplateQueryDecorator(template, query);
        return (T) this;
    }

    public T withSubTemplate(String template) {
        query = new SubTemplateQueryDecorator(template, query);
        return (T) this;
    }

    public T withSize(int size) {
        esQuery.setSize(size);
        return (T) this;
    }

    /**
     * <p>
     * The given map will be used to fill variables/properties in the used template by the mapped values.
     * </p>
     *
     * @param templateProperties A map holding the poperties from the template and the values they should be replaced with.
     * @return This T.
     */
    public T withProperties(Map<String, String> templateProperties) {
        query = new StaticMapQueryDecorator(templateProperties, query);

        return (T) this;
    }

    /**
     * <p>
     * The given map will be used to fill variables/properties in the used template by the mapped values.
     * </p>
     * <p>The parameters of the method are property-value pairs were the property always comes first and then the associated value.</p>
     *
     * @param templatePropertiesAndValues An array holding the poperties from the template and the values they should be replaced with.
     * @return This T.
     */
    public T withProperties(String... templatePropertiesAndValues) {
        query = new StaticMapQueryDecorator(array2Map(templatePropertiesAndValues), query);

        return (T) this;
    }

    public Query getQuery() {
        return query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public T withSimilarityParameters(SimilarityParameters parameters) {
        esQuery.setSimilarityParameters(parameters);
        return (T) this;
    }

    public void setIrScoresToDocuments(DocumentList<Q> documents, String docIdField, IRScoreFeatureKey scoreType) {
        final Map<Q, List<Document<Q>>> documentsByQuery = documents.stream().collect(Collectors.groupingBy(Document::getQueryDescription));
        for (Q query : documentsByQuery.keySet()) {
            Map<String, Document<Q>> documentsById = documentsByQuery.get(query).stream().collect(Collectors.toMap(Document::getId, Function.identity()));
            esQuery.setTermFilter(docIdField, documentsById.keySet());
            esQuery.setSize(documentsById.size());
            final ResultList<Q> resultList = retrieve(Collections.singleton(query)).get(0);
            if (resultList.getResults().size() != documentsById.size())
                log.debug("{} documents were requested, {} were returned.", documentsById.size(), resultList.getResults().size());

            resultList.getResults().forEach(r -> {
                final Document<Q> document = documentsById.get(r.getId());
                document.setScore(scoreType, r.getScore());
                if (r.getSourceFields() != null)
                    document.setSourceFields(r.getSourceFields());
            });
        }
        esQuery.clearTermFilter();
    }

    public String getResultsDir() {
        return resultsDir;
    }

    public T withResultsDir(String dir) {
        this.resultsDir = dir;
        return (T) this;
    }

    private Map<String, String> array2Map(String[] mapItems) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < mapItems.length; i++) {
            if (i % 2 == 1) {
                String key = mapItems[i - 1];
                String value = mapItems[i];
                map.put(key, value);
            }
        }
        return map;
    }

    /**
     * <p>Issues to the given queries for retrieval and returns the retrieved results. Writes the results to file
     * if {@link #withResultsDir(String)} was called for this retrieval.</p>
     *
     * @param queryDescriptions The queries to issue.
     * @param queryIdFunction   The function that generates the query IDs in the result file.
     * @return The query results.
     */
    public List<ResultList<Q>> retrieve(Collection<Q> queryDescriptions, Function<QueryDescription, String> queryIdFunction) {
        return retrieve(queryDescriptions, resultsDir, queryIdFunction);
    }

    /**
     * <p>Issues the given queries without writing the results to file.</p>
     *
     * @param queryDescriptions The queries to run.
     * @return The result lists for the queries.
     */
    public List<ResultList<Q>> retrieve(Collection<Q> queryDescriptions) {
        return retrieve(queryDescriptions, null, null);
    }

    /**
     * <p>Issues to the given queries for retrieval and returns the retrieved results. Writes the results to file
     * if <tt>resultsDirPath</tt> is not null.</p>
     *
     * @param queryDescriptions The queries to issue.
     * @param resultsDirPath    The path to the directory where results should be written to.
     * @param queryIdFunction   The function that generates the query IDs in the result file.
     * @return The query results.
     */
    public List<ResultList<Q>> retrieve(Collection<Q> queryDescriptions, String resultsDirPath, Function<QueryDescription, String> queryIdFunction) {
        TrecWriter tw = null;
        if (resultsDirPath != null) {
            File resultsDir = new File(resultsDirPath);
            if (!resultsDir.exists())
                resultsDir.mkdir();
            File output = getOutput(resultsDirPath);
            final String runName = getExperimentName();  // TODO generate from experimentID, but respecting TREC syntax
            tw = new TrecWriter(output, runName);
        }
        List<ResultList<Q>> resultListSet = new ArrayList<>();
        for (Q topic : queryDescriptions) {
            Q cleanCopy = topic.getCleanCopy();
            List<Result> results = query.query(cleanCopy);
            if (results.isEmpty()) {
                String index = topic.getIndex() != null ? topic.getIndex() : indexName;
                if (indexSuffix != null && !indexSuffix.isBlank())
                    index = index + indexSuffix;
                log.error("RESULT EMPTY for run {} on index {} by thread {}; query was: {}", getExperimentId(), index,Thread.currentThread(), new JSONObject(query.getJSONQuery()));
            }


            ResultList<Q> resultList = new ResultList<>(cleanCopy);
            resultList.addAll(results);
            if (negativeBoosts != null) {
                final Map<String, Result> resultsById = results.stream().collect(Collectors.toMap(Result::getId, Function.identity()));
                for (Retrieval<T, Q> negativeBoost : negativeBoosts) {
                    final DocumentList<Q> documents = DocumentList.fromRetrievalResultList(resultList);
                    negativeBoost.setIrScoresToDocuments(documents, "pubmedId", scoreKey);
                    documents.stream().filter(d -> resultsById.containsKey(d.getId())).forEach(d -> {
                                final Result r = resultsById.get(d.getId());
                                r.setScore(r.getScore() - .00001 * d.getIrScore(scoreKey));
                            }
                    );
                }
            }
            resultListSet.add(resultList);
        }

        if (tw != null) {
            tw.write(resultListSet, queryIdFunction);
            tw.close();
        }
        return resultListSet;
    }

    public File getOutput() {
        return getOutput(this.resultsDir);
    }

    public File getOutput(String resultsDir) {
        return new File(resultsDir + getExperimentId() + ".trec_results");
    }

    public String getExperimentId() {
        if (experimentName != null) {
            return experimentName.replace(" ", "_");
        }
        return String.format("%s_%s", indexName, query.getName().replace(" ", "_"));
    }

    public String getExperimentName() {
        if (experimentName == null) {
            return "experiment";
        }

        return experimentName;
    }
}
