package de.julielab.ir.pm.pmclassifier.featurepipes;

import de.julielab.ir.pm.pmclassifier.AhoCorasickOptimized;
import de.julielab.ir.pm.pmclassifier.Document;
import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import de.julielab.java.utilities.FileUtilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class HasKeywordPipe extends Pipe {

    @Override
    public Instance pipe(Instance inst) {
        Token token = (Token) inst.getData();
        Document document = (Document) inst.getSource();
        if (document.getKeywords() != null) {
            for (String kw : document.getKeywords()) {
                token.setFeatureValue("hasKeyword="+kw, 1);
            }
        }
        return inst;
    }
}
