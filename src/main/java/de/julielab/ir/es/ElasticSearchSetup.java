package de.julielab.ir.es;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.retrieval.StaticMapQueryDecorator;
import at.medunigraz.imi.bst.retrieval.TemplateQueryDecorator;
import at.medunigraz.imi.bst.trec.model.Topic;
import at.medunigraz.imi.bst.trec.query.DummyElasticSearchQuery;
import at.medunigraz.imi.bst.trec.search.ElasticClientFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import de.julielab.ir.model.QueryDescription;
import de.julielab.java.utilities.CLIInteractionUtilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.admin.indices.close.CloseIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.open.OpenIndexRequest;
import org.elasticsearch.action.admin.indices.open.OpenIndexResponse;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ElasticSearchSetup {
    private static final Logger log = LogManager.getLogger();

    private static Map<String, String> defaultProperties = new HashMap<>();
    private static String[] allSimilarities = new String[]{"tfidf", "bm25", "dfr", "dfi", "ib", "lmd", "lmjm"};

    static {
        defaultProperties.put("bm25_k1", "1.2");
        defaultProperties.put("bm25_b", "0.75");

        defaultProperties.put("dfr_basic_model", "be");
        defaultProperties.put("dfr_after_effect", "l");
        defaultProperties.put("dfr_normalization", "z");

        defaultProperties.put("dfi_independence_measure", "standardized");

        defaultProperties.put("ib_distribution", "ll");
        defaultProperties.put("ib_lambda", "df");
        defaultProperties.put("ib_normalization", "z");

        defaultProperties.put("lmd_mu", "2000");

        defaultProperties.put("lmjm_lambda", "0.1");


    }

    public static void main(String args[]) throws IOException {
//        deletePubmedIndices();
//        deleteCtIndices();
        createPubmedIndices();
        createCtIndices();
    }

    public static void deletePubmedIndices() {
        deleteIndices(TrecConfig.ELASTIC_BA_INDEX);
    }

    public static void deleteCtIndices() {
        deleteIndices(TrecConfig.ELASTIC_CT_INDEX);
    }

    private static void deleteIndices(String indexbaseName) {
        try {
            final boolean doDelete = CLIInteractionUtilities.readYesNoFromStdInWithMessage("WARNING: You are about to delete all " + indexbaseName + " indices. Are you sure?", false);
            if (doDelete) {
                final RestHighLevelClient client = ElasticClientFactory.getClient();
                for (String similarity : allSimilarities) {
                    String indexName = indexbaseName + "_" + similarity;
                    deleteIndex(client, indexName);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void deleteIndex(RestHighLevelClient client, String indexName) throws IOException {
        log.info("Deleting index {}.", indexName);
        final DeleteIndexRequest deleteIndexRequest = Requests.deleteIndexRequest(indexName);
        final AcknowledgedResponse response = client.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
        if (!response.isAcknowledged())
            throw new IllegalArgumentException("Could not delete index " + indexName + ", ES did not acknowledge.");
    }

    private static void createPubmedIndices() throws IOException {
        File esConfigTemplate = Path.of("es-mappings", "cikm19-pubmed-template.json").toFile();
        createIndices(TrecConfig.ELASTIC_BA_INDEX, esConfigTemplate, defaultProperties, TrecConfig.ELASTIC_BA_MEDLINE_TYPE);
    }

    private static void createCtIndices() throws IOException {
        File esConfigTemplate = Path.of("es-mappings", "cikm19-ct-template.json").toFile();
        createIndices(TrecConfig.ELASTIC_CT_INDEX, esConfigTemplate, defaultProperties, TrecConfig.ELASTIC_CT_TYPE);
    }

    private static void createIndices(String indexBasename, File configurationTemplateFile, Map<String, String> properties, String esType) throws IOException {
        Map<String, String> parameters = new HashMap<>(properties);
        for (String similarity : allSimilarities) {
            parameters.put("similarity", "my_" + similarity);
            final TemplateQueryDecorator<QueryDescription> decorator = new TemplateQueryDecorator<>(configurationTemplateFile, new StaticMapQueryDecorator(parameters, new DummyElasticSearchQuery()));
            final Topic t = new Topic();
            decorator.query(t);
            final String indexSettingsAndMappings = decorator.getJSONQuery();
            JSONObject indexSettingsAndMappingsObject = new JSONObject(indexSettingsAndMappings);

            final JSONObject settings = indexSettingsAndMappingsObject.getJSONObject("settings");
            final JSONObject mappings = indexSettingsAndMappingsObject.getJSONObject("mappings").getJSONObject(esType);

            configureIndex(indexBasename, settings, mappings, esType, similarity);
        }
    }

    public static void configureSimilarity(String indexBasename, SimilarityParameters parameters, String esType) throws IOException {
        File esSettingsTemplate = Path.of("es-mappings", "cikm19-settingsoly-template.json").toFile();
        final ObjectMapper om = new ObjectMapper();
        final MapType mapType = om.getTypeFactory().constructMapType(HashMap.class, String.class, String.class);
        final Map<String, String> parameterMap = new HashMap<>(defaultProperties);
        parameterMap.putAll(om.convertValue(parameters, mapType));
        final TemplateQueryDecorator<QueryDescription> decorator = new TemplateQueryDecorator<>(esSettingsTemplate, new StaticMapQueryDecorator(parameterMap, new DummyElasticSearchQuery()));
        final Topic t = new Topic();
        decorator.query(t);
        final String settingsJson = decorator.getJSONQuery();
        final JSONObject settingsObject = new JSONObject(settingsJson);
        configureIndex(indexBasename, settingsObject, null, esType, parameters.getBaseSimilarity());
    }

    /**
     * Creates and/or configures an ElasticSearch index.
     *
     * @param indexBasename The basic index name. The similarity will be added as a suffix.
     * @param settingsJson  The settings configuration.
     * @param mappingJson   The mapping configuration.
     * @param esType        The index type.
     * @param similarity    The base similarity used by the index. Is used as a index name suffix.
     */
    private static void configureIndex(String indexBasename, JSONObject settingsJson, JSONObject mappingJson, String esType, String similarity) throws IOException {
        final RestHighLevelClient client = ElasticClientFactory.getClient();
        final String indexName = indexBasename + "_" + similarity;
        boolean indexExisted = false;

        final boolean indexExists = client.indices().exists(new GetIndexRequest(indexName), RequestOptions.DEFAULT);
        if (!indexExists) {
            log.info("Index {} does not exist and is created.", indexName);
            final CreateIndexRequest indexRequest = new CreateIndexRequest(indexName);
            indexRequest.settings(settingsJson.toString(), XContentType.JSON);
            final org.elasticsearch.client.indices.CreateIndexResponse createIndexResponse = client.indices().create(indexRequest, RequestOptions.DEFAULT);
            if (!createIndexResponse.isAcknowledged())
                throw new IllegalArgumentException("Could not create index " + indexName);
        } else {
            indexExisted = true;
            log.info("Closing index {} for settings/mapping update.", indexName);
            final CloseIndexRequest closeIndexRequest = Requests.closeIndexRequest(indexName);
            final AcknowledgedResponse closeIndexResponse = client.indices().close(closeIndexRequest, RequestOptions.DEFAULT);
            if (!closeIndexResponse.isAcknowledged())
                throw new IllegalStateException("Could not close index " + indexName + ", ES did not acknowledge.");
        }
        if (indexExisted) {
            log.info("Sending update settings request to {}.", indexName);
            final UpdateSettingsRequest updateSettingsRequest = Requests.updateSettingsRequest(indexName);
            final JSONObject similarityJson = settingsJson.getJSONObject("similarity");
            final JSONObject similaritySettings = new JSONObject();
            similaritySettings.put("similarity", similarityJson);
            updateSettingsRequest.settings(similaritySettings.toString(), XContentType.JSON);
            final AcknowledgedResponse updateSettingsResponse = client.indices().putSettings(updateSettingsRequest, RequestOptions.DEFAULT);
            if (!updateSettingsResponse.isAcknowledged())
                throw new IllegalStateException("Could not update index settings for index" + indexName + ", ES did not acknowledge.");
        }
        if (mappingJson != null) {
            log.info("Putting the mapping to index {}.", indexName);
            final PutMappingRequest putMappingRequest = new PutMappingRequest(indexName);
            putMappingRequest.source(mappingJson.toString(), XContentType.JSON);
            final AcknowledgedResponse putMappingResponse = client.indices().putMapping(putMappingRequest, RequestOptions.DEFAULT);
            if (!putMappingResponse.isAcknowledged())
                throw new IllegalArgumentException("Could not put mapping " + mappingJson + ", ES did not acknowledge.");
        }
        if (indexExisted) {
            log.info("Reopening index {}.", indexName);
            final OpenIndexRequest openIndexRequest = Requests.openIndexRequest(indexName);
            final OpenIndexResponse openIndexResponse = client.indices().open(openIndexRequest, RequestOptions.DEFAULT);
            if (!openIndexResponse.isAcknowledged())
                throw new IllegalArgumentException("Could not reopen index " + indexName + ", ES did not acknowledge.");
        }
    }
}
