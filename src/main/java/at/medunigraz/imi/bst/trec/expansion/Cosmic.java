package at.medunigraz.imi.bst.trec.expansion;

import com.opencsv.*;
import de.julielab.java.utilities.FileUtilities;
import de.julielab.java.utilities.IOStreamUtilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Cosmic {
public static void main(String args[]) {
    final Cosmic cosmic = new Cosmic();
    final Map<String, Set<String>> stringSetMap = cosmic.readResistanceMutationsFile(new File("resources/CosmicResistanceMutations.tsv.gz"));
    System.out.println(stringSetMap);
}

    private Map<String, Set<String>> drugsByGeneMutation;

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
