package de.julielab.ir.goldstandards;

import de.julielab.ir.ltr.Document;
import de.julielab.ir.model.QueryDescription;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class defines an aggregation of datasets using the traditional TREC Qrels file format.
 *
 * @param <Q> The query class used with this dataset.
 */
public class AggregatedTrecQrelGoldStandard<Q extends QueryDescription> extends AggregatedGoldStandard<Q> {

    public static final Function<QueryDescription, String> CROSS_DATASET_QUERY_ID_FUNCTION = q -> q.getYear() + "" + q.getNumber();

    private final static Logger log = LogManager.getLogger();
    private static Function<Document, String> qrelRecordFunction = gsDoc -> Stream.of(CROSS_DATASET_QUERY_ID_FUNCTION.apply(gsDoc.getQueryDescription()), "Q0", gsDoc.getId(), String.valueOf(gsDoc.getRelevance())).collect(Collectors.joining("\t"));
    private static Function<Document, String> sampleQrelRecordFunction = gsDoc -> Stream.of(CROSS_DATASET_QUERY_ID_FUNCTION.apply(gsDoc.getQueryDescription()), "Q0", gsDoc.getId(), String.valueOf(gsDoc.getStratum()), String.valueOf(gsDoc.getRelevance())).collect(Collectors.joining("\t"));

    public AggregatedTrecQrelGoldStandard(File qrelFile, TrecQrelGoldStandard<Q>... goldStandards) {
        this(qrelFile, null, goldStandards);
    }

    public AggregatedTrecQrelGoldStandard(File qrelFile, File sampleQrelFile, TrecQrelGoldStandard<Q>... goldStandards) {
        super(log, qrelFile, sampleQrelFile, qrelRecordFunction, sampleQrelRecordFunction, goldStandards);

    }

    @Override
    public void writeQrelFile(File qrelFile) {
        writeAggregatedQrelFile(qrelFile, goldStandards.values().toArray(new GoldStandard[0]), gs -> gs.getQrelDocuments(), qrelRecordFunction);
    }

    @Override
    public Function<QueryDescription, String> getQueryIdFunction() {
        return q -> q.getYear() + "" + q.getNumber();
    }
}
