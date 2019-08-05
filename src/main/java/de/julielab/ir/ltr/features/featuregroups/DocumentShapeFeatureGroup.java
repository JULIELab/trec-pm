package de.julielab.ir.ltr.features.featuregroups;

import cc.mallet.types.Token;
import de.julielab.ir.ltr.Document;
import de.julielab.ir.ltr.features.FeatureGroup;

public class DocumentShapeFeatureGroup extends FeatureGroup {
    public static final String GROUP_NAME = "docshape";
    public static final String TITLE_LENGTH = "titlelength";
    public static final String ABSTRACT_LENGTH = "abstractlength";
    public DocumentShapeFeatureGroup() {
        super(GROUP_NAME);
    }

    @Override
    protected void addFeatures() {
        addFeature(TITLE_LENGTH, inst -> {
            Document doc = (Document) inst.getSource();
            final Token token = (Token) inst.getData();
            token.setFeatureValue(TITLE_LENGTH, doc.getTitelLength());
        });
        addFeature(ABSTRACT_LENGTH, inst -> {
            Document doc = (Document) inst.getSource();
            final Token token = (Token) inst.getData();
            token.setFeatureValue(ABSTRACT_LENGTH, doc.getAbstractLength());
        });
    }
}
