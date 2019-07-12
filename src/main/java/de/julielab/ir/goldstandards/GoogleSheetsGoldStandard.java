package de.julielab.ir.goldstandards;

import at.medunigraz.imi.bst.trec.model.Challenge;
import at.medunigraz.imi.bst.trec.model.Task;
import de.julielab.ir.ltr.Document;
import de.julielab.ir.ltr.DocumentList;
import de.julielab.ir.model.QueryDescription;
import de.julielab.ir.sheets.GoogleSheets;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;

public class GoogleSheetsGoldStandard<Q extends QueryDescription> extends AtomicGoldStandard<Q> {
    private static final Logger LOG = LogManager.getLogger();

    private static final int TOPIC_COLUMN = 0;
    private static final int DOC_COLUMN = 2;
    private static final int RELEVANCE_COLUMN = 3;
    private static final int MIN_ROW_SIZE = 3;

    private final String spreadsheetId;
    private final String range;

    private final GoogleSheets sheet = new GoogleSheets();

    public GoogleSheetsGoldStandard(Challenge challenge, Task task, int year, List<Q> queries, String spreadsheetId, String range) {
        super(challenge, task, year, queries, new DocumentList<>());
        this.spreadsheetId = spreadsheetId;
        this.range = range;
        qrelDocuments = read();
    }

    private DocumentList<Q> read() {
        DocumentList<Q> documents = new DocumentList<>();

        List<List<Object>> values = null;
        try {
            values = sheet.read(spreadsheetId, range);
        } catch (IOException e) {
            throw new RuntimeException("Could not read Google spreadsheet.", e);
        }

        if (values == null || values.isEmpty()) {
            LOG.warn("Empty Google spreadsheet.");
            return documents;
        }

        // Remove header
        values.remove(0);

        for (List row : values) {
            if (row.size() > MIN_ROW_SIZE) {
                final Document<Q> doc = new Document<>();
                Q topic = getQueriesByNumber().get(Integer.valueOf(row.get(TOPIC_COLUMN).toString()));
                doc.setQueryDescription(topic);
                doc.setId(row.get(DOC_COLUMN).toString());
                doc.setRelevance(Integer.valueOf(row.get(RELEVANCE_COLUMN).toString()));
                documents.add(doc);
            }
        }

        return documents;
    }

    @Override
    public void writeQrelFile(File qrelFile) {
        throw new NotImplementedException("Use TrecQrelGoldStandard.writeQrelFile instead.");
    }

    @Override
    public Function<QueryDescription, String> getQueryIdFunction() {
        return q -> String.valueOf(q.getNumber());
    }
}
