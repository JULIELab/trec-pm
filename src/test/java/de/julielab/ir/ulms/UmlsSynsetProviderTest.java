package de.julielab.ir.ulms;

import com.google.common.collect.Sets;
import de.julielab.ir.umls.UmlsSynsetProvider;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class UmlsSynsetProviderTest {

    private class UmlsSynsetTestProvider extends UmlsSynsetProvider{
        private UmlsSynsetTestProvider(boolean containTermInSynset) {
            super("src/test/resources/umls/example.synsets", "src/test/resources/umls/semanticTypes.test", UmlsSynsetProvider.DEFAULT_SEPARATOR, containTermInSynset, false);
        }
    }

    @Test
    public void testContainTerm()  {
        UmlsSynsetProvider u = new UmlsSynsetTestProvider(true);
        assertEquals(0, u.getSynsets("X").size());
        assertEquals(1, u.getSynsets("F").size());
        assertEquals(2, u.getSynsets("A").size());
        assertEquals(Sets.newHashSet(Sets.newHashSet("A", "B", "C"), Sets.newHashSet("A", "F", "G")), u.getSynsets("A"));
    }

    @Test
    public void testNotContainTerm() {
        UmlsSynsetProvider u = new UmlsSynsetTestProvider(false);
        assertEquals(0, u.getSynsets("X").size());
        assertEquals(1, u.getSynsets("F").size());
        assertEquals(2, u.getSynsets("A").size());
        assertEquals(Sets.newHashSet(Sets.newHashSet("B", "C"), Sets.newHashSet("F", "G")), u.getSynsets("A"));
    }

    @Test
    public void testGetCuis() {
        UmlsSynsetProvider u = new UmlsSynsetTestProvider(false);
        assertThat(u.getCuis("A")).containsExactlyInAnyOrder("CUI1", "CUI2");
        assertThat(u.getCuis("B")).containsExactlyInAnyOrder("CUI1");
        assertThat(u.getCuis("C")).containsExactlyInAnyOrder("CUI1");
        assertThat(u.getCuis("F")).containsExactlyInAnyOrder("CUI2");
        assertThat(u.getCuis("G")).containsExactlyInAnyOrder("CUI2");
    }

    @Test
    public void getSemanticTypeForCui() {
        UmlsSynsetProvider u = new UmlsSynsetTestProvider(false);
        assertThat(u.getSemanticTypeForCui("CUI1")).containsExactly("T121");
    }

    @Test
    public void getSemanticTypesForTerm() {
        UmlsSynsetProvider u = new UmlsSynsetTestProvider(false);
        // The term 'A' is associated with CUI1 and CUI2 in the examplet.synsets file. Those
        // have been set the semantic types tested for.
        assertThat(u.getSemanticTypes("A")).containsExactlyInAnyOrder("T121", "T130", "T109");
    }
}
