package de.julielab.jcore.trec2018.pubmed;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.uima.resource.SharedResourceObject;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IUmlsTreatmentsProvider extends SharedResourceObject {

    Map<String, List<Pair<String, String>>> getCuisAndTextByPmid();

    Set<String> getFocusedTreatmentCuis();

    Set<String> getBroadTreatmentCuis();
}
