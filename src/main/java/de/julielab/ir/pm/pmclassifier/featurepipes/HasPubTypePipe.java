package de.julielab.ir.pm.pmclassifier.featurepipes;

import de.julielab.ir.pm.pmclassifier.Document;
import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.Token;

public class HasPubTypePipe extends Pipe {

    @Override
    public Instance pipe(Instance inst) {
        Token token = (Token) inst.getData();
        Document document = (Document) inst.getSource();
        if (document.getPublicationTypes() != null) {
            for (String pt : document.getPublicationTypes()) {
                token.setFeatureValue("hasPubType="+pt, 1);
            }
        }
        return inst;
    }
}
