package de.julielab.ir.ltr.features;


import cc.mallet.pipe.Pipe;
import cc.mallet.types.FeatureVector;
import cc.mallet.types.Instance;
import de.julielab.ir.ltr.Document;

public class SetFeatureVectorPipe extends Pipe {
    @Override
    public Instance pipe(Instance inst) {
        final FeatureVector fv = (FeatureVector) inst.getData();
        // This call causes the AugmentableFeatureVector to sort its indices; it is not
        // really in order before that. Perhaps we don't even use that subclass (by specifying
        // augmentable: false in the Token2FeatureVector pipe constructor) but better to be on the safe side.
        fv.numLocations();
        final Document document = (Document) inst.getSource();
        document.setFeatureVector(fv);
        return inst;
    }
}
