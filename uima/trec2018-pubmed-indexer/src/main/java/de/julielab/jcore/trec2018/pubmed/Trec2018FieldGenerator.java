package de.julielab.jcore.trec2018.pubmed;

import de.julielab.jcore.consumer.es.ArrayFieldValue;
import de.julielab.jcore.consumer.es.FieldGenerationException;
import de.julielab.jcore.consumer.es.FieldGenerator;
import de.julielab.jcore.consumer.es.FilterRegistry;
import de.julielab.jcore.consumer.es.preanalyzed.Document;
import de.julielab.jcore.consumer.es.preanalyzed.RawToken;
import de.julielab.jcore.types.*;
import de.julielab.jcore.types.Date;
import de.julielab.jcore.utility.JCoReTools;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;

import java.util.*;
import java.util.List;

public class Trec2018FieldGenerator extends FieldGenerator {


    private static final Set<String> unwantedGsFields = new HashSet<>(Arrays.asList("unified_id", "title", "abstract", "major_mesh", "minor_mesh", "trec_doc_id",  "trec_topic_number", "trec_topic_other2", "trec_topic_other2", "trec_topic_other3"));

    public Trec2018FieldGenerator(FilterRegistry filterRegistry) {
        super(filterRegistry);
    }

    @Override
    public Document addFields(JCas jCas, Document document) throws CASException, FieldGenerationException {
        addDocId(jCas, document);
        addTitle(jCas, document);
        addAbstract(jCas, document);
        //addAbstractSections(jCas, document);
        addPublicationDate(jCas, document);
        addMeshTags(jCas, document);
        addDocumentSource(jCas, document);
        addGenes(jCas, document);
        addOrganisms(jCas, document);
        addDocumentClasses(jCas, document);
        addGsInfo(jCas, document);
        addPublicationType(jCas, document);
        return document;
    }

    private void addPublicationType(JCas jCas, Document document) {
        Header header = JCasUtil.selectSingle(jCas, Header.class);
        FSArray pubTypeList = header.getPubTypeList();
        ArrayFieldValue v = new ArrayFieldValue();
        if (pubTypeList != null) {
            for (int i = 0; i < pubTypeList.size(); ++i) {
                PubType pt = (PubType) pubTypeList.get(i);
                if (pt != null) {
                    String publicationTypeName = pt.getName();
                    v.add(new RawToken(publicationTypeName));
                }
            }
        }
        document.addField("publicationTypes", v);
    }

    private void addOrganisms(JCas jCas, Document document) {
        Collection<Organism> organisms = JCasUtil.select(jCas, Organism.class);
        ArrayFieldValue fieldValue = new ArrayFieldValue();
        for (Organism o : organisms) {
            FSArray resourceEntryList = o.getResourceEntryList();
            if (resourceEntryList != null) {
                for (int i = 0; i < resourceEntryList.size(); ++i) {
                    ResourceEntry entry = (ResourceEntry) resourceEntryList.get(i);
                    if (entry != null)
                        fieldValue.add(new RawToken(entry.getEntryId()));
                }
            }
        }
        document.addField("organisms", fieldValue);
    }

    private void addDocumentClasses(JCas jCas, Document document) {
        Optional<AutoDescriptor> any = JCasUtil.select(jCas, AutoDescriptor.class).stream().findAny();
        if (any.isPresent() && any.get().getDocumentClasses() != null) {
            ArrayFieldValue arrayFieldValue = new ArrayFieldValue();
            AutoDescriptor ad = any.get();
            for (int i = 0; i < ad.getDocumentClasses().size(); i++) {
                DocumentClass documentClass = ad.getDocumentClasses(i);
                arrayFieldValue.add(new RawToken(documentClass.getClassname()));
            }
            document.addField("documentClasses", arrayFieldValue);
        }
    }

    private void addGenes(JCas jCas, Document document) {
        Collection<Gene> genes = JCasUtil.select(jCas, Gene.class);
        ArrayFieldValue genesFieldValue = new ArrayFieldValue();
        for (Gene gene : genes) {
            genesFieldValue.add(new RawToken(gene.getCoveredText()));
        }
        document.addField("genes", genesFieldValue);
    }

