package de.julielab.ir.pm.pmclassifier;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.julielab.java.utilities.FileUtilities;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class DataReader {
    private static final Logger LOG = LoggerFactory.getLogger(DataReader.class);

    private DataReader() {
    }

    public static Map<String, Document> readDocuments(File documentJsonZip) throws DataReadingException {
        Map<String, Document> docsById;

        try {
            docsById = new HashMap<>();
            ObjectMapper om = new ObjectMapper();
            ZipFile zipFile = new ZipFile(documentJsonZip);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                if (zipEntry.isDirectory())
                    continue;
                InputStream inputStream = zipFile.getInputStream(zipEntry);
                if (zipEntry.getName().toLowerCase().endsWith(".gz") || zipEntry.getName().toLowerCase().endsWith(".gzip"))
                    inputStream = new GZIPInputStream(inputStream);
                Document document = om.readValue(inputStream, Document.class);
                docsById.put(document.getId(), document);
            }
        } catch (IOException e) {
            throw new DataReadingException(e);
        }
        return docsById;
    }

    public static void addPMLabels(File gsTable, Map<String, Document> docsById) throws DataReadingException {
        int pmAndNotPMCounter = 0;
        try (CSVParser csvRecords = CSVFormat.TDF.withFirstRecordAsHeader().parse(FileUtilities.getReaderFromFile(gsTable))) {
            Iterator<CSVRecord> it = csvRecords.iterator();
            while (it.hasNext()) {
                CSVRecord record = it.next();
                String trecDocId = record.get("trec_doc_id");
                String pmRelDesc = record.get("pm_rel_desc");
                if (pmRelDesc.equalsIgnoreCase("Human PM") || pmRelDesc.equalsIgnoreCase("Animal PM"))
                    pmRelDesc = "PM";
                Document doc = docsById.get(trecDocId);
                if (doc == null) {
                    LOG.warn("Null document for doc ID " + trecDocId + ". Record: " + record);
                    continue;
                }
                if (doc.getPmLabel() != null && doc.getPmLabel().equals("Not PM") && pmRelDesc.equalsIgnoreCase("PM"))
                    ++pmAndNotPMCounter;
                if (doc.getPmLabel() != null && doc.getPmLabel().equals("PM") && pmRelDesc.equalsIgnoreCase("Not PM"))
                    ++pmAndNotPMCounter;
                // "Once PM, always PM"
                if (doc.getPmLabel() == null || doc.getPmLabel().equalsIgnoreCase("Not PM")) {
                    doc.setPMLabel(pmRelDesc);
                }
            }
            LOG.info("Encountered {} documents labeled as PM and also as Not PM for different queries", pmAndNotPMCounter);
        } catch (IOException e1) {
            throw new DataReadingException(e1);
        }
    }

    public static List<String> getTextTerms(Document document) throws IOException {
        String documentText = document.getTitle() + " " + document.getAbstractText();
        return getTextTerms(documentText);
    }

    public static List<String> getTextTerms(String text) throws IOException {
        StandardAnalyzer analyzer = new StandardAnalyzer();
        TokenStream ts = analyzer.tokenStream("text", text);
        ts.reset();
        CharTermAttribute termAtt = ts.getAttribute(CharTermAttribute.class);
        List<String> terms = new ArrayList<>();
        while (ts.incrementToken()) {
            String s = new String(termAtt.buffer(), 0, termAtt.length());
            terms.add(s);
        }
        return terms;
    }
}
