package at.medunigraz.imi.bst.trec.query;

import at.medunigraz.imi.bst.retrieval.Query;
import at.medunigraz.imi.bst.trec.model.Topic;

import java.util.regex.Pattern;

public class GeneFamilyQueryDecorator extends DynamicQueryDecorator {

    /**
     * Matches patterns for removal, normally including numbers. E.g.:
     * TP53 -> TP
     * CDK6 -> CDK
     * FGFR1 -> FGF
     * EGFR -> EGF
     * PIK3CA -> PIK
     * NF2 -> NF
     * CDKN2A -> CDKN
     * EML4 -> EML
     */
    private static final Pattern REMOVAL = Pattern.compile("([0-9]{1,2}[A-Z]{0,2}|R[0-9]{0,1})$");

    public GeneFamilyQueryDecorator(Query decoratedQuery) {
        super(decoratedQuery);
    }

    @Override
    public Topic expandTopic(Topic topic) {
        String[] geneTokens = topic.getGeneTokens();

        for (String token : geneTokens) {
            String family = REMOVAL.matcher(token).replaceAll("");
            if (!family.isEmpty() && !family.equals(token)) {
                topic.withGeneHypernym(family);
            }
        }
        return topic;
    }

}
