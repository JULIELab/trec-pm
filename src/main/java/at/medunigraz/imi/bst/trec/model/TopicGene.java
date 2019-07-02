package at.medunigraz.imi.bst.trec.model;

public class TopicGene {
    private String geneSymbol;
    private String mutation;

    @Override
    public String toString() {
        return geneSymbol + " (" + mutation + ")";
    }

    public String getGeneSymbol() {
        return geneSymbol;
    }

    public String getMutation() {
        return mutation;
    }

    public TopicGene(String geneSymbol, String mutation) {

        this.geneSymbol = geneSymbol;
        this.mutation = mutation;
    }
}
