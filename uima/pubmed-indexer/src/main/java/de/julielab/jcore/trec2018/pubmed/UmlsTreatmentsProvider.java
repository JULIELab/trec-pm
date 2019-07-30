package de.julielab.jcore.trec2018.pubmed;

import com.google.common.collect.Sets;
import de.julielab.jcore.utility.JCoReTools;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.uima.resource.DataResource;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.ConfigurationParameterSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UmlsTreatmentsProvider implements IUmlsTreatmentsProvider {
    private final static Logger log = LoggerFactory.getLogger(UmlsTreatmentsProvider.class);
    private Map<String, List<Pair<String, String>>> cuisPerPmid = new ConcurrentHashMap<>();
    private Set<String> focusedTreatmentCuis = new HashSet<>();
    private Set<String> broadTreatmentCuis = new HashSet<>();

    @Override
    public void load(DataResource aData) throws ResourceInitializationException {
        final ConfigurationParameterSettings parameterSettings = aData.getMetaData().getConfigurationParameterSettings();
        final String treatmentsDirectory = (String) parameterSettings.getParameterValue("uncompressedTreatmentsDir");
        if (treatmentsDirectory == null)
            throw new IllegalArgumentException("You must specify the path to the directory holding the Meta Map Light results to the 'uncompressedTreatmentsDir' parameter.");
        log.info("Loading Meta Map Light-tagged treatment files from {} ",treatmentsDirectory );
        Set<String> focusedSemanticTypes = Sets.newHashSet("T061", "T121", "T200");
        Set<String> broadSemanticTypes = Sets.newHashSet("T039",
                "T060",
                "T058",
                "T059",
                "T063",
                "T062",
                "T061",
                "T195",
                "T123",
                "T200",
                "T129",
                "T121",
                "T192");
        log.info("Focused semantic types for treatments are defined as {}.", focusedSemanticTypes );
        log.info("Broad semantic types for treatments are defined as {}.", broadSemanticTypes );
        try {
            InputStream is = JCoReTools.resolveExternalResourceGzipInputStream(aData);
            LineIterator lineIt = IOUtils.lineIterator(new InputStreamReader(is, "UTF-8"));
            while (lineIt.hasNext()) {
                final String mrstyLine = lineIt.nextLine();
                // C0000005|T116|A1.4.1.2.1.7|Amino Acid, Peptide, or Protein|AT17648347|256|
                final String[] record = mrstyLine.split("\\|");
                String cui = record[0].intern();
                String semType = record[1].intern();
                if (focusedSemanticTypes.contains(semType))
                    focusedTreatmentCuis.add(cui);
                if (broadSemanticTypes.contains(semType))
                    broadTreatmentCuis.add(cui);
            }
            lineIt.close();
            log.info("Read {} focused treatment CUIs.",focusedTreatmentCuis.size() );
            log.info("Read {} broad treatment CUIs.",broadTreatmentCuis.size() );

            log.info("Now reading the treatment files and filtering for the focused and broad treament CUIs." );
            final File[] treatmentFiles = new File(treatmentsDirectory).listFiles((f, n) -> n.startsWith("pubmed") && n.contains("treatments"));
            for (int i = 0; i < treatmentFiles.length; i++) {
                File treatmentFile = treatmentFiles[i];
                final LineIterator lineIterator = IOUtils.lineIterator(new FileInputStream(treatmentFile), "UTF-8");
                while (lineIterator.hasNext()) {
                    String line = lineIterator.next();
                    final String[] split = line.split("\t");
                    if (split.length != 3) {
                        log.debug("Ignoring line '{}' of file '{}' because it doesn't consist of exactly three tab-separated columns", line, treatmentFile);
                        continue;
                    }
                    try {
                        String cui = split[1].intern();
                        if (!focusedTreatmentCuis.contains(cui) && ! broadTreatmentCuis.contains(cui))
                            continue;
                        final String text = split[2].intern();
                        cuisPerPmid.compute(split[0], (k, v) -> v != null ? v : new ArrayList<>()).add(new ImmutablePair<>(cui, text));
                    } catch (Exception e) {
                        log.error("Error when parsing treatments line '{}' of file {}.", line, treatmentFile);
                    }
                }
            }
            log.info("Loaded treatment annotations for {} documents.", cuisPerPmid.size() );
        } catch (IOException e) {
            throw new ResourceInitializationException(e);
        } catch (NullPointerException e) {
            log.error("Could not read file from {}", aData.getUri());
            throw e;
        }
    }


    @Override
    public Map<String, List<Pair<String, String>>> getCuisAndTextByPmid() {
        return cuisPerPmid;
    }

    @Override
    public Set<String> getFocusedTreatmentCuis() {
        return focusedTreatmentCuis;
    }

    @Override
    public Set<String> getBroadTreatmentCuis() {
        return broadTreatmentCuis;
    }

}
