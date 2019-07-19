package de.julielab.ir.umls;

import de.julielab.ir.cache.CacheAccess;
import de.julielab.ir.cache.CacheService;
import de.julielab.java.utilities.FileUtilities;
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
    private final String umlsSynsetFile;
    private final String separator;
    private static boolean useCache;
    private CacheAccess<String, Set<String>> parentsCache;
    private CacheAccess<String, Set<String>> childrenCache;
    private static String defaultRelationsFile = "resources/umlsRelations.txt.gz";

    private UmlsRelationsProvider(String umlsSynsetFile, boolean useCache) throws IOException {
        this(umlsSynsetFile, DEFAULT_SEPARATOR, useCache);
    }

    private UmlsRelationsProvider(String umlsSynsetFile, String separator, boolean useCache) throws IOException {

        this.umlsSynsetFile = umlsSynsetFile;
        this.separator = separator;
        this.useCache = useCache;

        parentsCache = CacheService.getInstance().getCacheAccess("umls.db", "UmlsRelationParents", CacheAccess.STRING, CacheAccess.JAVA);
        childrenCache = CacheService.getInstance().getCacheAccess("umls.db", "UmlsRelationChildren", CacheAccess.STRING, CacheAccess.JAVA);
    }

    public static UmlsRelationsProvider getInstance() {
        if (instance == null) {
            try {
                instance = new UmlsRelationsProvider(defaultRelationsFile, useCache);
            } catch (IOException e) {
                log.error("Could not read UMLS data from " + defaultRelationsFile, e);
            }
        }
        return instance;
    }

    private Set<String> getRelativesFromFile(String umlsSynsetFile, String separator, String cui, Relation relation) throws IOException {
        Set<String> ret = new HashSet<>();
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

    /**
     * <p>Resets the service to use the given source for relations.</p>
     * <p>This is mostly used for testing.</p>
     * @param file The UMLS relations file to use.
     */
    public static void setRelationsSourceFile(String file) {
        defaultRelationsFile = file;
        instance = null;
    }

    /**
     * <p>Resets the service to make usage of caching as specified.</p>
     * <p>This is mostly used for testing.</p>
     * @param useCache Whether or not to use caching.
     */
    public static void setUseCache(boolean useCache) {
        UmlsRelationsProvider.useCache = useCache;
        instance = null;
    }

    public List<Set<String>> getRelatives(String cui, Relation relation, int maxDistance) {
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

    public Set<String> getRelatives(String cui, Relation relation) {
        CacheAccess<String, Set<String>> cacheToUse = relation == Relation.PARENT ? parentsCache : childrenCache;
        Set<String> sets = useCache ? cacheToUse.get(cui) : null;
        if (sets == null) {
            try {
                sets = getRelativesFromFile(umlsSynsetFile, separator, cui, relation);
                if (useCache)
                    cacheToUse.put(cui, sets);
            } catch (IOException e) {
                log.error("Could not retrieve synsets for term {}", cui, e);
            }
        }
        return sets;
    }

    public enum Relation {PARENT, CHILD}
}
