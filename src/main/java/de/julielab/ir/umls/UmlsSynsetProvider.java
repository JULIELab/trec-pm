package de.julielab.ir.umls;

import de.julielab.ir.cache.CacheAccess;
import de.julielab.ir.cache.CacheService;
import de.julielab.java.utilities.FileUtilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class UmlsSynsetProvider {
    public static final String DEFAULT_SEPARATOR = "\t";
    private static final Logger log = LogManager.getLogger();
    private final static String defaultSynsetFile = "resources/umlsSynsets.txt.gz";
    private static final String defaultSemanticTypesFile = "resources/umlsSemanticTypes.txt.gz";
    private static UmlsSynsetProvider instance;
    private final String umlsSynsetFile;
    private final String umlsSemanticTypesFile;
    private final String separator;
    private final boolean useCache;
    private final boolean containTermInSynset;
    private CacheAccess<String, Set<String>> cuisForTermCache;
    private CacheAccess<String, UmlsSynset> cuiSynsetCache;
    private CacheAccess<String, Set<UmlsSynset>> synsetCache;
    private CacheAccess<String,  Set<String>> semanticTypesCache;

    /**
     * Provides synsets for a term
     *
     * @param umlsSynsetFile      Qualified file name with synsets
     * @param separator           Used in synset file to separate terms
     * @param containTermInSynset Is term part of its own synset?
     */
    protected UmlsSynsetProvider(String umlsSynsetFile, String umlsSemanticTypesFile, String separator,
                                 boolean containTermInSynset, boolean useCache) {

        this.umlsSynsetFile = umlsSynsetFile;
        this.umlsSemanticTypesFile = umlsSemanticTypesFile;
        this.separator = separator;
        this.containTermInSynset = containTermInSynset;
        this.useCache = useCache;

        if (useCache) {
            synsetCache = CacheService.getInstance().getCacheAccess("umls.db", "UmlsSynsets", CacheAccess.STRING, CacheAccess.JAVA);
            cuisForTermCache = CacheService.getInstance().getCacheAccess("umls.db", "CUIsForTerms", CacheAccess.STRING, CacheAccess.JAVA);
            cuiSynsetCache = CacheService.getInstance().getCacheAccess("umls.db", "CUISynsets", CacheAccess.STRING, CacheAccess.JAVA);
            semanticTypesCache = CacheService.getInstance().getCacheAccess("umls.db", "CUISymanticTypes", CacheAccess.STRING, CacheAccess.JAVA);
        }
    }


    public static UmlsSynsetProvider getInstance() {
        if (instance == null) {
            instance = new UmlsSynsetProvider(defaultSynsetFile, defaultSemanticTypesFile, DEFAULT_SEPARATOR, false, true);
        }
        return instance;
    }

    private Set<UmlsSynset> getSynsetsFromFile(String umlsSynsetFile, String separator, boolean containTermInSynset, String inputTerm) throws IOException {
        Set<UmlsSynset> ret = new HashSet<>();
        try (final BufferedReader br = FileUtilities.getReaderFromFile(new File(umlsSynsetFile))) {
            br.lines().forEach(line -> {
                final String[] record = line.split(separator);
                // The first element of the record is the CUI so let's start at the second index
                Set<String> synset = java.util.Arrays.stream(record, 1, record.length).collect(Collectors.toSet());
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
                        Set<String> synset = java.util.Arrays.stream(record, 1, record.length).collect(Collectors.toSet());
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

    public  Set<String> getSemanticTypeForCui(String cui) {
        Set<String> semTypes = useCache ? semanticTypesCache.get(cui) : null;
        if (semTypes == null) {
            semTypes = getSemanticTypeFromFile(umlsSemanticTypesFile, separator, cui);
            if (useCache)
                semanticTypesCache.put(cui, semTypes);
        }
        return semTypes;
    }

    private Set<String> getSemanticTypeFromFile(String umlsSemanticTypesFile, String separator, String cui) {
        Set<String> types = null;
        try (final BufferedReader br = FileUtilities.getReaderFromFile(new File(umlsSemanticTypesFile))) {
          types = br.lines().map(line -> line.split(separator)).filter(split -> split[0].equals(cui)).map(split -> split[1]).collect(Collectors.toSet());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return types;
    }

    public Set<String> getSemanticTypes(String term) {
        final Set<String> cuis = getCuis(term);
        return cuis.stream().map(this::getSemanticTypeForCui).flatMap(Collection::stream).collect(Collectors.toSet());
    }


}
