package de.julielab.ir.cache;

import at.medunigraz.imi.bst.config.TrecConfig;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CacheService {
    private static CacheService service;
    private Map<String, DB> dbs = new HashMap<>();
    private Set<String> readOnly = new HashSet<>();

    private CacheService() {
    }

    static CacheService getInstance() {
        if (service == null)
            service = new CacheService();
        return service;
    }

     boolean isDbReadOnly(File file) {
        try {
            return readOnly.contains(file.getCanonicalPath());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    <K, V> HTreeMap<K, V> getCache(File dbFile, String regionName, Serializer<K> keySerializer, Serializer<V> valueSerializer) {
        final DB filedb = getFiledb(dbFile);
        final DB.HashMapMaker<K, V> dbmaker = filedb.hashMap(regionName).keySerializer(keySerializer).valueSerializer(valueSerializer);
        if (isDbReadOnly(dbFile))
            return dbmaker.open();
        return dbmaker.
                createOrOpen();
    }


    void commitCache(File dbFile) {
        if (!isDbReadOnly(dbFile))
            getFiledb(dbFile).commit();
    }

    private DB getFiledb(File cacheDir) {
        try {
            DB db = dbs.get(cacheDir.getCanonicalPath());
            if (db == null) {
                final DBMaker.Maker dbmaker = DBMaker
                        .fileDB(cacheDir.getAbsolutePath())
                        .fileMmapEnable()
                        .transactionEnable()
                        .closeOnJvmShutdown();
                if (TrecConfig.DOCUMENT_DB_CACHE_READ_ONLY && cacheDir.exists()) {
                    dbmaker.readOnly();
                    readOnly.add(cacheDir.getCanonicalPath());
                }
                db = dbmaker.make();
                dbs.put(cacheDir.getCanonicalPath(), db);
            }
            return db;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
