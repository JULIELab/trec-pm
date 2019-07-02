package at.medunigraz.imi.bst.trec.query;

import at.medunigraz.imi.bst.retrieval.Query;
import at.medunigraz.imi.bst.trec.expansion.NCBIGeneInfo;
import at.medunigraz.imi.bst.trec.model.Topic;

import java.util.Optional;

public class GeneDescriptionQueryDecorator extends DynamicQueryDecorator {

    private static final NCBIGeneInfo NCBI_GENE_INFO = NCBIGeneInfo.getInstance();

    public GeneDescriptionQueryDecorator(Query decoratedQuery) {
        super(decoratedQuery);
    }

    @Override
    public Topic expandTopic(Topic topic) {
        String[] geneTokens = topic.getGeneFieldTokens();

        for (String token : geneTokens) {
            Optional<String> description = NCBI_GENE_INFO.getDescription(token);
            description.ifPresent(d -> topic.withGeneDescription(d));
        }
        return topic;
    }

}
