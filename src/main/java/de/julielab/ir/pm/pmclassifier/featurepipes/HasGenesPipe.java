package de.julielab.ir.pm.pmclassifier.featurepipes;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import de.julielab.ir.pm.pmclassifier.Document;

public class HasGenesPipe extends Pipe {
    @Override
    public Instance pipe(Instance inst) {
        Document document = (Document) inst.getSource();
        Token token = (Token) inst.getData();
        if (document.getGenes() != null) {
            token.setFeatureValue("hasGenes", document.getGenes().size());
            for (String gene : document.getGenes())
                token.setFeatureValue("hasGene="+gene.toLowerCase(), 1);
        }
        return inst;
    }
}
