package at.medunigraz.imi.bst.trec.model;

import com.opencsv.CSVWriter;
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
import java.util.function.Supplier;

public class TopicSet extends QueryDescriptionSet<Topic>{

	private static final String TAGNAME = "topic";

	public TopicSet() {
		super();
	}

	public TopicSet(Collection<Topic> topics) {
		super(topics);
	}

	@Override
	public Supplier<QueryDescriptionSet<Topic>> getSupplier() {
		return TopicSet::new;
	}

	public TopicSet(String xmlFile, Challenge challenge, int year) {
		super(xmlFile, challenge, year, TAGNAME, Topic::fromElement);
	}

	/**
	 * Auxiliary main method to dump topics into different formats.
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		toCSV(TrecPMTopicSetFactory.topics2017(), new File("topics2017.tsv"));
		toCSV(TrecPMTopicSetFactory.topics2018(), new File("topics2018.tsv"));
		toCSV(TrecPMTopicSetFactory.topics2019(), new File("topics2019.tsv"));

		TopicSet topicSet = TrecPMTopicSetFactory.topics2019();
		List<String> diseases = uniqueDiseases(topicSet);
		List<String> genes = uniqueGenes(topicSet);

		System.out.println(String.join(System.lineSeparator(), diseases));
		System.out.println(String.join(System.lineSeparator(), genes));
	}

	private static void toCSV(TopicSet topicSet, File output) throws IOException {
		CSVWriter writer = new CSVWriter(new FileWriter(output), '\t', CSVWriter.NO_QUOTE_CHARACTER, '\\', System.getProperty("line.separator"));
		for (Topic topic : topicSet) {
			String id = String.valueOf(topic.getNumber());
			String disease = topic.getDisease();
			String gene = topic.getGeneField();
			String demo = topic.getDemographic();
			writer.writeNext(new String[]{id, disease, gene, demo});
		}
		writer.close();
	}

	private static List<String> uniqueDiseases(TopicSet topicSet) {
		List<String> ret = new ArrayList<>();

		Set<String> diseaseSet = new HashSet<>();

		for (Topic topic : topicSet) {
			String disease = topic.getDisease();

			// Do not repeat diseases
			if (diseaseSet.contains(disease)) {
				continue;
			}
			diseaseSet.add(disease);

			ret.add(disease);
		}

		return ret;
	}

	private static List<String> uniqueGenes(TopicSet topicSet) {
		List<String> ret = new ArrayList<>();

		Set<String> geneSet = new HashSet<>();

		for (Topic topic : topicSet) {
			TopicGene[] genes = topic.getGenes();
			for (TopicGene gene : genes) {
				String geneSymbol = gene.getGeneSymbol();

				// Do not repeat genes
				if (geneSet.contains(geneSymbol)) {
					continue;
				}
				geneSet.add(geneSymbol);

				ret.add(geneSymbol);
			}
		}

		return ret;
	}

}
