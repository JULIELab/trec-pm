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
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.admin.cluster.state.ClusterStateRequest;
import org.elasticsearch.action.admin.indices.close.CloseIndexRequest;
import org.elasticsearch.action.admin.indices.close.CloseIndexResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.admin.indices.open.OpenIndexRequest;
import org.elasticsearch.action.admin.indices.open.OpenIndexResponse;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsAction;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsRequestBuilder;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsResponse;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequest;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ElasticSearchSetup {
    private static final Logger log = LoggerFactory.getLogger(ElasticSearchSetup.class);

    private static Map<String, String> defaultProperties = new HashMap<>();
    private static String[] allSimilarities = new String[]{"bm25"
            //, "dfr", "dfi", "ib", "lmd", "lmjm"
    };
    /**
     * For multiple concurrent experiments on an index where each run may change the index settings, we need copies for each independent run. Otherwise one run will change the settings of the other run, invalidating the experimental results.
     */
    private static String[] independentCopies = new String[]{"copy0", "copy1", "copy2", "copy3", "copy4", "copy5", "copy6", "copy7", "copy8", "copy9"};

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

    ;

    public static void main(String args[]) {
      //  createPubmedIndices();
        createCtIndices();
//        deletePubmedIndices();
//        deleteCtIndices();
    }

    public static void deletePubmedIndices() {
        deleteIndices(TrecConfig.ELASTIC_BA_INDEX);
    }

    public static void deleteCtIndices() {
        deleteIndices(TrecConfig.ELASTIC_CT_INDEX);
    }

    public static void deleteIndices(String indexbaseName) {
        try {
            final boolean doDelete = CLIInteractionUtilities.readYesNoFromStdInWithMessage("WARNING: You are about to delete all " + indexbaseName + " indices. Are you sure?", false);
            if (doDelete) {
                final Client client = ElasticClientFactory.getClient();
                for (String similarity : allSimilarities) {
                    for (String copy : independentCopies) {
                        String indexName = indexbaseName + "_" + similarity + "_" + copy;
                        deleteIndex(client, indexName);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void deleteIndex(Client client, String indexName) {
        log.info("Deleting index {}.", indexName);
        final DeleteIndexRequest deleteIndexRequest = Requests.deleteIndexRequest(indexName);
        final DeleteIndexResponse deleteIndexResponse = client.admin().indices().delete(deleteIndexRequest).actionGet();
        if (!deleteIndexResponse.isAcknowledged())
            throw new IllegalArgumentException("Could not delete index " + indexName + ", ES did not acknowledge.");
    }

    public static void createPubmedIndices() {
        String esConfigTemplate = Path.of("es-mappings", "cikm19-pubmed-template.json").toFile().getAbsolutePath();
        createIndices(TrecConfig.ELASTIC_BA_INDEX, esConfigTemplate, defaultProperties, TrecConfig.ELASTIC_BA_MEDLINE_TYPE);
    }

    public static void createCtIndices() {
        String esConfigTemplate = Path.of("es-mappings", "cikm19-ct-template.json").toFile().getAbsolutePath();
        createIndices(TrecConfig.ELASTIC_CT_INDEX, esConfigTemplate, defaultProperties, TrecConfig.ELASTIC_CT_TYPE);
    }

    public static void createIndices(String indexBasename, String configurationTemplateFile, Map<String, String> properties, String esType) {
        Map<String, String> parameters = new HashMap<>(properties);
        for (String similarity : allSimilarities) {
            for (String copy : independentCopies) {
                parameters.put("similarity", "my_" + similarity);
                final TemplateQueryDecorator<QueryDescription> decorator = new TemplateQueryDecorator<>(configurationTemplateFile, new StaticMapQueryDecorator(parameters, new DummyElasticSearchQuery()));
                final Topic t = new Topic();
                decorator.query(t);
                final String indexSettingsAndMappings = decorator.getJSONQuery();
                JSONObject indexSettingsAndMappingsObject = new JSONObject(indexSettingsAndMappings);

                final JSONObject settings = indexSettingsAndMappingsObject.getJSONObject("settings");
                final JSONObject mappings = indexSettingsAndMappingsObject.getJSONObject("mappings").getJSONObject(esType);

                String indexName = indexBasename + "_" + similarity + "_" + copy;
                configureIndex(indexName, true, settings, mappings, esType, similarity);
            }
        }
    }

    public static void configureSimilarity(String indexBasename, boolean isExactIndexName, SimilarityParameters parameters, String esType) {
        log.trace("Got desired similarity parameters for index {}: {}", indexBasename, parameters);
        // Strategy: We "mis"use the TemplateQueryDecorator to inject the similarity setting parameters into the JSON template that otherwise adheres to the ElasticSearch required format
        // For this to work we must create a map with the template property keys and the desired values
        String esSettingsTemplate = Path.of("es-mappings", "cikm19-similarityonly-template.json").toFile().getAbsolutePath();
        // Read the SimilarityParameters into map so we can easily iterate over its fields (could also be done using reflection but this seemed simpler)
        final ObjectMapper om = new ObjectMapper();
        final MapType mapType = om.getTypeFactory().constructMapType(HashMap.class, String.class, String.class);
        // Create a parameterMap with all the default property settings. We will replace some values with the given SimilarityParameter values
        final Map<String, String> parameterMap = new HashMap<>(defaultProperties);
        Map<String, String> desiredSettingsMap = om.convertValue(parameters, mapType);
        for (String similarityProperty : desiredSettingsMap.keySet()) {
            // All similarities have the property "baseSimilarity" that just says what the underlying similarity is, BM25, DFI, ... this is not an actual value we are interested in
            if (similarityProperty.equals("baseSimilarity")) continue;
            // Create the template property keys and the desired values
            parameterMap.put(parameters.getBaseSimilarity() + "_" + similarityProperty, desiredSettingsMap.get(similarityProperty));
        }
        final TemplateQueryDecorator<QueryDescription> decorator = new TemplateQueryDecorator<>(esSettingsTemplate, new StaticMapQueryDecorator(parameterMap, new DummyElasticSearchQuery()));
        final Topic t = new Topic();
        decorator.query(t);
        final String settingsJson = decorator.getJSONQuery();
        final JSONObject settingsObject = new JSONObject(settingsJson);
        JSONObject map = new JSONObject();
        map.put("similarity", settingsObject);

        final Client client = ElasticClientFactory.getClient();
        final String indexName = isExactIndexName ? indexBasename : indexBasename + "_" + parameters.getBaseSimilarity();
        GetSettingsResponse getSettingsResponse = client.admin().indices().getSettings(new GetSettingsRequestBuilder(client, GetSettingsAction.INSTANCE, indexName).request()).actionGet();
        Settings s = getSettingsResponse.getIndexToSettings().get(indexName);
        boolean unequalSettingFound = false;
        log.trace("Checking if similarity settings for index {} must be adapted to desired settings {}", indexBasename, settingsObject);
        for (Object key : settingsObject.names()) {
            JSONObject concreteSimilaritySettings = settingsObject.getJSONObject((String) key);
            for (Object concreteSimilarityParam : concreteSimilaritySettings.names()) {
                String setting = "index.similarity." + key + "." + concreteSimilarityParam;
                String currentValue = s.get(setting);
                Object desiredValue = concreteSimilaritySettings.get((String) concreteSimilarityParam);
                log.trace("Got current setting {}: {} and desired value {}", setting, currentValue, desiredValue);
                try {
                    Double currentDouble = Double.valueOf(currentValue);
                    Double desiredDouble = Double.valueOf(desiredValue.toString());
                    if (currentDouble-desiredDouble > 0.00001)
                        unequalSettingFound = true;
                } catch (NumberFormatException e) {
                    if (!String.valueOf(desiredValue).equals(currentValue))
                        unequalSettingFound = true;
                }
                if (unequalSettingFound)
                    break;
            }
            if (unequalSettingFound)
                break;
        }
        if (unequalSettingFound) {
            log.debug("Found divergence in current and desired similarity settings, updating the index settings to {}.", map);
            configureIndex(indexBasename, isExactIndexName, map, null, esType, parameters.getBaseSimilarity());
        } else {
            log.trace("No index settings update required.");
        }
    }

    /**
     * Creates and/or configures an ElasticSearch index.
     *
     * @param indexBasename
     * @param isExactIndexName
     * @param settingsJson     The settings configuration.
     * @param mappingJson      The mapping configuration.
     * @param esType           The index type.
     * @param similarity       The base similarity used by the index. Is used as a index name suffix.
     */
    private static void configureIndex(String indexBasename, boolean isExactIndexName, JSONObject settingsJson, JSONObject mappingJson, String esType, String similarity) {
        final Client client = ElasticClientFactory.getClient();
        final String indexName = isExactIndexName ? indexBasename : indexBasename + "_" + similarity;
        boolean indexExisted = false;

        final IndicesExistsRequest indicesExistsRequest = Requests.indicesExistsRequest(indexName);
        final IndicesExistsResponse indicesExistsResponse = client.admin().indices().exists(indicesExistsRequest).actionGet();
        if (!indicesExistsResponse.isExists()) {
            log.info("Index {} does not exist and is created.", indexName);
            final CreateIndexRequest indexRequest = Requests.createIndexRequest(indexName);
            indexRequest.settings(settingsJson.toString(), XContentType.JSON);
            final CreateIndexResponse createIndexResponse = client.admin().indices().create(indexRequest).actionGet();
            if (!createIndexResponse.isAcknowledged())
                throw new IllegalArgumentException("Could not create index " + indexName);
        } else {
            indexExisted = true;
            log.info("Closing index {} for settings/mapping update.", indexName);
            final CloseIndexRequest closeIndexRequest = Requests.closeIndexRequest(indexName);
            final CloseIndexResponse closeIndexResponse = client.admin().indices().close(closeIndexRequest).actionGet();
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
            final UpdateSettingsResponse updateSettingsResponse = client.admin().indices().updateSettings(updateSettingsRequest).actionGet();
            if (!updateSettingsResponse.isAcknowledged())
                throw new IllegalStateException("Could not update index settings for index" + indexName + ", ES did not acknowledge.");
        }
        if (mappingJson != null) {
            log.info("Putting the mapping to index {}.", indexName);
            final PutMappingRequest putMappingRequest = Requests.putMappingRequest(indexName);
            putMappingRequest.source(mappingJson.toString(), XContentType.JSON);
            putMappingRequest.type(esType);
            final PutMappingResponse putMappingResponse = client.admin().indices().putMapping(putMappingRequest).actionGet();
            if (!putMappingResponse.isAcknowledged())
                throw new IllegalArgumentException("Could not put mapping " + mappingJson + ", ES did not acknowledge.");
        }
        if (indexExisted) {
            log.info("Reopening index {}.", indexName);
            final OpenIndexRequest openIndexRequest = Requests.openIndexRequest(indexName);
            ActionFuture<OpenIndexResponse> future = client.admin().indices().open(openIndexRequest);
            final OpenIndexResponse openIndexResponse = future.actionGet();
            // The sleep is necessary because there will be a connection error with the ES5.4 transport client
            // when we connect too quickly again to the index
            //curl http://localhost:9203/_cat/indices/trecpm19_ct_bm25_copy9\?h\=status
            IndexMetaData index = client.admin().cluster().state(new ClusterStateRequest().indices(indexName)).actionGet().getState().getMetaData().index(indexName);
            System.out.println(index.getState());
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            index = client.admin().cluster().state(new ClusterStateRequest().indices(indexName)).actionGet().getState().getMetaData().index(indexName);
            System.out.println(index.getState());
            if (!openIndexResponse.isAcknowledged())
                throw new IllegalArgumentException("Could not reopen index " + indexName + ", ES did not acknowledge.");
        }
    }
}
