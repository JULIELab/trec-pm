package at.medunigraz.imi.bst.trec.search;

import at.medunigraz.imi.bst.trec.model.Result;
import at.medunigraz.imi.bst.trec.utils.ConnectionUtils;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.junit.Assume;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertFalse;

public class ElasticSearchTest {

	public ElasticSearchTest() {
		// There must be an available server
		Assume.assumeTrue(ConnectionUtils.checkElasticOpenPort());
	}

	@Test
	public void testJsonQuery() throws Exception {
		ElasticSearch es = new ElasticSearch();

		File simple = new File(getClass().getResource("/templates/match-title-thyroid.json").getFile());
		String jsonQuery = FileUtils.readFileToString(simple, "UTF-8");

		List<Result> results = es.query(new JSONObject(jsonQuery));

		assertFalse(results.isEmpty());
	}

}
