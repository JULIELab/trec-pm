package de.julielab.ir.model;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.List;
import java.util.Set;

public class CovidTopic extends QueryDescription {
    @QueryDescriptionAttribute
    private String query;
    @QueryDescriptionAttribute
    private String question;
    @QueryDescriptionAttribute
    private String narrative;
    /**
     * <p>
     * This field should contain parts of the query that pose alternatives to each other. They will be used in a
     * dismax query. This causes documents to be checked for the best match of one these words instead of
     * matches for all of these words while summing the scores for each alternative appearance.
     * </p>
     * <p>
     * Each element of this list represents one set of alternatives.
     * </p>
     */
    @QueryDescriptionAttribute
    private List<Set<String>> mandatorySynonymWords;
    /**
     * This field should contain those parts of the query that are not reflected in {@link #mandatorySynonymWords}.
     * Thus, the bulk of the query terms will go into this field.
     */
    @QueryDescriptionAttribute
    private Set<String> mandatoryBoW;
    /**
     * @deprecated This is just a hack - handle collections correctly
     */
    @QueryDescriptionAttribute
    private String mandatoryBoWString;
    /**
     * This field is analogous to {@link #mandatorySynonymWords} but will be searched as optional relevance boosters.
     * Thus, documents can still be returned as a hit when they do not contain any of the words herein.
     */
    @QueryDescriptionAttribute
    private List<Set<String>> optionalSynonymWords;
    /**
     * This field is analogous to {@link #mandatoryBoW} but will be searched as optional relevance boosters.
     * Thus, documents can still be returned as a hit when they do not contain any of the words herein.
     */
    @QueryDescriptionAttribute
    private Set<String> optionalBoW;

    /**
     * Builds a Topic out of a XML file in the format:
     *
     * <pre>
     * {@code
     *  <topic number="3">
     *     <query>coronavirus immunity</query>
     *     <question>will SARS-CoV2 infected people develop immunity? Is cross protection possible?</question>
     *     <narrative>seeking studies of immunity developed due to infection with SARS-CoV2 or cross protection gained due to infection with other coronavirus types</narrative>
     *   </topic>
     * }
     * </pre>
     *
     * @param xmlFile
     * @return
     */
    public static CovidTopic fromXML(File xmlFile) {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

        Document doc;
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            doc = documentBuilder.parse(xmlFile);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        Element element = (Element) doc.getElementsByTagName("topic").item(0);

        return fromElement(element);
    }

    public static CovidTopic fromElement(Element element) {
        int number = Integer.parseInt(getAttribute(element, "number"));
        String query = getElement(element, "query");
        String question = getElement(element, "question");
        String narrative = getElement(element, "narrative");

        CovidTopic topic = new CovidTopic().withNumber(number).withQuery(query).withQuestion(question).withNarrative(narrative);

        return topic;
    }

    private static String getElement(Element element, String name) {
        return element.getElementsByTagName(name).item(0).getTextContent();
    }

    private static String getAttribute(Element element, String name) {
        return element.getAttribute(name);
    }

    public List<Set<String>> getMandatorySynonymWords() {
        return mandatorySynonymWords;
    }

    public void setMandatorySynonymWords(List<Set<String>> mandatorySynonymWords) {
        this.mandatorySynonymWords = mandatorySynonymWords;
    }

    public List<Set<String>> getOptionalSynonymWords() {
        return optionalSynonymWords;
    }

    public void setOptionalSynonymWords(List<Set<String>> optionalSynonymWords) {
        this.optionalSynonymWords = optionalSynonymWords;
    }

    public Set<String> getMandatoryBoW() {
        return mandatoryBoW;
    }

    public void setMandatoryBoW(Set<String> mandatoryBoW) {
        this.mandatoryBoW = mandatoryBoW;
        mandatoryBoWString = String.join(" ", mandatoryBoW);
    }

    public Set<String> getOptionalBoW() {
        return optionalBoW;
    }

    public void setOptionalBoW(Set<String> optionalBoW) {
        this.optionalBoW = optionalBoW;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getNarrative() {
        return narrative;
    }

    public void setNarrative(String narrative) {
        this.narrative = narrative;
    }

    @Override
    public CovidTopic getCleanCopy() {
        return new CovidTopic().withNumber(number).withQuery(query).withQuestion(question).withNarrative(narrative);
    }

    public CovidTopic withNarrative(String narrative) {
        this.narrative = narrative;
        return this;
    }

    public CovidTopic withQuestion(String question) {
        this.question = question;
        return this;
    }

    public CovidTopic withQuery(String query) {
        this.query = query;
        return this;
    }

    public CovidTopic withNumber(int number) {
        this.withNumber(number);
        return this;
    }
}
