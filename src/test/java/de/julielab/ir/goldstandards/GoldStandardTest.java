package de.julielab.ir.goldstandards;

import at.medunigraz.imi.bst.trec.model.Topic;
import de.julielab.java.utilities.ConfigurationUtilities;
import org.apache.commons.configuration2.ConfigurationUtils;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class GoldStandardTest {

    @Test
    public void testRandomizedQueryPartitioning() throws InterruptedException {
        final TrecQrelGoldStandard<Topic> gs = TrecPMGoldStandardFactory.pubmedOfficial2019();
        final List<List<Topic>> split = gs.createRandomizedQueryPartitioning(10, 1);
        assertEquals(10, split.size());
        assertEquals(40, split.stream().flatMap(Collection::stream).collect(Collectors.toList()).size());
        assertEquals(4, (int) split.stream().mapToDouble(Collection::size).average().getAsDouble());
        for (int i = 0; i < split.size(); i++) {
            List<Topic> topics = split.get(i);
            assertFalse("Split " + i + " is empty.", topics.isEmpty());
            assertEquals("Split " + i + " is of size " + topics.size() + ".", 4, topics.size());
        }

        Function<List<List<Topic>>, String> splitIdFunction = l -> l.stream().flatMap(Collection::stream).map(Topic::getNumber).map(String::valueOf).collect(Collectors.joining("-"));
        String originalQueryNumberOrder = splitIdFunction.apply(split);

        final String reproducedQueryNumberOrder = splitIdFunction.apply(gs.createRandomizedQueryPartitioning(10, 1));
        assertEquals(originalQueryNumberOrder, reproducedQueryNumberOrder);

        for (int i = 0; i < 10; i++) {
            int seed = (int) System.nanoTime();
            final List<List<Topic>> randomSplit = gs.createRandomizedQueryPartitioning(10, seed);
            final String randomOrderId = splitIdFunction.apply(randomSplit);
            assertNotEquals(originalQueryNumberOrder, randomOrderId);
            Thread.sleep(5);
        }
    }

    @Test
    public void testEqualSplitSize() {
        final TrecQrelGoldStandard<Topic> gs = TrecPMGoldStandardFactory.pubmedOfficial2019();
        final List<List<Topic>> split = gs.createRandomizedQueryPartitioning(7, 1);
        assertEquals(7, split.size());
        assertEquals(40, split.stream().flatMap(Collection::stream).collect(Collectors.toList()).size());
        assertEquals(5, (int) split.stream().mapToDouble(Collection::size).average().getAsDouble());
        for (int i = 0; i < split.size(); i++) {
            List<Topic> topics = split.get(i);
            assertFalse("Split " + i + " is empty.", topics.isEmpty());
            int expectedSize = 6;
            if (i >= 5)
                expectedSize = 5;
            assertEquals("Split " + i + " is of size " + topics.size() + ".", expectedSize, topics.size());
        }
    }

    @Test
    public void testBalancedSplit() {
        AggregatedGoldStandard<Topic> gs = new AggregatedTrecQrelGoldStandard(TrecPMGoldStandardFactory.pubmedOfficial2017(), TrecPMGoldStandardFactory.pubmedOfficial2018(), TrecPMGoldStandardFactory.pubmedOfficial2019());
        List<List<Topic>> geneBalancedPartioning = gs.createPropertyBalancedQueryPartitioning(10, Arrays.asList(Topic::getGeneField));
        for (List<Topic> l : geneBalancedPartioning) {
            long c = l.stream().map(Topic::getGeneField).filter(gene -> gene.contains("BRAF")).count();
            assertTrue(c > 0);
        }
    }

    @Test
    public void testMultiplyBalancedSplit() {
        AggregatedGoldStandard<Topic> gs = new AggregatedTrecQrelGoldStandard(TrecPMGoldStandardFactory.pubmedOfficial2017(), TrecPMGoldStandardFactory.pubmedOfficial2018(), TrecPMGoldStandardFactory.pubmedOfficial2019());
        List<List<Topic>> geneBalancedPartioning = gs.createPropertyBalancedQueryPartitioning(10, Arrays.asList(Topic::getDisease));
        for (List<Topic> l : geneBalancedPartioning) {
            System.out.println(l.stream().map(t -> t.getDisease() + ", " + t.getGeneField()).collect(Collectors.joining("-")));
            long c = l.stream().map(Topic::getDisease).filter(d -> d.toLowerCase().contains("melanoma")).count();
            assertTrue(c > 1);
            c = l.stream().map(Topic::getGeneField).filter(gene -> gene.contains("BRCA")).count();
            assertTrue(c <= 1);
        }
    }

    @Test
    public void muh() throws ConfigurationException {
        HierarchicalConfiguration<ImmutableNode> c = ConfigurationUtilities.createEmptyConfiguration();

        String xpath = "/retrievalparameters/keywords/chemotherapy/word";
        c.addProperty(xpath, "");
        c.addProperty(xpath+" @attr", "*mab");


        System.out.println(ConfigurationUtils.toString(c));
        System.out.println(c.getString(xpath));
        ConfigurationUtilities.writeConfiguration(c, new File("myconfig.xml"));

    }
}
