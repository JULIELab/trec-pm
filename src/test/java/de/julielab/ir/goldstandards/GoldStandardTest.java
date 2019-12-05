package de.julielab.ir.goldstandards;

import at.medunigraz.imi.bst.trec.model.Topic;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class GoldStandardTest {

    @Test
    @Ignore
    public void testRandomizedQueryPartitioning() throws InterruptedException {
        final TrecQrelGoldStandard<Topic> gs = TrecPMGoldStandardFactory.pubmedInternal2019();
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
    @Ignore
    public void testEqualSplitSize() {
        final TrecQrelGoldStandard<Topic> gs = TrecPMGoldStandardFactory.pubmedInternal2019();
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
}
