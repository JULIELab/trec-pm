package de.julielab.trec.ct;

import de.julielab.jcore.consumer.es.FieldGenerationException;
import de.julielab.jcore.consumer.es.FieldGenerator;
import de.julielab.jcore.consumer.es.FilterRegistry;
import de.julielab.jcore.consumer.es.preanalyzed.Document;
import de.julielab.jcore.types.AutoDescriptor;
import de.julielab.jcore.types.DocumentClass;
import de.julielab.jcore.types.Keyword;
import de.julielab.jcore.types.MeshHeading;
import de.julielab.jcore.types.ct.*;
import de.julielab.jcore.types.pubmed.ManualDescriptor;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectSingle;

public class CTFieldGenerator extends FieldGenerator {

    public CTFieldGenerator(FilterRegistry filterRegistry) {
        super(filterRegistry);
    }

    @Override
    public Document addFields(JCas jCas, Document document) {
        final Header header = selectSingle(jCas, Header.class);
        try {
            final BriefTitle briefTitle = selectSingle(jCas, BriefTitle.class);
            final OfficialTitle officialTitle = selectSingle(jCas, OfficialTitle.class);
            final Summary summaryAnno = selectSingle(jCas, Summary.class);
            final Description descriptionAnno = selectSingle(jCas, Description.class);
            final Collection<OutcomeMeasure> outcomeMeasureAnnos = select(jCas, OutcomeMeasure.class);
            final Collection<OutcomeDescription> outcomeDescriptionAnnos = select(jCas, OutcomeDescription.class);
            final Collection<Condition> conditionAnnos = select(jCas, Condition.class);
            final Collection<InterventionType> interventionTypeAnnos = select(jCas, InterventionType.class);
            final Collection<InterventionName> interventionNameAnnos = select(jCas, InterventionName.class);
            final Collection<ArmGroupDescription> armGroupDescriptionAnnos = select(jCas, ArmGroupDescription.class);
            final Inclusion inclusionAnno = selectSingle(jCas, Inclusion.class);
            final Exclusion exclusionAnno = selectSingle(jCas, Exclusion.class);
            ManualDescriptor md = null;
            if (!select(jCas, ManualDescriptor.class).isEmpty())
                md = selectSingle(jCas, ManualDescriptor.class);


            final String id = header.getDocId();
            final String brief_title = briefTitle.getCoveredText();
            final String official_title = officialTitle.getCoveredText();
            final String summary = summaryAnno.getCoveredText();
            final String description = descriptionAnno.getCoveredText();
            final String studyType = header.getStudyType();
            final String interventionModel = header.getStudyDesignInfo().getInterventionModel();
            final String primary_purpose = header.getStudyDesignInfo().getPrimaryPurpose();
            final List<String> outcomeMeasures = annos2CoveredStringList(outcomeMeasureAnnos);
            final List<String> outcomeDescriptions = annos2CoveredStringList(outcomeDescriptionAnnos);
            final List<String> conditions = annos2CoveredStringList(conditionAnnos);
            final List<String> interventionTypes = annos2CoveredStringList(interventionTypeAnnos);
            final List<String> interventionNames = annos2CoveredStringList(interventionNameAnnos);
            final List<String> armGroupDescriptions = annos2CoveredStringList(armGroupDescriptionAnnos);
            final String[] sex = header.getGender() != null ? header.getGender().toStringArray() : null;
            final int minimum_age = header.getMinimumAge();
            final int maximum_age = header.getMaximumAge();
            final String inclusion = inclusionAnno.getCoveredText();
            final String exclusion = exclusionAnno.getCoveredText();
            final List<String> keywords = md != null && md.getKeywordList() != null  ? Stream.of(md.getKeywordList().toArray()).map(Keyword.class::cast).map(Keyword::getName).collect(Collectors.toList()) : null;
            final List<String> meshTags = md != null && md.getMeSHList() != null     ? Stream.of(md.getMeSHList().toArray()).map(MeshHeading.class::cast).map(MeshHeading::getDescriptorName).collect(Collectors.toList()) : null;

            document.setId(id);
            document.addField("id", id);
            document.addField("brief_title", brief_title);
            document.addField("official_title", official_title);
            document.addField("summary", summary);
            document.addField("description", description);
            document.addField("studyType", studyType);
            document.addField("interventionModel", interventionModel);
            document.addField("primary_purpose", primary_purpose);
            document.addField("outcomeMeasures", outcomeMeasures);
            document.addField("outcomeDescriptions", outcomeDescriptions);
            document.addField("conditions", conditions);
            document.addField("interventionTypes", interventionTypes);
            document.addField("interventionNames", interventionNames);
            document.addField("armGroupDescriptions", armGroupDescriptions);
            document.addField("sex", sex);
            document.addField("minimum_age", minimum_age);
            document.addField("maximum_age", maximum_age);
            document.addField("inclusion", inclusion);
            document.addField("exclusion", exclusion);
            document.addField("keywords", keywords);
            document.addField("meshTags", meshTags);

            addDocumentClasses(jCas, document);
        } catch (Throwable t) {
            log.error("Error while indexing document {}", header.getDocId(), t);
            throw t;
        }
        return document;
    }

    private List<String> annos2CoveredStringList(Collection<? extends Annotation> annos) {
        List<String> ret = new ArrayList<>();
        for (Annotation a : annos) {
            ret.add(a.getCoveredText());
        }
        return ret;
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
}
