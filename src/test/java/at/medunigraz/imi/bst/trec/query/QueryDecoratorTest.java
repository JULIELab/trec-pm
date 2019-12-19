package at.medunigraz.imi.bst.trec.query;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.retrieval.Query;
import at.medunigraz.imi.bst.trec.model.Result;
import at.medunigraz.imi.bst.trec.model.Topic;
import at.medunigraz.imi.bst.trec.utils.ConnectionUtils;
import de.julielab.ir.TrecCacheConfiguration;
import de.julielab.java.utilities.cache.CacheService;
import org.junit.Assume;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public abstract class QueryDecoratorTest {

	protected Query decoratedQuery;
	protected Topic topic;

	@Test
	public void testLiveQuery() {
		CacheService.initialize(new TrecCacheConfiguration());
		Assume.assumeTrue(ConnectionUtils.checkElasticOpenPort());
		TrecConfig.SUBTEMPLATES_FOLDER = "/test-subtemplates/";
		List<Result> resultList = decoratedQuery.query(topic);

		assertFalse(resultList.isEmpty());
		assertThat(resultList.size(), greaterThan(10));
	}

}
