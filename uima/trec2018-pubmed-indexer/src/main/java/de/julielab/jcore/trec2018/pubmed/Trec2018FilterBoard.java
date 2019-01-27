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
    @ExternalResource(key = "goldstandard")
    private List<String> gsData;

    @ExternalResource(key = "lstm2017")
    public Map<String, Double> lstm2017;
    @ExternalResource(key = "lstmatt2017")
    public Map<String, Double> lstmatt2017;
    @ExternalResource(key = "gru2017")
    public Map<String, Double> gru2017;

    @ExternalResource(key = "lstm2018")
    public Map<String, Double> lstm2018;
    @ExternalResource(key = "lstmatt2018")
    public Map<String, Double> lstmatt2018;
    @ExternalResource(key = "gru2018")
    public Map<String, Double> gru2018;


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
    }
}
