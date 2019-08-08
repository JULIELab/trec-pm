package at.medunigraz.imi.bst.trec.search;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.trec.utils.ConnectionUtils;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.junit.Assume;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ElasticClientFactoryTest {

	public ElasticClientFactoryTest() {
		// There must be an available server
		Assume.assumeTrue(ConnectionUtils.checkElasticOpenPort());
	}

	@Test
	public void testGetClient() throws IOException, ExecutionException, InterruptedException {
		Client client = ElasticClientFactory.getClient();

		ClusterHealthResponse health = client.admin().cluster().health(new ClusterHealthRequest()).get();
		String actual = health.getClusterName();
		assertEquals(TrecConfig.ELASTIC_CLUSTER, actual); // Any check
	}

	@Test
	public void testHealth() throws IOException, ExecutionException, InterruptedException {
		Client client = ElasticClientFactory.getClient();

		ClusterHealthResponse health = client.admin().cluster().health(new ClusterHealthRequest()).get();
		ClusterHealthStatus status = health.getStatus();
		int actual = status.compareTo(ClusterHealthStatus.RED);
		assertNotEquals(0, actual); // 0 = RED
	}
}
