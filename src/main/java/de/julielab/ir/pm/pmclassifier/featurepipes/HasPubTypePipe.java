package de.julielab.ir.pm.pmclassifier.featurepipes;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import de.julielab.ir.pm.pmclassifier.Document;

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
