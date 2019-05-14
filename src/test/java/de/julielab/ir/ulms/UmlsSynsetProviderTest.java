package de.julielab.ir.ulms;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import at.medunigraz.imi.bst.trec.model.Challenge;
import at.medunigraz.imi.bst.trec.model.Task;
import at.medunigraz.imi.bst.trec.model.Topic;
import at.medunigraz.imi.bst.trec.model.TopicSet;
import de.julielab.ir.umls.UmlsSynsetProvider;
import org.junit.Test;

import com.google.common.collect.Sets;

public class UmlsSynsetProviderTest {

	@Test
	public void testContainTerm() throws IOException {
		UmlsSynsetProvider u = new UmlsSynsetProvider("src/test/resources/umls/example.synsets", true, false);
		assertEquals(0,u.getSynsets("X").size());
		assertEquals(1,u.getSynsets("F").size());
		assertEquals(2,u.getSynsets("A").size());
		assertEquals(Sets.newHashSet(Sets.newHashSet("A", "B", "C"), Sets.newHashSet("A", "F", "G")),u.getSynsets("A"));
	}
	
	@Test
	public void testNotContainTerm() throws IOException {
		UmlsSynsetProvider u = new UmlsSynsetProvider("src/test/resources/umls/example.synsets", false, false);
		assertEquals(0,u.getSynsets("X").size());
		assertEquals(1,u.getSynsets("F").size());
		assertEquals(2,u.getSynsets("A").size());
		assertEquals(Sets.newHashSet(Sets.newHashSet("B", "C"), Sets.newHashSet("F", "G")),u.getSynsets("A"));
	}
}
