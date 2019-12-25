package de.julielab.ir.es;

import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;

public class RepairIndexSettings {
    public static void main(String args[]) throws IOException {

        // Load the existing, possibly corrupt index file
        IndexMetaData d = IndexMetaData.FORMAT.read(NamedXContentRegistry.EMPTY, Paths.get( "indexstaterepair","_state", "state-11.st"));

        // Create new IndexMetaData by copying all the valid meta data from the original and removing or fixing
        // the corrupt settings.
        Settings.Builder sb = Settings.builder();
        for (String key : d.getSettings().keySet()) {
            if (!key.contains("similarity"))
                sb.put(key, d.getSettings().get(key));
        }
        IndexMetaData newd = IndexMetaData.builder(d).settings(sb).build();

        // Write the new index state to file
        IndexMetaData.FORMAT.write(newd, Paths.get("indexstaterepair"));

        // Just for a quick look
        try (OutputStream os = new FileOutputStream("newstate.st")) {
            XContentBuilder builder = XContentFactory.smileBuilder(os);
            builder.startObject();
            IndexMetaData.FORMAT.toXContent(builder, newd);
            builder.endObject();
        }
    }
}
