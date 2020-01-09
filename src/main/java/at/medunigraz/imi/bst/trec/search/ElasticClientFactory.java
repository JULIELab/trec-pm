package at.medunigraz.imi.bst.trec.search;

import at.medunigraz.imi.bst.config.TrecConfig;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.io.Closeable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ElasticClientFactory implements Closeable {

    private static Client client = null;
    private static List<String> hostNames;

    public ElasticClientFactory() {
    }

    public static Client getClient() {
        if (client == null) {
            open();
        }
        return client;
    }

    @SuppressWarnings("resource")
    private static void open() {
        Settings settings = Settings.builder()
                .put("cluster.name", TrecConfig.ELASTIC_CLUSTER).build();
        client = new PreBuiltTransportClient(settings);
        if (hostNames == null)
            hostNames = Arrays.stream(TrecConfig.ELASTIC_HOSTNAME.split(",")).map(String::trim).collect(Collectors.toList());
        for (String hostName : hostNames) {
            try {
                ((PreBuiltTransportClient) client).addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(hostName),
                        TrecConfig.ELASTIC_PORT));
            } catch (UnknownHostException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    public void close() {
        client.close();
        client = null;
    }

}
