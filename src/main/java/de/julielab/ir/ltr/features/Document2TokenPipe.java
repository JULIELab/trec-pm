package de.julielab.ir.ltr.features;


import cc.mallet.pipe.Pipe;
import cc.mallet.types.*;
import de.julielab.ir.ltr.Document;

public class Document2TokenPipe extends Pipe {
    public Document2TokenPipe() {
        setTargetAlphabet(new LabelAlphabet());
    }

    public Document2TokenPipe(Alphabet targetAlphabet) {
        setTargetAlphabet(targetAlphabet);
    }

    @Override
    public Instance pipe(Instance inst) {
        Document doc = (Document) inst.getData();
        Token token = new Token(doc.getId());
        inst.setData(token);
        LabelAlphabet ta = (LabelAlphabet) getTargetAlphabet();
        Label label;
        synchronized (ta) {
            label = ta.lookupLabel(inst.getTarget());
        }
        inst.setTarget(label);
        return inst;
    }
}
