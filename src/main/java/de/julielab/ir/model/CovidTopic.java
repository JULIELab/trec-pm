package de.julielab.ir.model;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class CovidTopic extends QueryDescription {
    @QueryDescriptionAttribute
    private String query;
    @QueryDescriptionAttribute
    private String question;
    @QueryDescriptionAttribute
    private String narrative;

    /**
     * Builds a Topic out of a XML file in the format:
     *
     * <pre>
     * {@code
     * <topic number="1">
     *     <disease>Acute lymphoblastic leukemia</disease>
     *     <gene>ABL1, PTPN11</gene>
     *     <demographic>12-year-old male</demographic>
     *     <other>No relevant factors</other>
     * </topic>
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

    public void setQuery(String query) {
        this.query = query;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public void setNarrative(String narrative) {
        this.narrative = narrative;
    }

    @Override
    public <Q extends QueryDescription> Q getCleanCopy() {
        return null;
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
        setNumber(number);
        return this;
    }
}
