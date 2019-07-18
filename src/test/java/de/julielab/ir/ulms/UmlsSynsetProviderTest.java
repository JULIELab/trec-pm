package de.julielab.ir.ulms;

import com.google.common.collect.Sets;
import de.julielab.ir.umls.UmlsSynsetProvider;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.assertj.core.api.Assertions.*;

public class UmlsSynsetProviderTest {

    @Test
    public void testContainTerm() throws IOException {
        UmlsSynsetProvider u = new UmlsSynsetProvider("src/test/resources/umls/example.synsets", true, false);
        assertEquals(0, u.getSynsets("X").size());
        assertEquals(1, u.getSynsets("F").size());
        assertEquals(2, u.getSynsets("A").size());
        assertEquals(Sets.newHashSet(Sets.newHashSet("A", "B", "C"), Sets.newHashSet("A", "F", "G")), u.getSynsets("A"));
    }

    @Test
    public void testNotContainTerm() throws IOException {
        UmlsSynsetProvider u = new UmlsSynsetProvider("src/test/resources/umls/example.synsets", false, false);
        assertEquals(0, u.getSynsets("X").size());
        assertEquals(1, u.getSynsets("F").size());
        assertEquals(2, u.getSynsets("A").size());
        assertEquals(Sets.newHashSet(Sets.newHashSet("B", "C"), Sets.newHashSet("F", "G")), u.getSynsets("A"));
    }

    @Test
    public void testGetCuis() throws IOException {
        UmlsSynsetProvider u = new UmlsSynsetProvider("src/test/resources/umls/example.synsets", false, false);
        assertThat(u.getCuis("A")).containsExactlyInAnyOrder("CUI1", "CUI2");
        assertThat(u.getCuis("B")).containsExactlyInAnyOrder("CUI1");
        assertThat(u.getCuis("C")).containsExactlyInAnyOrder("CUI1");
        assertThat(u.getCuis("F")).containsExactlyInAnyOrder("CUI2");
        assertThat(u.getCuis("G")).containsExactlyInAnyOrder("CUI2");
    }
}
