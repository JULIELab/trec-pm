package de.julielab.ir;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.retrieval.StaticMapQueryDecorator;
import at.medunigraz.imi.bst.retrieval.TemplateQueryDecorator;
import at.medunigraz.imi.bst.trec.model.Topic;
import at.medunigraz.imi.bst.trec.query.DummyElasticSearchQuery;
import at.medunigraz.imi.bst.trec.search.ElasticClientFactory;
import de.julielab.ir.model.QueryDescription;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.admin.indices.close.CloseIndexRequest;
import org.elasticsearch.action.admin.indices.close.CloseIndexResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.admin.indices.open.OpenIndexRequest;
import org.elasticsearch.action.admin.indices.open.OpenIndexResponse;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequest;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.xcontent.XContentType;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ElasticSearchSetup {
    private static final Logger log = LogManager.getLogger();

    public static void main(String args[]) {
        putMapping();
    }

    public static void putMapping() {

        Map<String, String> properties = new HashMap<>();
        properties.put("k1", "1.2");
        properties.put("b", "0.75");
        properties.put("similarity", "my_bm25");
        final TemplateQueryDecorator<QueryDescription> decorator = new TemplateQueryDecorator<>(new File("es-mappings/cikm19-pubmed-bm25.json"), new StaticMapQueryDecorator(properties, new DummyElasticSearchQuery()));
        final Topic t = new Topic();
        decorator.query(t);
        final String jsonQuery = decorator.getJSONQuery();
        final JSONObject jsonObject = new JSONObject(jsonQuery);
        putMapping(jsonObject.getJSONObject("settings"), jsonObject.getJSONObject("mappings").getJSONObject("medline"), "medline", "bm25");

    }

    private static void putMapping(JSONObject settingsJson, JSONObject mappingJson, String esType, String similarity) {
        final Client client = ElasticClientFactory.getClient();
        final String indexName = TrecConfig.ELASTIC_BA_INDEX + "_" + similarity;
        boolean indexExisted = false;

        final IndicesExistsRequest indicesExistsRequest = Requests.indicesExistsRequest(indexName);
        final IndicesExistsResponse indicesExistsResponse = client.admin().indices().exists(indicesExistsRequest).actionGet();
        if (!indicesExistsResponse.isExists()) {
            log.info("Index {} does not exist and is created.", indexName);
            final CreateIndexRequest indexRequest = Requests.createIndexRequest(indexName);
            indexRequest.settings(settingsJson, XContentType.JSON);
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
        if (indexExisted == true) {
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
        log.info("Putting the mapping to index {}.", indexName);
        final PutMappingRequest putMappingRequest = Requests.putMappingRequest(indexName);
        putMappingRequest.source(mappingJson.toString(), XContentType.JSON);
        putMappingRequest.type(esType);
        if (putMappingRequest.validate() != null)
            throw putMappingRequest.validate();
        final PutMappingResponse putMappingResponse = client.admin().indices().putMapping(putMappingRequest).actionGet();
        if (!putMappingResponse.isAcknowledged())
            throw new IllegalArgumentException("Could not put mapping " + mappingJson + ", ES did not acknowledge.");
        if (indexExisted) {
            log.info("Reopening index {}.", indexName);
            final OpenIndexRequest openIndexRequest = Requests.openIndexRequest(indexName);
            final OpenIndexResponse openIndexResponse = client.admin().indices().open(openIndexRequest).actionGet();
            if (!openIndexResponse.isAcknowledged())
                throw new IllegalArgumentException("Could not reopen index " + indexName + ", ES did not acknowledge.");
        }
    }
}