    private void addGsInfo(JCas jCas, Document document) {
        Trec2018FilterBoard filterBoard = filterRegistry.getFilterBoard(Trec2018FilterBoard.class);
        Map<String, List<CSVRecord>> gsRecords = filterBoard.gsRecords;
        String docId = JCoReTools.getDocId(jCas);
        List<CSVRecord> records = gsRecords.get(docId);
        if (records != null) {
            for (CSVRecord record : records) {
                // Here we add all the GS information about the document. Note that this often includes multiple
                // records because documents are repeated across topics
                Map<String, Integer> gsHeaderMap = filterBoard.gsHeaderMap;

                // Here we add the GS information for each topic separately. By making this a sub-document,
                // the resulting field will be called 'topic_<topicNr>.<record header>' and thus make it possible
                // to query documents depending on their topic
                final Document gsInfoByTopic = new Document();
                for (String header : gsHeaderMap.keySet()) {
                    if (unwantedGsFields.contains(header) || StringUtils.isBlank(header))
                        continue;
                    gsInfoByTopic.addField(header, record.get(header));
                }
                document.addField("topic_" + record.get("trec_topic_number"), gsInfoByTopic);
            }
        }
    }


    private void addAbstractSections(JCas jCas, Document document) {
        Collection<AbstractSection> sections = JCasUtil.select(jCas, AbstractSection.class);
        if (!sections.isEmpty()) {
            for (AbstractSection section : sections) {
                AbstractSectionHeading heading = (AbstractSectionHeading) section.getAbstractSectionHeading();
                String nlmCategory = heading.getNlmCategory();
                document.addField("abstract_" + nlmCategory, section.getCoveredText());
            }
        }
    }


    private void addPublicationDate(JCas jCas, Document document) {
        Header header = JCasUtil.selectSingle(jCas, Header.class);
        PubType pubType = header.getPubTypeList(0);
        Date pubDate = pubType.getPubDate();
        document.addField("publicationDate", pubDate.getMonth() + " " + pubDate.getYear());
    }


    private void addMeshTags(JCas jCas, Document document) {
        Collection<MeshHeading> meshHeadings = JCasUtil.select(jCas, MeshHeading.class);
        ArrayFieldValue allHeadings = new ArrayFieldValue();
        ArrayFieldValue majorTopicHeadings = new ArrayFieldValue();
        ArrayFieldValue minorTopicHeadings = new ArrayFieldValue();
        for (MeshHeading heading : meshHeadings) {
            allHeadings.add(new RawToken(heading.getDescriptorName()));
            if (heading.getDescriptorNameMajorTopic()) {
                majorTopicHeadings.add(new RawToken(heading.getDescriptorName()));
            } else {
                minorTopicHeadings.add(new RawToken(heading.getDescriptorName()));
            }
        }
        document.addField("meshTags", allHeadings);
        document.addField("meshTagsMajor", majorTopicHeadings);
        document.addField("meshMinor", minorTopicHeadings);
    }


    private void addDocumentSource(JCas jCas, Document document) {
        String source = "PUBMED";
        if (document.getId().toUpperCase().contains("ASCO"))
            source = "ASCO";
        if (document.getId().toUpperCase().contains("AACR"))
            source = "AACR";
        document.addField("documentSource", source);
    }

    private void addDocId(JCas jCas, Document document) {
        String pmid = JCoReTools.getDocId(jCas);
        document.addField("pubmedId", pmid);
        document.setId(pmid);
    }


    private void addAbstract(JCas jCas, Document document) {
        Collection<AbstractText> titles = JCasUtil.select(jCas, AbstractText.class);
        titles.stream().findFirst().ifPresent(abstractText -> document.addField("abstract", abstractText.getCoveredText()));
    }

    private void addTitle(JCas jCas, Document document) {
        Collection<Title> titles = JCasUtil.select(jCas, Title.class);
        titles.stream().findFirst().ifPresent(title -> document.addField("title", title.getCoveredText()));
    }
}
