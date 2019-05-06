package at.medunigraz.imi.bst.retrieval;

import at.medunigraz.imi.bst.trec.evaluator.TrecWriter;
import at.medunigraz.imi.bst.trec.model.*;
import de.julielab.ir.model.QueryDescription;

import java.io.File;
import java.util.*;

public class Retrieval<T extends Retrieval> {

    protected Query query;
    private Task task;
    private GoldStandard goldStandard;
    private int year;
    private String resultsDir;
    private String experimentName;


    public List<Result> retrieve(QueryDescription queryDescription) {
        return retrieve(Collections.singleton(queryDescription)).get(0).getResults();
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

    public T withGoldStandard(GoldStandard gold) {
        goldStandard = gold;
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
            query = new ElasticSearchQuery(goldStandard);
        else
            query = new PubMedOnlineQuery();
        return (T) this;
    }

    public T withYear(int year) {
        this.year = year;
        return (T) this;
    }


    public T withResultsDir(String dir) {
        resultsDir = dir;
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
     * @return The query results.
     */
    public <T extends QueryDescription> List<ResultList<T>> retrieve(Collection<T> queryDescriptions) {
        TrecWriter tw = null;
        if (resultsDir != null) {
            File resultsDir = new File(this.resultsDir);
            if (!resultsDir.exists())
                resultsDir.mkdir();
            File output = getOutput();
            final String runName = getExperimentName();  // TODO generate from experimentID, but respecting TREC syntax
            tw = new TrecWriter(output, runName);
        }
        List<ResultList<T>> resultListSet = new ArrayList<>();
        for (T topic : queryDescriptions) {
            List<Result> results = query.query(topic);


            if (results.isEmpty())
                throw new IllegalStateException("RESULT EMPTY for " + experimentName);

            ResultList<T> resultList = new ResultList<>(topic);
            resultList.addAll(results);
            resultListSet.add(resultList);
        }

        if (tw != null) {
            tw.write(resultListSet);
            tw.close();
        }
        return resultListSet;
    }

    public File getOutput() {
        return new File(this.resultsDir + getExperimentId() + ".trec_results");
    }

    public String getExperimentId() {
        if (experimentName != null) {
            return experimentName.replace(" ", "_");
        }
        return String.format("%s_%d_%s", getShortTaskName(), year, query.getName().replace(" ", "_"));
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
