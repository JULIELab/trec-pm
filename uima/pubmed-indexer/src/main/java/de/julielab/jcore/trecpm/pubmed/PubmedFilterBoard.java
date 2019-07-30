package de.julielab.jcore.trecpm.pubmed;

import de.julielab.jcore.consumer.es.ExternalResource;
import de.julielab.jcore.consumer.es.FilterBoard;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PubmedFilterBoard extends FilterBoard {

    @ExternalResource(key = "treatments", property = "cuisAndTextByPmid")
    public Map<String, List<Pair<String, String>>> cuisAndTextByPmid;
    @ExternalResource(key = "treatments", property = "focusedTreatmentCuis")
    public Set<String> focusedTreatmentCuis = new HashSet<>();
    @ExternalResource(key = "treatments", property = "broadTreatmentCuis")
    public Set<String> broadTreatmentCuis = new HashSet<>();

    @Override
    public void setupFilters() {
    }
}
