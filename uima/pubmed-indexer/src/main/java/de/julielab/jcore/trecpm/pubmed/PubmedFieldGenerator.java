package de.julielab.jcore.trecpm.pubmed;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import de.julielab.jcore.consumer.es.ArrayFieldValue;
import de.julielab.jcore.consumer.es.FieldGenerationException;
import de.julielab.jcore.consumer.es.FieldGenerator;
import de.julielab.jcore.consumer.es.FilterRegistry;
import de.julielab.jcore.consumer.es.filter.SnowballFilter;
import de.julielab.jcore.consumer.es.preanalyzed.Document;
import de.julielab.jcore.consumer.es.preanalyzed.RawToken;
import de.julielab.jcore.types.*;
import de.julielab.jcore.types.Date;
import de.julielab.jcore.utility.JCoReTools;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class PubmedFieldGenerator extends FieldGenerator {

    private final static Logger log = LoggerFactory.getLogger(PubmedFieldGenerator.class);
    private static Set<String> negativeBoosters = new HashSet<>(Arrays.asList("dna", "tumor", "cell", "mouse", "model", "tissue", "development", "specific", "staining", "pathogenesis", "case"));
    private SnowballFilter sb = new SnowballFilter();

    public PubmedFieldGenerator(FilterRegistry filterRegistry) {
        super(filterRegistry);
    }

    @Override
    public Document addFields(JCas jCas, Document document) {
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
        addPublicationType(jCas, document);
        addNegationScopes(jCas, document);
        addMutations(jCas, document);
        addKeywords(jCas, document);
        addTreatments(document);
        addFilteredTreatments(document);
        addNegativeKeywordCounts(jCas, document);
        addWhetherPartOfGoldStandard(document);
        return document;
    }

    private void addWhetherPartOfGoldStandard(Document document) {
        if (filterRegistry != null) {
            final PubmedFilterBoard fb = filterRegistry.getFilterBoard(PubmedFilterBoard.class);
            String docId = document.getId();
            if (fb.officalGs2017Docs.contains(docId))
                document.addField("inOfficial2017gs", new RawToken("true"));
            if (fb.officalGs2018Docs.contains(docId))
                document.addField("inOfficial2018gs", new RawToken("true"));
            if (fb.internalGs2019Docs.contains(docId))
                document.addField("inInternal2019gs", new RawToken("true"));
        }
    }

    private void addNegativeKeywordCounts(JCas jCas, Document document) {
        Multiset<String> tokens = HashMultiset.create();
        for (Token t : jCas.<Token>getAnnotationIndex(Token.type)) {
            final String token = sb.filter(t.getCoveredText().toLowerCase()).get(0);
            if (negativeBoosters.contains(token))
                tokens.add(token);
        }
        document.addField("negativeBoosters", tokens.elementSet().stream().map(RawToken::new).collect(Collectors.toList()));
        document.addField("numNegativeBoosters", tokens.elementSet().stream().mapToInt(tokens::count).sum());
        document.addField("numUniqueNegativeBoosters", tokens.elementSet().size());
    }

    private void addTreatments(Document document) {
        if (filterRegistry != null) {
            final String docId = document.getId();
            final PubmedFilterBoard fb = filterRegistry.getFilterBoard(PubmedFilterBoard.class);
            ArrayFieldValue focusedTreatmentCuis = new ArrayFieldValue();
            ArrayFieldValue focusedTreatmentText = new ArrayFieldValue();
            ArrayFieldValue broadTreatmentCuis = new ArrayFieldValue();
            ArrayFieldValue broadTreatmentText = new ArrayFieldValue();
            if (fb.cuisAndTextByPmid.containsKey(docId)) {
                final List<Pair<String, String>> cuisAndText = fb.cuisAndTextByPmid.get(docId);
                for (Pair<String, String> cuiAndText : cuisAndText) {
                    String cui = cuiAndText.getLeft();
                    String text = cuiAndText.getRight();
                    if (fb.focusedTreatmentCuis.contains(cui)) {
                        focusedTreatmentCuis.add(new RawToken(cui));
                        focusedTreatmentText.add(new RawToken(text));
                    }
                    if (fb.broadTreatmentCuis.contains(cui)) {
                        broadTreatmentCuis.add(new RawToken(cui));
                        broadTreatmentText.add(new RawToken(text));
                    }
                }
            }
            document.addField("focusedTreatmentCuis", focusedTreatmentCuis);
            document.addField("focusedTreatmentText", focusedTreatmentText);
            document.addField("broadTreatmentCuis", broadTreatmentCuis);
            document.addField("broadTreatmentText", broadTreatmentText);
            document.addField("numFocusedTreatments", focusedTreatmentCuis.size());
            document.addField("numBroadTreatments", broadTreatmentCuis.size());
            document.addField("numUniqueFocusedTreatments", focusedTreatmentCuis.stream().map(RawToken.class::cast).map(RawToken::getTokenValue).distinct().count());
            document.addField("numUniqueBroadTreatments", broadTreatmentCuis.stream().map(RawToken.class::cast).map(RawToken::getTokenValue).distinct().count());
        }
    }

    private void addFilteredTreatments(Document document) {
        if (filterRegistry != null) {
            final String docId = document.getId();
            final PubmedFilterBoard fb = filterRegistry.getFilterBoard(PubmedFilterBoard.class);
            ArrayFieldValue focusedTreatmentCuis = new ArrayFieldValue();
            ArrayFieldValue focusedTreatmentText = new ArrayFieldValue();
            ArrayFieldValue broadTreatmentCuis = new ArrayFieldValue();
            ArrayFieldValue broadTreatmentText = new ArrayFieldValue();
            if (fb.cuisAndTextByPmid.containsKey(docId)) {
                final List<Pair<String, String>> cuisAndText = fb.cuisAndTextByPmid.get(docId);
                for (Pair<String, String> cuiAndText : cuisAndText) {
                    String cui = cuiAndText.getLeft();
                    String text = cuiAndText.getRight();
                    if (!fb.negativeTreatments.contains(text)) {
                        if (fb.focusedTreatmentCuis.contains(cui)) {
                            focusedTreatmentCuis.add(new RawToken(cui));
                            focusedTreatmentText.add(new RawToken(text));
                        }
                        if (fb.broadTreatmentCuis.contains(cui)) {
                            broadTreatmentCuis.add(new RawToken(cui));
                            broadTreatmentText.add(new RawToken(text));
                        }
                    }
                }
            }
            document.addField("filteredFocusedTreatmentCuis", focusedTreatmentCuis);
            document.addField("filteredFocusedTreatmentText", focusedTreatmentText);
            document.addField("filteredBroadTreatmentCuis", broadTreatmentCuis);
            document.addField("filteredBroadTreatmentText", broadTreatmentText);
            document.addField("filteredNumFocusedTreatments", focusedTreatmentCuis.size());
            document.addField("filteredNumBroadTreatments", broadTreatmentCuis.size());
            document.addField("filteredNumUniqueFocusedTreatments", focusedTreatmentCuis.stream().map(RawToken.class::cast).map(RawToken::getTokenValue).distinct().count());
            document.addField("filteredNumUniqueBroadTreatments", broadTreatmentCuis.stream().map(RawToken.class::cast).map(RawToken::getTokenValue).distinct().count());
        }
    }

    private void addKeywords(JCas jCas, Document document) {
        try {
            final de.julielab.jcore.types.pubmed.ManualDescriptor md = JCasUtil.selectSingle(jCas, de.julielab.jcore.types.pubmed.ManualDescriptor.class);
            final ArrayFieldValue keywords = new ArrayFieldValue();
            if (md.getKeywordList() != null) {
                for (FeatureStructure fs : md.getKeywordList()) {
                    Keyword kw = (Keyword) fs;
                    keywords.add(new RawToken(kw.getName()));
                }
            }
            document.addField("keyword", keywords);
        } catch (IllegalArgumentException e) {
            log.debug("Not adding keywords because the document {} does not have a ManualDescriptor.", document.getId());
        }
    }

    private void addNegationScopes(JCas jCas, Document document) {
        Collection<Scope> negationScopes = JCasUtil.select(jCas, Scope.class);
        ArrayFieldValue fieldValue = new ArrayFieldValue();
        for (Scope scope : negationScopes) {
            fieldValue.add(new RawToken(scope.getCoveredText()));
        }
        document.addField("negationPhrases", fieldValue);
    }

    private void addMutations(JCas jCas, Document document) {
        Collection<PointMutation> mutations = JCasUtil.select(jCas, PointMutation.class);
        ArrayFieldValue fieldValue = new ArrayFieldValue();
        for (PointMutation mutation : mutations) {
            fieldValue.add(new RawToken(mutation.getSpecificType()));
        }
        document.addField("mutations", fieldValue);
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
            AutoDescriptor ad = any.get();
            if (ad.getDocumentClasses().size() != 2)
                throw new IllegalStateException("This field generator expects two document classes given from PM classifiers. The first should be from the classifier trained on the TREC PM 2017, the second trained on 2018.");
            DocumentClass dc2017 = ad.getDocumentClasses(0);
            DocumentClass dc2018 = ad.getDocumentClasses(1);
            document.addField("pmclass2017", dc2017.getClassname());
            document.addField("pmclass2017confidence", dc2017.getConfidence());

            document.addField("pmclass2018", dc2018.getClassname());
            document.addField("pmclass2018confidence", dc2018.getConfidence());
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
        if (header.getPubTypeList() != null) {
            PubType pubType = header.getPubTypeList(0);
            Date pubDate = pubType.getPubDate();
            document.addField("publicationDate", pubDate.getMonth() + " " + pubDate.getYear());
        }
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
