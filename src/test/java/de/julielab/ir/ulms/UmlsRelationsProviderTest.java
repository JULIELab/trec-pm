package de.julielab.ir.ulms;

import de.julielab.ir.umls.UmlsRelationsProvider;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
public class UmlsRelationsProviderTest {
    @Test
    public void testGetParents() throws Exception {
        final UmlsRelationsProvider rp = new UmlsRelationsProvider("src/test/resources/umls/example.relations", false);
        assertThat(rp.getRelatives("CUI1", UmlsRelationsProvider.Relation.PARENT)).containsExactly("CUI2");
        assertThat(rp.getRelatives("CUI2", UmlsRelationsProvider.Relation.PARENT)).containsExactlyInAnyOrder("CUI3", "CUI4");

        final List<Set<String>> parentLevels = rp.getRelatives("CUI1", UmlsRelationsProvider.Relation.PARENT, 2);
        assertThat(parentLevels).hasSize(2);
        assertThat(parentLevels.get(0)).containsExactly("CUI2");
        assertThat(parentLevels.get(1)).containsExactlyInAnyOrder("CUI3", "CUI4");
    }

    @Test
    public void testGetChildren() throws Exception {
        final UmlsRelationsProvider rp = new UmlsRelationsProvider("src/test/resources/umls/example.relations", false);
        assertThat(rp.getRelatives("CUI2", UmlsRelationsProvider.Relation.CHILD)).containsExactly("CUI1");
        assertThat(rp.getRelatives("CUI3", UmlsRelationsProvider.Relation.CHILD)).containsExactlyInAnyOrder("CUI2");
        assertThat(rp.getRelatives("CUI4", UmlsRelationsProvider.Relation.CHILD)).containsExactlyInAnyOrder("CUI2");

        final List<Set<String>> parentLevels = rp.getRelatives("CUI3", UmlsRelationsProvider.Relation.CHILD, 2);
        assertThat(parentLevels).hasSize(2);
        assertThat(parentLevels.get(0)).containsExactly("CUI2");
        assertThat(parentLevels.get(1)).containsExactly("CUI1");
    }
}
