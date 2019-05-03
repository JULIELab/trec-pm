package at.medunigraz.imi.bst.trec.query;

import at.medunigraz.imi.bst.retrieval.Query;
import at.medunigraz.imi.bst.trec.expansion.NCBIGeneInfo;
import at.medunigraz.imi.bst.trec.model.Topic;

import java.util.List;

public class GeneSynonymQueryDecorator extends DynamicQueryDecorator {

    private static final NCBIGeneInfo NCBI_GENE_INFO = NCBIGeneInfo.getInstance();

    public GeneSynonymQueryDecorator(Query decoratedQuery) {
        super(decoratedQuery);
    }

    @Override
    public Topic expandTopic(Topic topic) {
        String[] geneTokens = topic.getGeneTokens();

        for (String token : geneTokens) {
            List<String> synonyms = NCBI_GENE_INFO.getSynonyms(token);
            for (String synonym : synonyms) {
                // TODO reintroduce variant
                topic.withGeneSynonym(synonym);
            }
        }
        return topic;
    }

}
