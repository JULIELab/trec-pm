package de.julielab.ir.goldstandards;

import at.medunigraz.imi.bst.trec.model.Challenge;
import at.medunigraz.imi.bst.trec.model.GoldStandardType;
import at.medunigraz.imi.bst.trec.model.QueryDescriptionSet;
import at.medunigraz.imi.bst.trec.model.Task;
import de.julielab.ir.ltr.Document;
import de.julielab.ir.ltr.DocumentList;
import de.julielab.ir.model.QueryDescription;
import de.julielab.ir.sheets.GoogleSheets;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GoogleSheetsGoldStandard<Q extends QueryDescription> extends AtomicGoldStandard<Q> {
    private static final Logger LOG = LoggerFactory.getLogger(GoogleSheetsGoldStandard.class);

    private static final int TOPIC_COLUMN = 0;
    private static final int DOC_COLUMN = 2;
    private static final int RELEVANCE_COLUMN = 3;
    private static final int MIN_ROW_SIZE = 3;

    private final String spreadsheetId;
    private final String[] readRange;
    private final String writeRange;

    private final GoogleSheets sheet = new GoogleSheets();

    public GoogleSheetsGoldStandard(Challenge challenge, Task task, int year, QueryDescriptionSet<Q> queries, String spreadsheetId, String[] readRange, String writeRange) {
        super(challenge, task, year, GoldStandardType.INTERNAL, queries, new DocumentList<>());
        this.spreadsheetId = spreadsheetId;
        this.readRange = readRange;
        this.writeRange = writeRange;
        qrelDocuments = read();
    }

    private DocumentList<Q> read() {
        DocumentList<Q> documents = new DocumentList<>();

        List<List<Object>> values = null;
        try {
            values = sheet.read(spreadsheetId, readRange);
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
            final Document<Q> doc = new Document<>();
            Q topic = getQueriesByNumber().get(Integer.valueOf(row.get(TOPIC_COLUMN).toString()));
            doc.setQueryDescription(topic);
            doc.setId(row.get(DOC_COLUMN).toString());
            if (row.size() > MIN_ROW_SIZE) {
                doc.setRelevance(Integer.valueOf(row.get(RELEVANCE_COLUMN).toString()));
            }
            documents.add(doc);
        }

        return documents;
    }

    /**
     * Sync the data in memory to the underlying representation, in this case, a Google Spreadsheet.
     */
    public void sync() {
        List<Object[]> values = new ArrayList<>();

        String[] fromColToCol = writeRange.split(":");
        int numCols = fromColToCol[1].charAt(0) - fromColToCol[0].charAt(0) + 1;

        // Header
        values.add(Arrays.copyOf(new Object[]{"Topic", "Q0", "ID", "Rel", "RelFixed", "URL"}, numCols));

        // Don't write duplicates to the gold standard.
        for (Document<Q> doc : qrelDocuments.getSubsetWithUniqueTopicDocumentIds()) {
            values.add(Arrays.copyOf(new Object[]{doc.getQueryDescription().getNumber(), "0", doc.getId(), doc.getRelevance(), "", doc.getUrl()}, numCols));
        }

        int rowsUpdated = -1;
        try {
            rowsUpdated = sheet.write(spreadsheetId, writeRange, values.stream().map(Arrays::asList).collect(Collectors.toList()));
        } catch (IOException e) {
            throw new RuntimeException("Could not write to Google spreadsheet.", e);
        }

        if (rowsUpdated <= 0) {
            LOG.warn("No cells updated.");
        }
    }

    @Override
    public void writeQrelFile(File qrelFile) {
        throw new NotImplementedException("Use TrecQrelGoldStandard.writeQrelFile instead.");
    }

    @Override
    public void writeQrelFile(File qrelFile, Collection<Q> queries) {
        throw new NotImplementedException("Use TrecQrelGoldStandard.writeQrelFile instead.");
    }

    @Override
    public void writeSampleQrelFile(File qrelFile) {
        throw new NotImplementedException("Use TrecQrelGoldStandard.writeSampleQrelFile instead.");
    }

    @Override
    public void writeSampleQrelFile(File qrelFile, Collection<Q> queries) {
        throw new NotImplementedException("Use TrecQrelGoldStandard.writeQrelFile instead.");
    }

    @Override
    public boolean isSampleGoldStandard() {
        return false;
    }

    @Override
    public Function<QueryDescription, String> getQueryIdFunction() {
        return q -> String.valueOf(q.getNumber());
    }


    @Override
    protected void setIndexToQuery(Q query) {
        // not implemented, use default index
    }
}
