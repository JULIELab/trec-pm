package at.medunigraz.imi.bst.trec.stats;

import at.medunigraz.imi.bst.trec.model.Metrics;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

public class XMLStatsWriter implements StatsWriter {

	private Document doc;
	private File output;
	private Element rootElement;

	public XMLStatsWriter(File output) {
		this.output = output;

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

		DocumentBuilder documentBuilder = null;
		try {
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		doc = documentBuilder.newDocument();

		writeHeader();
	}

	@Override
	public void close() {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();

		Transformer transformer = null;
		try {
			transformer = transformerFactory.newTransformer();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
			return;
		}
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(output);

		try {
			transformer.transform(source, result);
		} catch (TransformerException e) {
			e.printStackTrace();
			return;
		}
		return;
	}

	@Override
	public void flush() {
		return;
	}

	private void writeHeader() {
		rootElement = doc.createElement("report");
		doc.appendChild(rootElement);
		flush();
	}

	@Override
	public void write(String topic, Metrics metrics) {
		Element topicElement = doc.createElement("topic");
		topicElement.setAttribute("number", topic);
		rootElement.appendChild(topicElement);

		for (int i = 1; i < FIELDS.length; i++) {
			String metricName = FIELDS[i];
			
			if (!metrics.hasMetric(metricName)) {
				continue;
			}
			
			String metricValue = String.valueOf(metrics.getMetric(metricName));

			Element metricElement = doc.createElement(metricName);
			metricElement.appendChild(doc.createTextNode(metricValue));

			topicElement.appendChild(metricElement);
		}
	}
}
