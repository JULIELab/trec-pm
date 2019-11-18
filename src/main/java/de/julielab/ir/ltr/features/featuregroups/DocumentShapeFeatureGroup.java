package de.julielab.ir.ltr.features.featuregroups;

import cc.mallet.types.Token;
import de.julielab.ir.ltr.Document;
import de.julielab.ir.ltr.features.FeatureGroup;

import java.util.List;

public class DocumentShapeFeatureGroup extends FeatureGroup {
    public static final String GROUP_NAME = "docshape";
    public static final String TITLE_LENGTH = "titlelength";
    public static final String ABSTRACT_LENGTH = "abstractlength";
    public static final String NUM_FOCUSED_TREATMENTS = "numfocusedtreatments";
    public static final String NUM_BROAD_TREATMENTS = "numbroadtreatments";
    public static final String FOCUSED_TREATMENTS = "focusedTreatmentText";
    public static final String BROAD_TREATMENTS = "broadTreatmentText";
    public DocumentShapeFeatureGroup() {
        super(GROUP_NAME);
    }

    @Override
    protected void addFeatures() {
        addFeature(TITLE_LENGTH, inst -> {
            Document doc = (Document) inst.getSource();
            final Token token = (Token) inst.getData();
            token.setFeatureValue(TITLE_LENGTH, doc.getTitleLength());
        });
        addFeature(ABSTRACT_LENGTH, inst -> {
            Document doc = (Document) inst.getSource();
            final Token token = (Token) inst.getData();
            token.setFeatureValue(ABSTRACT_LENGTH, doc.getAbstractLength());
        });
        addFeature(FOCUSED_TREATMENTS, inst -> {
            Document doc = (Document) inst.getSource();
            if (doc.getSourceFields() != null) {
                final List<String> treatments = (List<String>) doc.getSourceFields().get(FOCUSED_TREATMENTS);
                if (treatments != null) {
                    final Token token = (Token) inst.getData();
                    token.setFeatureValue(NUM_FOCUSED_TREATMENTS, treatments.size());
                    for (String treatment : treatments) {
                        token.setFeatureValue("focusedTreatment-" + treatment, 1);
                    }
                }
            }
        });
        addFeature(BROAD_TREATMENTS, inst -> {
            Document doc = (Document) inst.getSource();
            if (doc.getSourceFields() != null) {
                final List<String> treatments = (List<String>) doc.getSourceFields().get(BROAD_TREATMENTS);
                if (treatments != null) {
                    final Token token = (Token) inst.getData();
                    token.setFeatureValue(NUM_BROAD_TREATMENTS, treatments.size());
                    for (String treatment : treatments) {
                        token.setFeatureValue("broadTreatment-" + treatment, 1);
                    }
                }
            }
        });
    }
}
