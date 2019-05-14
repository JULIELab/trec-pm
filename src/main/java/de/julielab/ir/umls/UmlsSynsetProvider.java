package de.julielab.ir.umls;

import com.google.common.collect.Sets;
import de.julielab.ir.cache.CacheAccess;
import de.julielab.ir.cache.CacheService;
import de.julielab.java.utilities.FileUtilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class UmlsSynsetProvider {
    private static final String DEFAULT_SEPARATOR = "---";
    private static final Logger log = LogManager.getLogger();
    private static UmlsSynsetProvider instance;
    private final String umlsSynsetFile;
    private final String separator;
    private final boolean containTermInSynset;
    private CacheAccess<String, Set<Set<String>>> cache;

    /**
     * Provides synsets for a term, assumes synset file uses default separator
     * "---" and will not include term as part of its synset
     *
     * @param umlsSynsetFile Qualified file name with synsets
     * @throws IOException
     */
    private UmlsSynsetProvider(String umlsSynsetFile) throws IOException {
        this(umlsSynsetFile, DEFAULT_SEPARATOR, false);
    }

    /**
     * Provides synsets for a term and will not include term as part of its
     * synset
     *
     * @param umlsSynsetFile Qualified file name with synsets
     * @param separator      Used in synset file to separate terms
     * @throws IOException
     */
    private UmlsSynsetProvider(String umlsSynsetFile, String separator)
            throws IOException {
        this(umlsSynsetFile, separator, false);
    }

    /**
     * Provides synsets for a term
     *
     * @param umlsSynsetFile      Qualified file name with synsets
     * @param separator           Used in synset file to separate terms
     * @param containTermInSynset Is term part of its own synset?
     * @throws IOException
     */
    private UmlsSynsetProvider(String umlsSynsetFile, String separator,
                               boolean containTermInSynset) throws IOException {

        this.umlsSynsetFile = umlsSynsetFile;
        this.separator = separator;
        this.containTermInSynset = containTermInSynset;

        cache = CacheService.getInstance().getCacheAccess("umls.db", "UmlsSynsets", CacheAccess.STRING, CacheAccess.JAVA);
    }

    /**
     * Provides synsets for a term, assumes synset file uses default separator
     * "---"
     *
     * @param umlsSynsetFile      Qualified file name with synsets
     * @param containTermInSynset Is term part of its own synset?
     * @throws IOException
     */
    public UmlsSynsetProvider(String umlsSynsetFile,
                              boolean containTermInSynset) throws IOException {
        this(umlsSynsetFile, DEFAULT_SEPARATOR, containTermInSynset);
    }

    public static UmlsSynsetProvider getInstance() {
        if (instance == null) {
            try {
                instance = new UmlsSynsetProvider("resources/umlsSynsets.txt.gz");
            } catch (IOException e) {
                log.error("Could not read UMLS data from resources/umlsSynsets.txt.gz", e);
            }
        }
        return instance;
    }

    private Set<Set<String>> getSynsetsFromFile(String umlsSynsetFile, String separator, boolean containTermInSynset, String inputTerm) throws IOException {
        Set<Set<String>> ret = new HashSet<>();
        try (final BufferedReader br = FileUtilities.getReaderFromFile(new File(umlsSynsetFile))) {
            br.lines().forEach(line -> {
                HashSet<String> synset = Sets.newHashSet(line.split(separator));
                if (synset.contains(inputTerm)) {
                    if (!containTermInSynset)
                        synset.remove(inputTerm);
                    ret.add(synset);
                }
            });
        }
        return ret;
    }

    public Set<Set<String>> getSynsets(String term) {
        Set<Set<String>> sets = cache.get(term);
        if (sets == null) {
            try {
                sets = getSynsetsFromFile(umlsSynsetFile, separator, containTermInSynset, term);
                cache.put(term, sets);
            } catch (IOException e) {
                log.error("Could not retrieve synsets for term {}", term, e);
            }
        }
        return sets;
    }
}
