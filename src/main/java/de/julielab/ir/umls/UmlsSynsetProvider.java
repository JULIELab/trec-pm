package de.julielab.ir.umls;

import de.julielab.ir.cache.CacheAccess;
import de.julielab.ir.cache.CacheService;
import de.julielab.java.utilities.FileUtilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class UmlsSynsetProvider {
    private static final String DEFAULT_SEPARATOR = "\t";
    private static final Logger log = LogManager.getLogger();
    private static UmlsSynsetProvider instance;
    private static boolean useCache = true;
    private static String defaultSynsetFile = "resources/umlsSynsets.txt.gz";
    private final String umlsSynsetFile;
    private final String separator;
    private final boolean containTermInSynset;
    private final CacheAccess<String, Set<String>> cuisForTermCache;
    private final CacheAccess<String, UmlsSynset> cuiSynsetCache;
    private CacheAccess<String, Set<UmlsSynset>> synsetCache;

    /**
     * Provides synsets for a term, assumes synset file uses default separator
     * "---" and will not include term as part of its synset
     *
     * @param umlsSynsetFile Qualified file name with synsets
     * @throws IOException
     */
    private UmlsSynsetProvider(String umlsSynsetFile, boolean useCache) throws IOException {
        this(umlsSynsetFile, DEFAULT_SEPARATOR, false, useCache);
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
                               boolean containTermInSynset, boolean useCache) throws IOException {

        this.umlsSynsetFile = umlsSynsetFile;
        this.separator = separator;
        this.containTermInSynset = containTermInSynset;
        this.useCache = useCache;

        synsetCache = CacheService.getInstance().getCacheAccess("umls.db", "UmlsSynsets", CacheAccess.STRING, CacheAccess.JAVA);
        cuisForTermCache = CacheService.getInstance().getCacheAccess("umls.db", "CUIsForTerms", CacheAccess.STRING, CacheAccess.JAVA);
        cuiSynsetCache = CacheService.getInstance().getCacheAccess("umls.db", "CUISynsets", CacheAccess.STRING, CacheAccess.JAVA);
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
                              boolean containTermInSynset, boolean useCache) throws IOException {
        this(umlsSynsetFile, DEFAULT_SEPARATOR, containTermInSynset, useCache);
    }

    public static UmlsSynsetProvider getInstance() {
        if (instance == null) {
            try {
                instance = new UmlsSynsetProvider(defaultSynsetFile, useCache);
            } catch (IOException e) {
                log.error("Could not read UMLS data from " + defaultSynsetFile, e);
            }
        }
        return instance;
    }

    /**
     * <p>Resets the service to use the given source for synsets.</p>
     * <p>This is mostly used for testing.</p>
     *
     * @param file The UMLS synset file to use.
     */
    public static void setSynsetSourceFile(String file) {
        defaultSynsetFile = file;
        instance = null;
    }

    /**
     * <p>Resets the service to make usage of caching as specified.</p>
     * <p>This is mostly used for testing.</p>
     *
     * @param useCache Whether or not to use caching.
     */
    public static void setUseCache(boolean useCache) {
        UmlsSynsetProvider.useCache = useCache;
        instance = null;
    }

    private Set<UmlsSynset> getSynsetsFromFile(String umlsSynsetFile, String separator, boolean containTermInSynset, String inputTerm) throws IOException {
        Set<UmlsSynset> ret = new HashSet<>();
        try (final BufferedReader br = FileUtilities.getReaderFromFile(new File(umlsSynsetFile))) {
            br.lines().forEach(line -> {
                final String[] record = line.split(separator);
                // The first element of the record is the CUI so let's start at the second index
                Set<String> synset = IntStream.range(1, record.length).mapToObj(i -> record[i]).collect(Collectors.toSet());
                if (synset.contains(inputTerm)) {
                    if (!containTermInSynset)
                        synset.remove(inputTerm);
                    final UmlsSynset umlsSynset = new UmlsSynset(synset, record[0]);
                    ret.add(umlsSynset);
                    if (useCache)
                        cuiSynsetCache.put(umlsSynset.getCui(), umlsSynset);
                }
            });
        }
        return ret;
    }

    public Set<UmlsSynset> getSynsets(String term) {
        Set<UmlsSynset> sets = useCache ? synsetCache.get(term) : null;
        if (sets == null) {
            try {
                sets = getSynsetsFromFile(umlsSynsetFile, separator, containTermInSynset, term);
                if (useCache)
                    synsetCache.put(term, sets);
            } catch (IOException e) {
                log.error("Could not retrieve synsets for term {}", term, e);
            }
        }
        return sets;
    }

    public Set<String> getCuis(String term) {
        try {
            Set<String> cuisForTerm = useCache ? cuisForTermCache.get(term) : null;
            if (cuisForTerm == null) {
                cuisForTerm = getCuisFromFile(umlsSynsetFile, separator, term);
            }
            return cuisForTerm;
        } catch (IOException e) {
            log.error("Could not retrieve the cuis form term {}", term, e);
        }
        return null;
    }

    public UmlsSynset getCuiSynset(String cui) {
        try {
            UmlsSynset synset = useCache ? cuiSynsetCache.get(cui) : null;
            if (synset == null) {
                synset = getCuiSynsetFromFile(umlsSynsetFile, separator, cui);
            }
            return synset;
        } catch (IOException e) {
            log.error("Could not retrieve the synset for cui {}", cui, e);
        }
        return null;
    }

    private UmlsSynset getCuiSynsetFromFile(String umlsSynsetFile, String separator, String cui) throws IOException {
        UmlsSynset ret = UmlsSynset.EMPTY;
        try (final BufferedReader br = FileUtilities.getReaderFromFile(new File(umlsSynsetFile))) {
            final Optional<UmlsSynset> synSet = br.lines()
                    .map(line -> line.split(separator))
                    .filter(record -> record[0].equals(cui))
                    .map(record -> {
                        // The first element of the record is the CUI so let's start at the second index
                        Set<String> synset = IntStream.range(1, record.length).mapToObj(i -> record[i]).collect(Collectors.toSet());
                        return new UmlsSynset(synset, record[0]);
                    }).findAny();

            if (synSet.isPresent()) {
                ret = synSet.get();
                if (useCache)
                    cuiSynsetCache.put(ret.getCui(), ret);
            }
        }
        return ret;
    }

    private Set<String> getCuisFromFile(String umlsSynsetFile, String separator, String term) throws IOException {
        Set<String> cuis = new HashSet<>();
        try (final BufferedReader br = FileUtilities.getReaderFromFile(new File(umlsSynsetFile))) {
            br.lines().forEach(line -> {
                final String[] record = line.split(separator);
                // The first element of the record is the CUI so let's start at the second index
                for (int i = 1; i < record.length; i++)
                    if (record[i].equals(term)) {
                        cuis.add(record[0]);
                        break;
                    }
            });
        }
        return cuis;
    }


}
