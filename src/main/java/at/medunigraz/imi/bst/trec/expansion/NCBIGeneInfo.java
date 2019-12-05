package at.medunigraz.imi.bst.trec.expansion;

import at.medunigraz.imi.bst.trec.model.Gene;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;

public class NCBIGeneInfo {
    private static final Logger LOG = LogManager.getLogger();

    private static final String GENE_INFO_FILE = "/genes/Homo_sapiens.gene_info";

    private static Map<String, Gene> symbolToGene = new HashMap<>();

    private static NCBIGeneInfo instance = null;

    public static NCBIGeneInfo getInstance() {
        if (instance == null) {
            instance = new NCBIGeneInfo();
        }
        return instance;
    }

    private NCBIGeneInfo() {
        // singleton
        readGenes();
    }

    public boolean containsKey(String key) {
        return symbolToGene.containsKey(key);
    }

    public Gene get(String key) {
        return symbolToGene.get(key);
    }

    public List<String> getSynonyms(String symbol) {
        List<String> ret = new ArrayList<>();
        if (!symbolToGene.containsKey(symbol)) {
            return ret;
        }
        return symbolToGene.get(symbol).getSynonyms();
    }

    public Optional<String> getDescription(String symbol) {
        if (!symbolToGene.containsKey(symbol)) {
            return Optional.empty();
        }
        return Optional.of(symbolToGene.get(symbol).getDescription());
    }

    private void readGenes() {
        LOG.info("Reading genes...");

        try (CSVReader reader = new CSVReader(new InputStreamReader(NCBIGeneInfo.class.getResourceAsStream(GENE_INFO_FILE), UTF_8), '\t', CSVWriter.NO_QUOTE_CHARACTER)) {
            String[] line;
            while ((line = reader.readNext()) != null) {
                Gene gene = Gene.fromArray(line);
                symbolToGene.put(gene.getSymbol(), gene);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        LOG.info("Reading genes complete.");
    }

    /**
     * Creates a dump in SOLR synonym file
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        final File solrSynonyms = new File("geneSynonyms.txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(solrSynonyms));

        final Gene.Field[] expandTo = { Gene.Field.DESCRIPTION, Gene.Field.SYNONYMS, Gene.Field.OTHER_DESIGNATIONS };
        // Gene.Field.SYMBOL,
        // Gene.Field.DESCRIPTION,
        // Gene.Field.SYNONYMS,
        // Gene.Field.OTHER_DESIGNATIONS

        for (Map.Entry<String, Gene> entry : NCBIGeneInfo.getInstance().symbolToGene.entrySet()) {
            String symbol = entry.getKey();
            Gene gene = entry.getValue();

            StringBuilder sb = new StringBuilder(symbol);
            sb.append(" => ");
            sb.append(gene.getExpandedField(", ", expandTo));
            sb.append("\n");

            writer.write(sb.toString());
        }

        writer.close();
    }
}
