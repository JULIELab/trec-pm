package at.medunigraz.imi.bst.trec.evaluator;

import at.medunigraz.imi.bst.trec.model.Result;
import at.medunigraz.imi.bst.trec.model.ResultList;
import at.medunigraz.imi.bst.trec.model.Topic;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TrecWriterTest {
	private static final String OUTPUT = "TrecWriterTest.trec_results";
	private static final String RUN_NAME = "test";

	@Rule
	public TemporaryFolder testFolder = new TemporaryFolder();

	@Test
	public void testWrite() throws IOException {
		File output = testFolder.newFile(OUTPUT);
		TrecWriter tw = new TrecWriter(output, RUN_NAME);

		Result result = new Result(String.valueOf(28410400), 2.5f);
		
		Topic topic1 = new Topic().withNumber(1);
		ResultList resultList1 = new ResultList(topic1);	
		resultList1.add(result);

		tw.write(resultList1);
		tw.flush();

		assertTrue(output.exists());

		String actual = FileUtils.readFileToString(output, "UTF-8");
		String expected1 = String.join("\t", "1", "Q0", "28410400", "1", "2.500000", RUN_NAME) + "\n";
		assertEquals(expected1, actual);

		// Second topic with the same result
		Topic topic2 = new Topic().withNumber(2);
		ResultList resultList2 = new ResultList(topic2);
		resultList2.add(result);
		
		tw.write(resultList2);
		tw.flush();

		actual = FileUtils.readFileToString(output, "UTF-8");
		String expected2 = String.join("\t", "2", "Q0", "28410400", "1", "2.500000", RUN_NAME) + "\n";
		assertEquals(expected1 + expected2, actual);

		tw.close();
	}
}
