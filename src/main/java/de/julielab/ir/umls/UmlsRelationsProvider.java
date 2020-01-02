package de.julielab.ir.umls;

import de.julielab.java.utilities.FileUtilities;
import de.julielab.java.utilities.cache.CacheAccess;
import de.julielab.java.utilities.cache.CacheService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class UmlsRelationsProvider {
    private static final String DEFAULT_SEPARATOR = "\t";
    private static final Logger log = LogManager.getLogger();
    private static UmlsRelationsProvider instance;
    private static boolean useCache = true;
    private static String defaultRelationsFile = "resources/umlsRelations.txt.gz";
    private final String umlsRelationsFile;
    private final String separator;
    private CacheAccess<String, Set<String>> parentsCache;
    private CacheAccess<String, Set<String>> childrenCache;

    protected UmlsRelationsProvider(String umlsRelationsFile, boolean useCache) {
        this(umlsRelationsFile, DEFAULT_SEPARATOR, useCache);
    }

    private UmlsRelationsProvider(String umlsRelationsFile, String separator, boolean useCache) {
        this.umlsRelationsFile = umlsRelationsFile;
        this.separator = separator;
        this.useCache = useCache;

        if (useCache) {
            parentsCache = CacheService.getInstance().getCacheAccess("umls.db", "UmlsRelationParents", CacheAccess.STRING, CacheAccess.JAVA);
            childrenCache = CacheService.getInstance().getCacheAccess("umls.db", "UmlsRelationChildren", CacheAccess.STRING, CacheAccess.JAVA);
        }
    }

    public synchronized static UmlsRelationsProvider getInstance() {
        if (instance == null) {
            instance = new UmlsRelationsProvider(defaultRelationsFile, useCache);
            log.debug("Is caching used for {}: {}", UmlsRelationsProvider.class.getSimpleName(), useCache);
        }
        return instance;
    }

    /**
     * <p>Resets the service to use the given source for relations.</p>
     * <p>This is mostly used for testing.</p>
     *
     * @param file The UMLS relations file to use.
     */
    public static void setRelationsSourceFile(String file) {
        defaultRelationsFile = file;
        instance = null;
    }

    /**
     * <p>Resets the service to make usage of caching as specified.</p>
     * <p>This is mostly used for testing.</p>
     *
     * @param useCache Whether or not to use caching.
     */
    public static void setUseCache(boolean useCache) {
        UmlsRelationsProvider.useCache = useCache;
        instance = null;
    }

    private Set<String> getRelativesFromFile(String umlsSynsetFile, String separator, String cui, Relation relation) throws IOException {
        Set<String> ret = new HashSet<>();
        log.trace("Reading file {} to obtain {} relations for {}", umlsSynsetFile, relation, cui);
        try (final BufferedReader br = FileUtilities.getReaderFromFile(new File(umlsSynsetFile))) {
            br.lines().forEach(line -> {
                final String[] record = line.split(separator);
                // CUI1 rel CUI2
                // rel = PAR|RN
                // So: specific terms to the left, broader terms to the right
                boolean lineIsaHit = relation == Relation.PARENT ? record[0].equals(cui) : record[2].equals(cui);
                if (lineIsaHit)
                    ret.add(relation == Relation.PARENT ? record[2] : record[0]);
            });
        }
        return ret;
    }

    public synchronized List<Set<String>> getRelatives(String cui, Relation relation, int maxDistance) {
        Set<String> currentCuis = Collections.singleton(cui);
        List<Set<String>> relativeLevels = new ArrayList<>();
        for (int i = 0; i < maxDistance; i++) {
            Set<String> level = new HashSet<>();
            for (String currentLevelCui : currentCuis) {
                level.addAll(getRelatives(currentLevelCui, relation));
            }
            relativeLevels.add(level);
            currentCuis = level;
        }
        return relativeLevels;
    }

    public synchronized Set<String> getRelatives(String cui, Relation relation) {
        CacheAccess<String, Set<String>> cacheToUse = relation == Relation.PARENT ? parentsCache : childrenCache;
        Set<String> sets = useCache ? cacheToUse.get(cui) : null;
        if (sets == null) {
            try {
                sets = getRelativesFromFile(umlsRelationsFile, separator, cui, relation);
                if (useCache) {
                    cacheToUse.put(cui, sets);
                }
            } catch (IOException e) {
                log.error("Could not retrieve synsets for term {}", cui, e);
            }
        }
        return sets;
    }

    public enum Relation {PARENT, CHILD}
}
