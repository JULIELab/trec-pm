package at.medunigraz.imi.bst.retrieval;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.trec.evaluator.TrecWriter;
import at.medunigraz.imi.bst.trec.model.Result;
import at.medunigraz.imi.bst.trec.model.ResultList;
import at.medunigraz.imi.bst.trec.model.Task;
import de.julielab.ir.es.SimilarityParameters;
import de.julielab.ir.model.QueryDescription;

import java.io.File;
import java.util.*;
import java.util.function.Function;

public class Retrieval<T extends Retrieval, Q extends QueryDescription> {

    protected Query query;
    private Task task;
    private String resultsDir;
    private String experimentName;
    private int size = TrecConfig.SIZE;

    public static String getIndexName(Task task) {
        switch (task) {
            case CLINICAL_TRIALS:
                return TrecConfig.ELASTIC_CT_INDEX;
            case PUBMED:
                return TrecConfig.ELASTIC_BA_INDEX;
            default:
                return "";
        }
    }

    public T withExperimentName(String name) {
        this.experimentName = name;
        return (T) this;
    }

    public T withDecorator(Query decorator) {
        query = decorator;
        return (T) this;
    }

    public T withTemplate(File template) {
        query = new TemplateQueryDecorator(template, query);
        return (T) this;
    }

    public T withSubTemplate(File template) {
        query = new SubTemplateQueryDecorator(template, query);
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

    public T withTarget(Task task) {
        this.task = task;
        if (task != Task.PUBMED_ONLINE)
            query = new ElasticSearchQuery(size, getIndexName(task));
        else
            query = new PubMedOnlineQuery();
        return (T) this;
    }

    public T withSimilarityParameters(SimilarityParameters parameters) {
        if (query == null || !(query instanceof  ElasticSearchQuery))
            throw new IllegalStateException("Cannot set similarity parameters to the current query " + query + ". This call must immediately follow a call to withTarget(Task) where Task is CLINICAL_TRIALS or PUBMED.");
        ElasticSearchQuery q = (ElasticSearchQuery) query;
        q.setSimilarityParameters(parameters);
        return (T) this;
    }

    public String getResultsDir() {
        return resultsDir;
    }

    public T withResultsDir(String dir) {
        this.resultsDir = dir;
        return (T) this;
    }

    public T withSize(int size) {
        this.size = size;
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
            List<Result> results = query.query(topic);

            if (results.isEmpty())
                throw new IllegalStateException("RESULT EMPTY for " + getExperimentId());

            ResultList<Q> resultList = new ResultList<>(topic);
            resultList.addAll(results);
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
        return String.format("%s_%d_%s", getShortTaskName(), query.getName().replace(" ", "_"));
    }

    public String getShortTaskName() {
        switch (task) {
            case CLINICAL_TRIALS:
                return "ct";
            case PUBMED:
                return "pmid";
            default:
                return "";
        }
    }

    public String getExperimentName() {
        if (experimentName == null) {
            return "experiment";
        }

        return experimentName;
    }
}
