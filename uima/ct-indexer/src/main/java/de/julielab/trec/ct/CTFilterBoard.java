package de.julielab.trec.ct;

import de.julielab.jcore.consumer.es.ExternalResource;
import de.julielab.jcore.consumer.es.FilterBoard;

import java.util.Map;

public class CTFilterBoard extends FilterBoard {

    @ExternalResource(key = "gru2017")
    public Map<String, Double> gru2017;

    @ExternalResource(key = "gru2018")
    public Map<String, Double> gru2018;


    @Override
    public void setupFilters() {
    }
}
