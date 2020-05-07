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

public class ElasticSearchQuery<T extends QueryDescription> implements Query<T> {
    private static final Logger log = LoggerFactory.getLogger(ElasticSearchQuery.class);
    private String jsonQuery;

    private String[] indices;
    private SimilarityParameters parameters;
    private String[] storedFields;
    private int size = TrecConfig.SIZE;
    // Used to restrict the result set based on a set of values of which the field
    // must include at least one. Required for LtR feature creation.
    private String filterField;
    private Collection<String> filterValues;
    // This suffix is appended to the index name given or retrieved from the query
    private String indexSuffix;
    /**
     * When not null, for each value in the given field only the first document in a result list will be returned.
     */
    private String unifyingField;
    private int resultListeSizeCutoff;
    private SearchHitReranker reranker;

    public void setReranker(SearchHitReranker reranker) {
        this.reranker = reranker;
    }

    public ElasticSearchQuery(int size, String index) {
        this(size, new String[]{index});
    }

    public ElasticSearchQuery(String index) {
        this.indices = new String[]{index};
    }

    public ElasticSearchQuery(int size, String[] indices) {
        this.size = size;
        this.indices = indices;
    }

    public ElasticSearchQuery(String[] indices) {
        this.indices = indices;
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

    public void setUnifyingField(String unifyingField) {
        this.unifyingField = unifyingField;
    }

    @Override
    public List<Result> query(T topic) {
        String[] indices = topic.getIndex() != null ? new String[]{topic.getIndex()} : this.indices;
        if (indexSuffix != null && !indexSuffix.isBlank())
            for (int i = 0; i < indices.length; i++) {
                indices[i] = indices[i] + indexSuffix;
            }
        log.trace("Searching on indices {} for query {}", indices, topic);
        if (indices == null || indices.length == 0)
            throw new IllegalStateException("No index was specified for this ElasticSearchQuery and the given topic does also not specify an index.");
        ElasticSearch es = new ElasticSearch(indices, parameters);
        if (storedFields != null) {
            es.setStoredFields(storedFields);
        }
        if (filterField != null) {
            es.setFilterOnFieldValues(filterField, filterValues);
        }
        if (unifyingField != null) {
            es.setUnifyingField(unifyingField);
        }
        if (resultListeSizeCutoff > 0) {
            es.setResultListeSizeCutoff(resultListeSizeCutoff);
        }
        if (reranker != null)
            es.setReranker(reranker);
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


    public void setResultListeSizeCutoff(int resultListeSizeCutoff) {
        this.resultListeSizeCutoff = resultListeSizeCutoff;
    }

}
