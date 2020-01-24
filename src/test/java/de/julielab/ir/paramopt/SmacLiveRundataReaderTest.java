package de.julielab.ir.paramopt;

import org.assertj.core.data.Offset;
import org.junit.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
public class SmacLiveRundataReaderTest {

    @Test
    public void read() throws Exception {
        SmacLiveRundataReader reader = new SmacLiveRundataReader();
        SmacLiveRundata data = reader.read(Path.of("src", "test", "resources", "paramopt", "live-rundata-1.json.gz").toFile());
        assertThat(data).isNotNull();
        assertThat(data).hasSize(304);
        SmacLiveRundataEntry entry = data.get(0);
        assertThat(entry.getRunInfo().getRcid()).isEqualTo(2);
        assertThat(entry.getrQuality()).isCloseTo(-0.4863, Offset.offset(0.00001));
        assertThat(entry.getWallclockTime()).isCloseTo(362.967, Offset.offset(0.00001));
        assertThat(entry.getRunInfo().getConfiguration().isDefaultConfig()).isEqualTo(true);
        assertThat(entry.getRunInfo().getConfiguration().getSettings())
                .hasSize(109)
                .containsEntry("indexparameters.bm25.b", "0.75")
                .containsEntry("retrievalparameters.geneexpansion.description", "false")
                .containsEntry("retrievalparameters.keywords.negativepm@word:specific", "true")
                .containsEntry("retrievalparameters.templateparameters.gene.phraseslops.gene_topic_slop", "0");

        entry = data.get(303);
        assertThat(entry.getRunInfo().getRcid()).isEqualTo(305);
        assertThat(entry.getrQuality()).isCloseTo(-0.1102, Offset.offset(0.00001));
        assertThat(entry.getWallclockTime()).isCloseTo(1312.215, Offset.offset(0.00001));
        assertThat(entry.getRunInfo().getConfiguration().isDefaultConfig()).isEqualTo(false);
        assertThat(entry.getRunInfo().getConfiguration().getSettings())
                .hasSize(109)
                .containsEntry("indexparameters.bm25.b", "0.2406659572488089")
                .containsEntry("retrievalparameters.geneexpansion.description", "false")
                .containsEntry("retrievalparameters.keywords.negativepm@word:specific", "true")
                .containsEntry("retrievalparameters.templateparameters.gene.phraseslops.gene_topic_slop", "0");
    }
}