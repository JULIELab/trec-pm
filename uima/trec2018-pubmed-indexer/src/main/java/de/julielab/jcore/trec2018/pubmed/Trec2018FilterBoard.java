package de.julielab.jcore.trec2018.pubmed;

import de.julielab.jcore.consumer.es.ExternalResource;
import de.julielab.jcore.consumer.es.FilterBoard;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;

public class Trec2018FilterBoard extends FilterBoard {

    public Map<String, List<CSVRecord>> gsRecords;
    public Map<String, Integer> gsHeaderMap;
    public Map<String, Double> lstmpm;
    public Map<String, Double> lstmpmatt;
    @ExternalResource(key = "goldstandard")
    private List<String> gsData;
    /**
     * The lines of a file that included pairs of classification score to PMID
     * for a cascaded LSTM NN predicting the PM class.
     */
    @ExternalResource(key = "lstmpm")
    private List<String> lstmpmresults;

    /**
     * The lines of a file that included pairs of classification score to PMID
     * for a cascaded LSTM with attention NN predicting the PM class.
     */
    @ExternalResource(key = "lstmpmatt")
    private List<String> lstmpmattresults;

    @Override
    public void setupFilters() {
        gsRecords = new HashMap<>();
        try (CSVParser csvRecords = CSVFormat.TDF.withFirstRecordAsHeader().parse(new StringReader(gsData.stream().collect(Collectors.joining(System.getProperty("line.separator")))))) {
            gsHeaderMap = csvRecords.getHeaderMap();
            Iterator<CSVRecord> it = csvRecords.iterator();
            while (it.hasNext()) {
                CSVRecord record = it.next();
                String trecDocId = record.get("trec_doc_id");
                gsRecords.compute(trecDocId, (id, list) -> {
                    List<CSVRecord> l = list;
                    if (list == null) l = new ArrayList<>();
                    l.add(record);
                    return l;
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (lstmpmresults != null)
            lstmpm = lstmpmresults.stream().map(l -> l.trim().split("\\s+")).collect(Collectors.toMap(a -> a[1], a -> Double.parseDouble(a[0])));
        if (lstmpmatt != null)
            lstmpmatt = lstmpmattresults.stream().map(l -> l.trim().split("\\s+")).collect(Collectors.toMap(a -> a[1], a -> Double.parseDouble(a[0])));
    }
}
