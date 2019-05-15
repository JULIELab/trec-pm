package at.medunigraz.imi.bst.trec.expansion;

import at.medunigraz.imi.bst.trec.model.TopicGene;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderHeaderAware;
import de.julielab.java.utilities.FileUtilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class Cosmic {
    private Map<String, Set<String>> drugsByGeneMutation;


    private static Cosmic instance;

    private Cosmic() {
        drugsByGeneMutation = readResistanceMutationsFile(new File("resources/CosmicResistanceMutations.tsv.gz"));
    }

    public static Cosmic getInstance() {
        if (instance == null) {
            instance = new Cosmic();
        }
        return instance;

    }

    /**
     * Returns drugs against which the passed gene with its mutation is resistant
     * according to the COSMIC CosmicResistanceMutations.tsv file.
     *
     * @param gene The gene instance for that we seek drugs for which is known that the gene with its mutation confers resistance against.
     * @return The drugs that the gene mutation confers resistance against according to COSMIS.
     */
    public Set<String> getDrugsThatGeneMutationConfersResistanceAgainst(TopicGene gene) {
        String mapkey = (gene.getGeneSymbol() + "-" + gene.getMutation()).toUpperCase();
        return drugsByGeneMutation.getOrDefault(mapkey, Collections.emptySet());
    }

    private Map<String, Set<String>> readResistanceMutationsFile(File resistanceMutationsFile) {
        Map<String, Set<String>> map = new HashMap<>();
        try (final BufferedReader br = FileUtilities.getReaderFromFile(resistanceMutationsFile)) {
            final CSVParser parser = new CSVParserBuilder().withSeparator('\t').build();
            final CSVReaderHeaderAware csvReader = new CSVReaderHeaderAware(br, 0, parser, false, false, 0, null);
            Map<String, String> values;
            while ((values = csvReader.readMap()) != null) {
                String geneName = values.get("Gene Name");
                String drug = values.get("Drug Name").toLowerCase();
                String mutation = values.get("AA Mutation").replaceFirst("^p\\.", "");

                String mapkey = (geneName + "-" + mutation).toUpperCase();
                Set<String> drugs = map.get(mapkey);
                if (drugs == null) {
                    drugs = new HashSet<>();
                    map.put(mapkey, drugs);
                }
                if (drug.equalsIgnoreCase("Tyrosine kinase inhibitor - NS")) {
                    final String[] drugSplit = drug.split(" - ");
                    drugs.add(drugSplit[0]);
                    drugs.add(drugSplit[1]);
                } else {
                    drugs.add(drug);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }
}
