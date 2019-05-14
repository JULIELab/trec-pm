package at.medunigraz.imi.bst.trec.expansion;

import com.opencsv.*;
import de.julielab.java.utilities.FileUtilities;
import de.julielab.java.utilities.IOStreamUtilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class Cosmic {
public static void main(String args[]) {
    final Cosmic cosmic = new Cosmic();
    cosmic.readResistanceMutationsFile(new File("resources/CosmicResistanceMutations.tsv.gz"));
}
    private void readResistanceMutationsFile(File resistanceMutationsFile) {

        try (final BufferedReader br = FileUtilities.getReaderFromFile(resistanceMutationsFile)) {
            final CSVParser parser = new CSVParserBuilder().withSeparator('\t').build();
            final CSVReaderHeaderAware csvReader = new CSVReaderHeaderAware(br, 0, parser, false, false, 0, null);
            Map<String, String> values;
            while ((values = csvReader.readMap()) != null) {
                String geneName = values.get("Gene Name");
                String drug = values.get("Drug Name");
                String mutation = values.get("AA Mutation");
                System.out.println(geneName + " " + drug + " " + mutation);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
