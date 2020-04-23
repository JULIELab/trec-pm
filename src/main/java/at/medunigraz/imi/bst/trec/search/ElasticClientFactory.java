package at.medunigraz.imi.bst.trec.search;

import at.medunigraz.imi.bst.config.TrecConfig;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ElasticClientFactory implements Closeable {

    private static RestHighLevelClient client = null;
    private static List<String> hostNames;

    public ElasticClientFactory() {
    }

    public static RestHighLevelClient getClient() {
        if (client == null) {
            open();
        }
        return client;

    }

    @SuppressWarnings("resource")
    private static void open() {
//        Settings settings = Settings.builder()
//                .put("cluster.name", TrecConfig.ELASTIC_CLUSTER).build();
        if (hostNames == null)
            hostNames = Arrays.stream(TrecConfig.ELASTIC_HOSTNAME.split(",")).map(String::trim).collect(Collectors.toList());
        HttpHost[] httpHosts = IntStream.range(0, hostNames.size()).mapToObj(i -> new HttpHost(hostNames.get(i), TrecConfig.ELASTIC_PORT, "http")).toArray(HttpHost[]::new);
        client = new RestHighLevelClient(RestClient.builder(httpHosts));
    }

    public void close() {
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        client = null;
    }

}
