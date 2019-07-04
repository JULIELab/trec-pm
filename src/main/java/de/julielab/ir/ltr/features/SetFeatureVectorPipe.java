package de.julielab.ir.ltr.features;


import cc.mallet.pipe.Pipe;
import cc.mallet.types.AugmentableFeatureVector;
import cc.mallet.types.FeatureVector;
import cc.mallet.types.Instance;
import de.julielab.ir.ltr.Document;

/**
 * This pipe does not add any features of itself. I just takes the feature vector from the documents
 * and calls the {@link AugmentableFeatureVector#numLocations()} method which causes the vector to be sorted
 * and organized into its final form. One should not read from it before that because it would only contain the
 * raw feature entries unconsolidated. That means that multiple additions of the same feature are not summed up
 * and that the feature indices are not sorted but just stand in the order they were added.
 */
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
