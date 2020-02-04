package de.julielab.ir.umls;

import de.julielab.java.utilities.FileUtilities;
import de.julielab.java.utilities.cache.CacheAccess;
import de.julielab.java.utilities.cache.CacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UmlsSynsetProvider {
    public static final String DEFAULT_SEPARATOR = "\t";
    private static final Logger log = LoggerFactory.getLogger(UmlsSynsetProvider.class);
    private static String defaultSynsetFile = "resources/umlsSynsets.txt.gz";
    private static String defaultSemanticTypesFile = "resources/umlsSemanticTypes.txt.gz";
    private static String defaultPreferredTermsFile = "resources/umlsPreferredTerms.txt.gz";
    private static UmlsSynsetProvider instance;
    private final String umlsSynsetFile;
    private final String umlsSemanticTypesFile;
    private final String umlsPreferredTermsFile;
    private final String separator;
    private final boolean useCache;
    private final boolean containTermInSynset;
    private CacheAccess<String, Set<String>> cuisForTermCache;
    private CacheAccess<String, UmlsSynset> cuiSynsetCache;
    private CacheAccess<String, Set<UmlsSynset>> synsetCache;
    private CacheAccess<String, Set<String>> semanticTypesCache;
    private CacheAccess<String, String> preftermForCuiCache;

    /**
     * Provides synsets for a term
     *
     * @param umlsSynsetFile      Qualified file name with synsets
     * @param separator           Used in synset file to separate terms
     * @param containTermInSynset Is term part of its own synset?
     */
    protected UmlsSynsetProvider(String umlsSynsetFile, String umlsSemanticTypesFile, String umlsPreferredTermsFile, String separator,
                                 boolean containTermInSynset, boolean useCache) {

        this.umlsSynsetFile = umlsSynsetFile;
        this.umlsSemanticTypesFile = umlsSemanticTypesFile;
        this.umlsPreferredTermsFile = umlsPreferredTermsFile;
        this.separator = separator;
        this.containTermInSynset = containTermInSynset;
        this.useCache = useCache;

        if (useCache) {
            synsetCache = CacheService.getInstance().getCacheAccess("umls.db", "UmlsSynsets", CacheAccess.STRING, CacheAccess.JAVA);
            cuisForTermCache = CacheService.getInstance().getCacheAccess("umls.db", "CUIsForTerms", CacheAccess.STRING, CacheAccess.JAVA);
            cuiSynsetCache = CacheService.getInstance().getCacheAccess("umls.db", "CUISynsets", CacheAccess.STRING, CacheAccess.JAVA);
            semanticTypesCache = CacheService.getInstance().getCacheAccess("umls.db", "CUISymanticTypes", CacheAccess.STRING, CacheAccess.JAVA);
            preftermForCuiCache = CacheService.getInstance().getCacheAccess("umls.db", "CUIPrefTerms", CacheAccess.STRING, CacheAccess.STRING);
        }
    }

    /**
     * Sets the default file to load the UMLS synsets from. Only has an effect if called before the first
     * call to {@link #getInstance()}.
     *
     * @param file
     */
    public static void setDefaultSynsetFile(String file) {
        defaultSynsetFile = file;
    }

    /**
     * Sets the default file to load the UMLS semantic types from. Only has an effect if called before the first
     * call to {@link #getInstance()}.
     *
     * @param file
     */
    public static void setDefaultSemanticTypesFile(String file) {
        defaultSemanticTypesFile = file;
    }

    public synchronized static UmlsSynsetProvider getInstance() {
        if (instance == null) {
            boolean useCache = true;
            instance = new UmlsSynsetProvider(defaultSynsetFile, defaultSemanticTypesFile, defaultPreferredTermsFile,DEFAULT_SEPARATOR, false, useCache);
            log.debug("Is caching used for {}: {}", UmlsSynsetProvider.class.getSimpleName(), useCache);
        }
        return instance;
    }

    private Set<UmlsSynset> getSynsetsFromFile(String umlsSynsetFile, String separator, boolean containTermInSynset, String inputTerm) throws IOException {
        Set<UmlsSynset> ret = new HashSet<>();
        log.trace("Reading file {} to obtain synsets for {}", umlsSynsetFile, inputTerm);
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

    private String getPreftermFromFile(String umlsPreftermsFile, String separator, String cui) throws IOException {
        log.trace("Reading file {} to obtain the preferred term for {}", umlsPreftermsFile, cui);
        String prefTerm = null;
        try (final BufferedReader br = FileUtilities.getReaderFromFile(new File(umlsPreftermsFile))) {
            Optional<String> prefTermOpt = br.lines().map(line -> line.split(separator)).filter(split -> split[0].equals(cui)).map(split -> split[1]).findAny();
            if (prefTermOpt.isPresent()) {
                prefTerm = prefTermOpt.get();
                if (useCache)
                    preftermForCuiCache.put(cui, prefTerm);
            }
        }
        return prefTerm;
    }

    public synchronized String getPreftermForCui(String cui) {
        String prefTerm = useCache ? preftermForCuiCache.get(cui) : null;
        if (prefTerm == null) {
            try {
                prefTerm = getPreftermFromFile(umlsPreferredTermsFile, separator, cui);
            } catch (IOException e) {
                log.error("Could not retrieve the preferred term for CUI {}", cui, e);
            }
        }
        return prefTerm;
    }

    public synchronized String getPreftermForTerm(String term) {
        Optional<List<String>> mostFrequentPrefTerm = getCuis(term).stream()
                .map(this::getPreftermForCui)
                .collect(Collectors.groupingBy(Function.identity()))
                .values().stream()
                .sorted(Comparator.<Collection>comparingInt(Collection::size).reversed())
                .findFirst();
        if (mostFrequentPrefTerm.isPresent())
            return mostFrequentPrefTerm.get().get(0);
        return null;
    }

    public synchronized Set<UmlsSynset> getSynsets(String term) {
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

    public synchronized Set<String> getCuis(String term) {
        try {
            Set<String> cuisForTerm = useCache ? cuisForTermCache.get(term) : null;
            if (cuisForTerm == null) {
                cuisForTerm = getCuisFromFile(umlsSynsetFile, separator, term);
                if (useCache)
                    cuisForTermCache.put(term, cuisForTerm);
            }
            return cuisForTerm;
        } catch (IOException e) {
            log.error("Could not retrieve the cuis form term {}", term, e);
        }
        return null;
    }

    public synchronized UmlsSynset getCuiSynset(String cui) {
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
        log.trace("Reading file {} to obtain CUI synsets for {}", umlsSynsetFile, cui);
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
            } else if (useCache){
                cuiSynsetCache.put(cui, UmlsSynset.EMPTY);
            }
        }
        return ret;
    }

    private Set<String> getCuisFromFile(String umlsSynsetFile, String separator, String term) throws IOException {
        Set<String> cuis = new HashSet<>();
        log.trace("Reading file {} to obtain CUIs for {}", umlsSynsetFile, term);
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

    public synchronized Set<String> getSemanticTypeForCui(String cui) {
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

    public synchronized Set<String> getSemanticTypes(String term) {
        final Set<String> cuis = getCuis(term);
        return cuis.stream().map(this::getSemanticTypeForCui).flatMap(Collection::stream).collect(Collectors.toSet());
    }


}
