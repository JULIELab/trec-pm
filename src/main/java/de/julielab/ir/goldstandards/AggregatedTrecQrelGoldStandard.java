package de.julielab.ir.goldstandards;

import at.medunigraz.imi.bst.trec.model.GoldStandardType;
import de.julielab.ir.ltr.Document;
import de.julielab.ir.model.QueryDescription;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class defines an aggregation of datasets using the traditional TREC Qrels file format.
 *
 * @param <Q> The query class used with this dataset.
 */
public class AggregatedTrecQrelGoldStandard<Q extends QueryDescription> extends AggregatedGoldStandard<Q> {

    /**
     * A function that provides an unique query id across datasets. Note that ideally this should provide a sortable
     * representation to be later used by, e.g., R scripts for plotting.
     */
    public static final Function<QueryDescription, String> CROSS_DATASET_QUERY_ID_FUNCTION = q -> q.getYear() + "" + String.format("%02d", q.getNumber());

    private final static Logger log = LogManager.getLogger();
    private static Function<Document, String> qrelRecordFunction = gsDoc -> Stream.of(CROSS_DATASET_QUERY_ID_FUNCTION.apply(gsDoc.getQueryDescription()), "Q0", gsDoc.getId(), String.valueOf(gsDoc.getRelevance())).collect(Collectors.joining("\t"));
    private static Function<Document, String> sampleQrelRecordFunction = gsDoc -> Stream.of(CROSS_DATASET_QUERY_ID_FUNCTION.apply(gsDoc.getQueryDescription()), "Q0", gsDoc.getId(), String.valueOf(gsDoc.getStratum()), String.valueOf(gsDoc.getRelevance())).collect(Collectors.joining("\t"));

    public AggregatedTrecQrelGoldStandard(TrecQrelGoldStandard<Q>... goldStandards) {
        super(log, goldStandards);

    }

    public AggregatedTrecQrelGoldStandard(Collection<TrecQrelGoldStandard<Q>> goldStandards) {
        super(log, goldStandards.stream().map(gs -> (AtomicGoldStandard<Q>)gs).collect(Collectors.toList()));
    }

    @Override
    public void writeQrelFile(File qrelFile) {
        writeAggregatedQrelFile(qrelFile, goldStandards.values().toArray(new GoldStandard[0]), gs -> gs.getQrelDocuments(), qrelRecordFunction);
    }

    @Override
    public void writeQrelFile(File qrelFile, Collection<Q> queries) {
        writeAggregatedQrelFile(qrelFile, goldStandards.values().toArray(new GoldStandard[0]), gs -> gs.getQrelDocuments().getQuerySubset(queries), qrelRecordFunction);
    }

    @Override
    public void writeSampleQrelFile(File qrelFile) {
        if (!isSampleGoldStandard()) {
            throw new UnsupportedOperationException("This is not a sample gold standard.");
        }

        writeAggregatedQrelFile(qrelFile, goldStandards.values().toArray(new GoldStandard[0]), gs -> gs.getQrelDocuments(), sampleQrelRecordFunction);
    }

    @Override
    public void writeSampleQrelFile(File qrelFile, Collection<Q> queries) {
        if (!isSampleGoldStandard()) {
            throw new UnsupportedOperationException("This is not a sample gold standard.");
        }

        writeAggregatedQrelFile(qrelFile, goldStandards.values().toArray(new GoldStandard[0]), gs -> gs.getQrelDocuments().getQuerySubset(queries), sampleQrelRecordFunction);
    }

    /**
     * Aggregated gold standards created over a traditional qrel and a sample qrel (e.g. CT 2017 + CT 2018) get the
     * representation of the simplest GS.
     * @return
     */
    public boolean isSampleGoldStandard() {
        for (AtomicGoldStandard<Q> gs : goldStandards.values()) {
            if (!gs.isSampleGoldStandard()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Function<QueryDescription, String> getQueryIdFunction() {
        return CROSS_DATASET_QUERY_ID_FUNCTION;
    }

    /**
     * An aggregated gold standard is said official if, and only if, all gold standards are official.
     * @return
     */
    @Override
    public GoldStandardType getType() {
        for (AtomicGoldStandard<Q> gs : goldStandards.values()) {
            if (gs.getType() == GoldStandardType.INTERNAL) {
                return GoldStandardType.INTERNAL;
            }
        }
        return GoldStandardType.OFFICIAL;
    }
}
