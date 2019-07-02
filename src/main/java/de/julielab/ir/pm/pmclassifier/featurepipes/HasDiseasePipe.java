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

public class HasDiseasePipe extends Pipe {

    private AhoCorasickOptimized ac;

    public HasDiseasePipe() {
        try (BufferedReader readerFromFile = FileUtilities.getReaderFromFile(new File("src/main/resources/topics/diseases2018.lst"))) {
        ac = new AhoCorasickOptimized(readerFromFile.lines().collect(Collectors.toList()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Instance pipe(Instance inst) {
        Token token = (Token) inst.getData();
        ac.match(token.getText(), (start,end,matched) -> token.setFeatureValue("hasDisease="+matched, 1));
        return inst;
    }
}
