package at.medunigraz.imi.bst.trec.search;

import at.medunigraz.imi.bst.config.TrecConfig;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.Closeable;
import java.io.IOException;
import java.util.stream.IntStream;

public class ElasticClientFactory implements Closeable {

	private static RestHighLevelClient client = null;

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
		String[] hosts = TrecConfig.ELASTIC_HOSTNAME;
		int[] ports = TrecConfig.ELASTIC_PORT;
		if (hosts.length != ports.length)
			throw new IllegalArgumentException("The number of ElasticSearch hosts and their ports must be equal in the configuration file.");
		HttpHost[] httpHosts = IntStream.range(0, hosts.length).mapToObj(i -> new HttpHost(hosts[i], ports[i], "http")).toArray(HttpHost[]::new);
		client = new RestHighLevelClient(RestClient.builder(httpHosts));
	}

	public void close() {
		try {
			client.close();
			client = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
