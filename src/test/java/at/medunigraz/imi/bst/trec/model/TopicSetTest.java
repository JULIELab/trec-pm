package at.medunigraz.imi.bst.trec.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.junit.Test;

public class TopicSetTest {

	@Test
	public void testFromXML() {
		File topicsFile = new File(getClass().getResource("/topics/topics2017.xml").getPath());
		List<Topic> topics = (new TopicSet(topicsFile, Challenge.TREC_PM, Task.PUBMED, 2017)).getTopics();
		
		assertEquals(30, topics.size());
		assertTrue(topics.contains(new Topic().withChallenge(Challenge.TREC_PM).withTask(Task.PUBMED).withYear(2017).withNumber(30)));
		
		Topic firstTopic = topics.iterator().next();
		assertEquals(1, firstTopic.getNumber());
		assertEquals("Liposarcoma", firstTopic.getDisease());
		assertEquals("CDK4 Amplification", firstTopic.getGene());
		assertEquals("38-year-old male", firstTopic.getDemographic());
		assertEquals("GERD", firstTopic.getOther());
	}

}
