package de.julielab.ir.pm.pmclassifier.featurepipes;

import de.julielab.ir.pm.pmclassifier.AhoCorasickOptimized;
import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import de.julielab.java.utilities.FileUtilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;

public class HasPositiveBoostWordPipe extends Pipe {

    private AhoCorasickOptimized ac;

    public HasPositiveBoostWordPipe() {
        try (BufferedReader readerFromFile = FileUtilities.getReaderFromFile(new File("src/main/resources/keywords/top-keywords.txt"))) {
        ac = new AhoCorasickOptimized(readerFromFile.lines().collect(Collectors.toList()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Instance pipe(Instance inst) {
        Token token = (Token) inst.getData();
        ac.match(token.getText().toLowerCase(), (start,end,matched) -> token.setFeatureValue("hasPositiveBooster="+matched, 1));
        return inst;
    }
}
