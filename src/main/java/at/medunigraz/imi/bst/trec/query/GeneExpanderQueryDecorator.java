package at.medunigraz.imi.bst.trec.query;

import at.medunigraz.imi.bst.retrieval.Query;
import at.medunigraz.imi.bst.retrieval.QueryDecorator;
import at.medunigraz.imi.bst.trec.expansion.NCBIGeneInfo;
import at.medunigraz.imi.bst.trec.model.Gene;
import at.medunigraz.imi.bst.trec.model.Result;
import at.medunigraz.imi.bst.trec.model.Topic;

import java.util.List;

public class GeneExpanderQueryDecorator extends QueryDecorator<Topic> {

	private static final String TOKEN_SEPARATOR = " ";

	private static final NCBIGeneInfo NCBI_GENE_INFO = NCBIGeneInfo.getInstance();

	private Gene.Field[] expandTo;

	public GeneExpanderQueryDecorator(Gene.Field[] expandTo, Query decoratedQuery) {
		super(decoratedQuery);
		this.expandTo = expandTo;
	}

	@Override
	public List<Result> query(Topic topic) {
		expandGenes(topic);
		return decoratedQuery.query(topic);
	}

	private void expandGenes(Topic topic) {
		String[] geneTokens = topic.getGeneFieldTokens();

		StringBuilder expandedGenes = new StringBuilder();
		for (String token : geneTokens) {
			if (NCBI_GENE_INFO.containsKey(token)) {
				Gene gene = NCBI_GENE_INFO.get(token);
				String expansion = gene.getExpandedField(TOKEN_SEPARATOR, expandTo);
				expandedGenes.append(expansion);
			} else {
				expandedGenes.append(token);
			}
			expandedGenes.append(TOKEN_SEPARATOR);
		}

		topic.withGeneField(expandedGenes.toString().trim());
	}
}
