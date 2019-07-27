package at.medunigraz.imi.bst.trec.model;

import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TopicSetTest {

	@Test
	public void testFromXML() {
		File topicsFile = new File(getClass().getResource("/topics/topics2017.xml").getPath());
		List<Topic> topics = (new TopicSet(topicsFile, Challenge.TREC_PM, 2017)).getTopics();
		
		assertEquals(30, topics.size());
		assertTrue(topics.contains(new Topic().withChallenge(Challenge.TREC_PM).withYear(2017).withNumber(30)));
		
		Topic firstTopic = topics.iterator().next();
		assertEquals(1, firstTopic.getNumber());
		assertEquals("Liposarcoma", firstTopic.getDisease());
		assertEquals("CDK4 Amplification", firstTopic.getGeneField());
		assertEquals("38-year-old male", firstTopic.getDemographic());
		assertEquals("GERD", firstTopic.getOther());
	}

}
