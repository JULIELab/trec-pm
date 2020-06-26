package at.medunigraz.imi.bst.retrieval;

import at.medunigraz.imi.bst.trec.model.Result;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import de.julielab.ir.goldstandards.GoldStandard;
import de.julielab.ir.ltr.Document;
import de.julielab.ir.ltr.DocumentList;
import de.julielab.ir.model.QueryDescription;
import de.julielab.java.utilities.FileUtilities;
import de.julielab.java.utilities.IOStreamUtilities;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>Removes results for a given query when the result is contained in the given gold standard for this query.</p>
 *
 * @param <Q>
 */
public class ResultListGoldStandardFilterDecorator<Q extends QueryDescription> extends QueryDecorator<Q> {
    private final static Logger log = LoggerFactory.getLogger(ResultListGoldStandardFilterDecorator.class);
    private final GoldStandard<Q> gs;
    private Function<String, String> documentIdMappingFunction;
    private Function<Result, String> resultDocIdFunction;

    public ResultListGoldStandardFilterDecorator(Query<Q> decoratedQuery, @Nullable Function<Result, String> resultDocIdFunction, String documentIdMapping, GoldStandard<Q> gs) {
        super(decoratedQuery);
        this.resultDocIdFunction = resultDocIdFunction;
        this.documentIdMappingFunction = loadDocumentIdMapping(documentIdMapping);
        this.gs = gs;
    }

    private Function<String, String> loadDocumentIdMapping(String idMapping) {
        if (idMapping != null) {
            final Map<String, String> map = new HashMap<>();
            try (BufferedReader br = IOStreamUtilities.getReaderFromInputStream(FileUtilities.findResource(idMapping));
                 CSVReader csvReader = new CSVReaderBuilder(br).withCSVParser(new CSVParserBuilder().withSeparator(',').build()).build()) {
                for (String[] record : csvReader) {
                    map.put(record[0], record[1]);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            log.debug("Loaded document ID mapping with {} entries from {}", map.size(), idMapping);
            return sourceId -> map.getOrDefault(sourceId, sourceId);
        }
        return Function.identity();
    }

    @Override
    public List<Result> query(Q topic) {
        List<Result> results = decoratedQuery.query(topic);
        try {
            DocumentList<Q> qrelDocumentsForQuery = gs.getQrelDocuments().stream().filter(d -> d.getQueryDescription().getNumber() == topic.getNumber()).collect(Collectors.toCollection(DocumentList::new));
            if (qrelDocumentsForQuery != null) {
                Set<String> docIdsInGs4topic = qrelDocumentsForQuery.stream().map(Document::getId).map(documentIdMappingFunction).collect(Collectors.toSet());
                List<Result> filteredresults = new ArrayList<>();
                for (Result r : results) {
                    String rId = resultDocIdFunction == null ? r.getId() : resultDocIdFunction.apply(r);
                    if (!docIdsInGs4topic.contains(rId))
                        filteredresults.add(r);
                    else {
                        log.debug("Filtered out: {}", rId);
                    }
                }
                return filteredresults;
            }
        } catch (IllegalArgumentException e) {
            // do nothing; this topic is not included in the  gold standard so we can't filter for its results
        }
        return results;
    }
}
