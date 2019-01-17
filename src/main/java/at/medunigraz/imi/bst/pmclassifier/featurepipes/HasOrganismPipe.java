package at.medunigraz.imi.bst.pmclassifier.featurepipes;

import at.medunigraz.imi.bst.pmclassifier.AhoCorasickOptimized;
import at.medunigraz.imi.bst.pmclassifier.Document;
import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import de.julielab.java.utilities.FileUtilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;

public class HasOrganismPipe extends Pipe {

    @Override
    public Instance pipe(Instance inst) {
        Document document = (Document) inst.getSource();
        Token token = (Token) inst.getData();
        if (document.getOrganisms() != null) {
            for (String gene : document.getOrganisms())
                token.setFeatureValue("hasOrganism="+gene.toLowerCase(), 1);
        }
        return inst;
    }
}
