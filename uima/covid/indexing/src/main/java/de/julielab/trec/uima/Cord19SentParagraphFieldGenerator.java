package de.julielab.trec.uima;

import de.julielab.jcore.consumer.es.DocumentGenerator;
import de.julielab.jcore.consumer.es.FilterRegistry;
import de.julielab.jcore.consumer.es.preanalyzed.Document;
import de.julielab.jcore.types.Caption;
import de.julielab.jcore.types.Paragraph;
import de.julielab.jcore.types.Title;
import de.julielab.jcore.types.Token;
import de.julielab.jcore.types.pubmed.AbstractText;
import de.julielab.jcore.types.pubmed.Header;
import de.julielab.jcore.types.pubmed.OtherID;
import org.apache.commons.lang.StringUtils;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Cord19SentParagraphFieldGenerator extends DocumentGenerator {
    private final static Logger log = LoggerFactory.getLogger(Cord19SentParagraphFieldGenerator.class);
    private static final boolean lemmatize = true;
    private static final String BASE_INDEX_NAME = "covid-rnd5-";

    public Cord19SentParagraphFieldGenerator(FilterRegistry filterRegistry) {
        super(filterRegistry);
    }

    @Override
    public List<Document> createDocuments(JCas jCas) {
        List<Document> docs = new ArrayList<>();
        //docs.addAll(indexStructure(jCas, jCas.getTypeSystem().getType(AbstractText.class.getCanonicalName())));
        docs.addAll(indexStructure(jCas, jCas.getTypeSystem().getType(Paragraph.class.getCanonicalName())));
//        docs.addAll(indexStructure(jCas, jCas.getTypeSystem().getType(Caption.class.getCanonicalName())));
        //docs.addAll(indexStructure(jCas, jCas.getTypeSystem().getType(Title.class.getCanonicalName())));
//        docs.addAll(indexStructure(jCas, jCas.getTypeSystem().getType(Sentence.class.getCanonicalName())));
        docs.add(indexParagraphTitleAbstract(jCas));
        return docs;
    }

    private Document indexParagraphTitleAbstract(JCas jCas) {
        Document doc = new Document();
        Type paragraphType = jCas.getTypeSystem().getType(Paragraph.class.getCanonicalName());
        String indexName = BASE_INDEX_NAME + paragraphType.getShortName().toLowerCase();
        doc.setIndex(indexName);
        addTitle(jCas, doc);
        addAbstract(jCas, doc);
        addDocumentIds(jCas, doc, 999999, paragraphType);
        return doc;
    }

    private List<Document> indexStructure(JCas jCas, Type type) {
        List<Document> docs = new ArrayList<>();
        int counter = 0;
        for (Annotation a : jCas.getAnnotationIndex(type)) {
            Document doc = new Document();
            docs.add(doc);
            doc.setIndex(BASE_INDEX_NAME + type.getShortName().toLowerCase());
            doc.addField("text", a.getCoveredText());
            if (lemmatize) {
                FSIterator<Token> tokenIt = jCas.<Token>getAnnotationIndex(Token.type).subiterator(a);
                StringBuilder sb = new StringBuilder();
                while (tokenIt.hasNext()) {
                    Token t = tokenIt.next();
                    sb.append(t.getLemma().getValue()).append(" ");
                }
                doc.addField("text-lemmas", sb.toString());
            }
            Title heading = null;
            if (a instanceof Caption) {
                Caption c = (Caption) a;
                heading = c.getCaptionTitle();
            }
            if (heading != null) {
                doc.addField("heading", heading.getCoveredText());
            }
            addDocumentIds(jCas, doc, counter, type);
            if (type.getShortName().equals("Paragraph")) {
                addTitle(jCas, doc);
                addAbstract(jCas, doc);
            }
            ++counter;
        }
        return docs;
    }

    private void addAbstract(JCas aJCas, Document doc) {
        AnnotationIndex<AbstractText> abstractIndex = aJCas.getAnnotationIndex(AbstractText.type);
        for (AbstractText at : abstractIndex) {
            doc.addField("abstract", at.getCoveredText());
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

    private void addDocumentIds(JCas aJCas, Document doc, int counter, Type type) {
        Header h = JCasUtil.selectSingle(aJCas, Header.class);
        doc.addField("paper_id", h.getDocId());
        doc.setId(h.getDocId() + "_" + type.getShortName().toLowerCase() + "_" + counter);
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
