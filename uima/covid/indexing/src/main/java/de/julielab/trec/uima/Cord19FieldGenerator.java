package de.julielab.trec.uima;

import de.julielab.jcore.consumer.es.FieldGenerationException;
import de.julielab.jcore.consumer.es.FieldGenerator;
import de.julielab.jcore.consumer.es.FilterRegistry;
import de.julielab.jcore.consumer.es.preanalyzed.Document;
import de.julielab.jcore.types.Caption;
import de.julielab.jcore.types.Section;
import de.julielab.jcore.types.Title;
import de.julielab.jcore.types.pubmed.AbstractText;
import de.julielab.jcore.types.pubmed.Header;
import de.julielab.jcore.types.pubmed.OtherID;
import org.apache.commons.lang.StringUtils;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Cord19FieldGenerator extends FieldGenerator {
    private final static Logger log = LoggerFactory.getLogger(Cord19FieldGenerator.class);

    public Cord19FieldGenerator(FilterRegistry filterRegistry) {
        super(filterRegistry);
    }

    @Override
    public Document addFields(JCas aJCas, Document doc) throws CASException, FieldGenerationException {
        addDocumentIds(aJCas, doc);
        addTitle(aJCas, doc);
        addAbstract(aJCas, doc);
        addBody(aJCas, doc);
        addCaptions(aJCas, doc);
        return doc;
    }

    private void addCaptions(JCas aJCas, Document doc) {
        AnnotationIndex<Caption> captionIndex = aJCas.getAnnotationIndex(Caption.type);
        for (Caption c : captionIndex) {
            String captionType = c.getCaptionType();
            if (captionType == null)
                captionType = "none";
            doc.addField("caption_" + captionType, c.getCoveredText());
        }
    }

    private void addBody(JCas aJCas, Document doc) {
        AnnotationIndex<Section> sectionIndex = aJCas.getAnnotationIndex(Section.type);
        for (Section s : sectionIndex) {
            Title sectionHeading = s.getSectionHeading();
            doc.addField("section_title", sectionHeading.getCoveredText());
            doc.addField("section_text", s.getCoveredText());
        }
    }

    private void addAbstract(JCas aJCas, Document doc) {
        AnnotationIndex<AbstractText> abstractIndex = aJCas.getAnnotationIndex(AbstractText.type);
        int i = 0;
        for (AbstractText at : abstractIndex) {
            doc.addField("abstract", at.getCoveredText());
            ++i;
        }
        if (i != 1) {
            log.warn("There were {} abstracts in document {}", i, doc.getId());
        }
    }

    private void addTitle(JCas aJCas, Document doc) {
        AnnotationIndex<Title> titleIndex = aJCas.getAnnotationIndex(Title.type);
        for (Title t : titleIndex) {
            String titleType = t.getTitleType();
            if (titleType != null && titleType.equals("document")) {
                doc.addField("title", t.getCoveredText());
            }
        }
    }

    private void addDocumentIds(JCas aJCas, Document doc) {
        Header h = JCasUtil.selectSingle(aJCas, Header.class);
        doc.addField("paper_id", h.getDocId());
        doc.setId(h.getDocId());
        FSArray otherIDs = h.getOtherIDs();
        if (otherIDs != null) {
            for (FeatureStructure fs : otherIDs) {
                OtherID otherID = (OtherID) fs;
                String source = otherID.getSource().toLowerCase().replaceAll("-|\\s+", "_");
                String id = otherID.getId();
                if (!StringUtils.isBlank(id))
                    doc.addField(source, id);
            }
        }
    }
}
