package at.medunigraz.imi.bst.trec.model;

import com.opencsv.CSVWriter;
import de.julielab.ir.model.QueryDescription;
import de.julielab.java.utilities.FileUtilities;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;

public class QueryDescriptionSet<T extends QueryDescription> extends ArrayList<T> {

	public QueryDescriptionSet(Collection<T> topics) {
		super(topics);
	}

	public QueryDescriptionSet(String xmlFile, Challenge challenge, int year, String tagname, Function<Element, T> topicParser) {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

		Document doc;
		try {
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			doc = documentBuilder.parse(FileUtilities.findResource(xmlFile));
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		NodeList xmlTopics = doc.getElementsByTagName(tagname);

		for (int i = 0; i < xmlTopics.getLength(); i++) {
			Element element = (Element) xmlTopics.item(i);
			T t = topicParser.apply(element);
			t.setChallenge(challenge);
			t.setYear(year);
			add(t);
		}
	}
}
