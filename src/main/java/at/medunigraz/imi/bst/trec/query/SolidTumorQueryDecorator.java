package at.medunigraz.imi.bst.trec.query;

import at.medunigraz.imi.bst.retrieval.Query;

import java.io.File;

public class SolidTumorQueryDecorator extends FileBasedQueryDecorator {

    private static final String SOLID ="/synonyms/solid.txt";

    public SolidTumorQueryDecorator(Query decoratedQuery) {
        super(SOLID, decoratedQuery);
    }
}
