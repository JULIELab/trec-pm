package de.julielab.ir.umls;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class UmlsSynset extends HashSet<String> {
    public static final UmlsSynset EMPTY = new UmlsSynset(Collections.emptySet(), "");
    private String cui;

    public UmlsSynset(Set<String> synonyms, String cui) {
        super(synonyms);
        this.cui = cui;
    }

    public String getCui() {
        return cui;
    }
}
