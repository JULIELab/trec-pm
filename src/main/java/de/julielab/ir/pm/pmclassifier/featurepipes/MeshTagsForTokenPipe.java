package de.julielab.ir.pm.pmclassifier.featurepipes;


import de.julielab.ir.pm.pmclassifier.Document;
import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import cc.mallet.types.TokenSequence;

public class MeshTagsForTokenPipe extends Pipe {
    @Override
    public Instance pipe(Instance inst) {
        Document document = (Document) inst.getSource();
        if (document.getMeshTagsMajor() != null) {
            Token token = (Token) inst.getData();
            for (String tag : document.getMeshTagsMajor())
                token.setFeatureValue("hasMajorMesh=" + tag, 1);
        }
        return inst;
    }
}
