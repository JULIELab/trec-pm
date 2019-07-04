package at.medunigraz.imi.bst.trec.model;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TopicSet {

	private static final String TAGNAME = "topic";

    private List<Topic> topics = new ArrayList<>();

	public TopicSet(Collection<Topic> topics) {
		this.topics = new ArrayList<>(topics);
	}

	public TopicSet(File xmlFile, Challenge challenge, Task task, int year) {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

		Document doc;
		try {
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			doc = documentBuilder.parse(xmlFile);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		NodeList xmlTopics = doc.getElementsByTagName(TAGNAME);

		for (int i = 0; i < xmlTopics.getLength(); i++) {
			Element element = (Element) xmlTopics.item(i);
			Topic t = Topic.fromElement(element);
			t.setChallenge(challenge);
			t.setTask(task);
			t.setYear(year);
			topics.add(t);
		}
	}

	public List<Topic> getTopics() {
		return topics;
	}

}
