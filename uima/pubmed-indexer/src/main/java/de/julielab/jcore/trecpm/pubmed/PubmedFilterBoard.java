package de.julielab.jcore.trecpm.pubmed;

import de.julielab.jcore.consumer.es.ExternalResource;
import de.julielab.jcore.consumer.es.FilterBoard;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class PubmedFilterBoard extends FilterBoard {

    @ExternalResource(key = "treatments", property = "cuisAndTextByPmid")
    public Map<String, List<Pair<String, String>>> cuisAndTextByPmid;
    @ExternalResource(key = "treatments", property = "focusedTreatmentCuis")
    public Set<String> focusedTreatmentCuis = new HashSet<>();
    @ExternalResource(key = "treatments", property = "broadTreatmentCuis")
    public Set<String> broadTreatmentCuis = new HashSet<>();
    @ExternalResource(key = "officialgs2017qrels")
    public List<String> officialgs2017;
    @ExternalResource(key = "officialgs2018qrels")
    public List<String> officialgs2018;
    @ExternalResource(key = "internalgs2019qrels")
    public List<String> internalgs2019;
    public Set<String> officalGs2017Docs;
    public Set<String> officalGs2018Docs;
    public Set<String> internalGs2019Docs;

    @Override
    public void setupFilters() {
        officalGs2017Docs = getDocIds(officialgs2017);
        officalGs2018Docs = getDocIds(officialgs2018);
        internalGs2019Docs = getDocIds(internalgs2019);
    }

    private Set<String> getDocIds(List<String> qrelLines) {
        return qrelLines.stream().map(l -> l.split("\\s+")[2]).collect(Collectors.toSet());
    }
}
