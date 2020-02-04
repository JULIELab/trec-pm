package de.julielab.ir.paramopt;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.julielab.java.utilities.FileUtilities;

import java.io.*;
import java.nio.CharBuffer;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SmacLiveRundataReader {
    public SmacLiveRundata read(InputStream is) throws IOException {
        ObjectMapper om = new ObjectMapper();
        SmacLiveRundata smacLiveRundata = new SmacLiveRundata();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, UTF_8))) {
            CharBuffer buf = CharBuffer.allocate(8192);
            int numRead;
            StringBuilder sb = new StringBuilder();
            int numOpeningBraces = 0;
            boolean doRecord = false;
            int totalRead = 0;
            while ((numRead = br.read(buf)) != -1) {
                totalRead += numRead;
                for (int i = 0; i < numRead; i++) {
                    if (buf.get(i) == '{') {
                        ++numOpeningBraces;
                        if (numOpeningBraces == 1)
                            doRecord = true;
                    } else if (buf.get(i) == '}') {
                        if (numOpeningBraces == 1) {
                            // append the current closing brace so we can go on and convert the json into the object
                            sb.append(buf.get(i));
                            String json = sb.toString();
                            if (json.contains("@rc-id")) {
                                SmacLiveRundataEntry entry = om.readValue(json, SmacLiveRundataEntry.class);
                                smacLiveRundata.add(entry);
                            }
                            sb.delete(0, sb.length());
                            doRecord = false;
                        }
                        --numOpeningBraces;
                    }
                    if (doRecord)
                        sb.append(buf.get(i));
                }
                buf.clear();
            }
        }
        return smacLiveRundata;
    }

    public SmacLiveRundata read(File livedataFile) throws IOException {
        return read(FileUtilities.getInputStreamFromFile(livedataFile));
    }
}
