package de.julielab.ir.paramopt;

import de.julielab.java.utilities.ConfigurationUtilities;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.assertj.core.data.Offset;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static de.julielab.java.utilities.ConfigurationUtilities.last;
import static de.julielab.java.utilities.ConfigurationUtilities.ws;
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

    @Test
    public void maeh() throws IOException {
        SmacLiveRundataReader reader = new SmacLiveRundataReader();
        SmacLiveRundata data = reader.read(new File("smac-output/allparams_ba_split4/live-rundata-3.json"));
        List<String> header = new ArrayList<>();
        List<List<String>> rows = new ArrayList<>();
        for (SmacLiveRundataEntry e : data) {
            if (header.isEmpty()) {
                header.add("time");
                for (String paramname : e.getRunInfo().getConfiguration().getSettings().keySet())
                    header.add(paramname);
            }

            if (e.getWallclockTime()>1000) {
                List<String> row = new ArrayList<>();
                row.add(String.valueOf(e.getWallclockTime()));
                for (String paramname : e.getRunInfo().getConfiguration().getSettings().keySet())
                    row.add(e.getRunInfo().getConfiguration().getSettings().get(paramname));
                rows.add(row);
            }
        }
        System.out.println(String.join("\t", header));
        for (List<String> row : rows)
            System.out.println(String.join("\t", row));
    }

    @Test
    public void maeh2() throws Exception {
        SmacLiveRundataReader reader = new SmacLiveRundataReader();
        SmacLiveRundata data = reader.read(new File("smac-output/allparams_ba_split4/live-rundata-3.json"));
        Map<String, String> settings = data.getEntryWithBestScore().getRunInfo().getConfiguration().getSettings();
        String[] p = new String[5 + settings.size()*2];
        int i = 5;
        for (String key : settings.keySet()) {
            p[i] = "-"+key;
            p[i + 1] = settings.get(key);
            i += 2;
        }
        HierarchicalConfiguration<ImmutableNode> c = parseConfiguration(p);
        ConfigurationUtilities.writeConfiguration(c, new File("config.xml"));

    }

    protected HierarchicalConfiguration<ImmutableNode> parseConfiguration(String[] args) throws ConfigurationException {
        final HierarchicalConfiguration<ImmutableNode> config = ConfigurationUtilities.createEmptyConfiguration();
        for (int i = 5; i < args.length; i++) {
            String parameter = args[i];
            if (i % 2 == 1) {
                parameter = "/" + parameter.substring(1);
                if (i == args.length - 1)
                    throw new IllegalArgumentException("The parameter " + parameter + " has no value specified.");
                String value = args[i + 1];
                if (parameter.contains("@")) {
                    String[] attrSplit = parameter.split("@");
                    String pathUntilAttr = attrSplit[0].replaceAll("\\.", "/");
                    String[] attrAndValue = attrSplit[1].split(":");
                    String attributeName = attrAndValue[0];
                    String valueWithoutQuotes = attrAndValue[1];
                    config.addProperty(pathUntilAttr, value);
                    config.addProperty(ws(last(pathUntilAttr), "@" + attributeName), valueWithoutQuotes);
                } else {
                    config.setProperty(parameter.replaceAll("\\.", "/"), value);
                }
            }
        }
        return config;
    }
}