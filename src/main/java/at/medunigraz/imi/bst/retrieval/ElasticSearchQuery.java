package at.medunigraz.imi.bst.retrieval;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.trec.model.Result;
import at.medunigraz.imi.bst.trec.search.ElasticSearch;
import de.julielab.ir.es.SimilarityParameters;
import de.julielab.ir.model.QueryDescription;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class ElasticSearchQuery<T extends QueryDescription> implements Query<T> {
    private static final Logger log = LoggerFactory.getLogger(ElasticSearchQuery.class);
    private String jsonQuery;

    private String index;
    private SimilarityParameters parameters;
    private String[] storedFields;
    private int size = TrecConfig.SIZE;
    // Used to restrict the result set based on a set of values of which the field
    // must include at least one. Required for LtR feature creation.
    private String filterField;
    private Collection<String> filterValues;
    // This suffix is appended to the index name given or retrieved from the query
    private String indexSuffix;

    public ElasticSearchQuery(int size, String index) {
        this.size = size;
        this.index = index;
    }

    public ElasticSearchQuery(String index) {
        this.index = index;
    }

    public void setStoredFields(String[] storedFields) {
        this.storedFields = storedFields;
    }

    /**
     * <p>Used for LtR. Causes the retrieval to restrict the result sets to documents that have at least one
     * of the given values appearing in the given field.</p>
     *
     * @param field  The field to filter on.
     * @param values The filter values.
     */
    public void setTermFilter(String field, Collection<String> values) {
        this.filterField = field;
        this.filterValues = values;
    }

    /**
     * <p>Clears the term filter set by {@link #setTermFilter(String, Collection)}.</p>
     */
    public void clearTermFilter() {
        this.filterField = null;
        this.filterValues = null;
    }

    @Override
    public List<Result> query(T topic) {
        String index = topic.getIndex() != null ? topic.getIndex() : this.index;
        if (indexSuffix != null && !indexSuffix.isBlank())
            index = index + indexSuffix;
        log.trace("Searching on index {} for query {}", index, topic);
        if (index == null)
            throw new IllegalStateException("No index was specified for this ElasticSearchQuery and the given topic does also not specify an index.");
        ElasticSearch es = new ElasticSearch(index, parameters);
        if (storedFields != null) {
            es.setStoredFields(storedFields);
        }
        if (filterField != null) {
            es.setFilterOnFieldValues(filterField, filterValues);
        }
        return es.query(new JSONObject(jsonQuery), size);
    }

    @Override
    public String getJSONQuery() {
        return jsonQuery;
    }

    @Override
    public void setJSONQuery(String jsonQuery) {
        this.jsonQuery = jsonQuery;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    public SimilarityParameters getParameters() {
        return parameters;
    }

    public void setSimilarityParameters(SimilarityParameters parameters) {
        this.parameters = parameters;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setIndexSuffix(String suffix) {
        indexSuffix = suffix;
    }


}
