package de.julielab.ir.goldstandards;

import de.julielab.ir.ltr.Document;
import de.julielab.ir.ltr.DocumentList;
import de.julielab.ir.model.QueryDescription;
import de.julielab.java.utilities.FileUtilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AggregatedTrecQrelGoldStandard<Q extends QueryDescription> extends AggregatedGoldStandard<Q> {

    private final static Logger log = LogManager.getLogger();
    public AggregatedTrecQrelGoldStandard(File qrelFile, TrecQrelGoldStandard<Q>... goldStandards) {
        super(goldStandards);
        if (!qrelFile.exists()) {
            if (!qrelFile.getParentFile().exists())
                qrelFile.getParentFile().mkdirs();
            try (final BufferedWriter bw = FileUtilities.getWriterToFile(qrelFile)) {
                for (TrecQrelGoldStandard<Q> gs : goldStandards) {
                    final DocumentList<Q> gsDocs = gs.getDocuments();
                    for (Document gsDoc : gsDocs) {
                        String line = Stream.of(gsDoc.getQueryDescription().getCrossDatasetId(), "Q0", gsDoc.getId(), String.valueOf(gsDoc.getRelevance())).collect(Collectors.joining("\t"));
                        bw.write(line);
                        bw.newLine();
                    }
                }
            } catch (IOException e) {
                log.error("Exception while writing aggregated qrel file to {}",qrelFile, e );
            }
        }
    }
}
