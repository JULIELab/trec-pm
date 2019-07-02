package de.julielab.ir.cache;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.trec.model.Result;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.units.qual.K;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.mapdb.serializer.GroupSerializer;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.*;

public class CacheService {
    private static final Logger log = LogManager.getLogger();
    private static CacheService service;
    private final String thisJvmName;
    private Map<String, DB> dbs = new HashMap<>();
    private Set<String> readOnly = new HashSet<>();

    private CacheService() {
        thisJvmName = ManagementFactory.getRuntimeMXBean().getName();
    }

    public static CacheService getInstance() {
        if (service == null)
            service = new CacheService();
        return service;
    }

    public <K, V> CacheAccess<K, V> getCacheAccess(String cacheId, String cacheRegion, String keySerializerName, String valueSerializerName) {
        switch (TrecConfig.CACHE_TYPE.toLowerCase()) {
            case "local":
                return new LocalFileCacheAccess<>(cacheId, cacheRegion, keySerializerName, valueSerializerName);
            case "remote":
                return new RemoteCacheAccess<>(cacheId, cacheRegion, keySerializerName, valueSerializerName);
            default:
                throw new IllegalArgumentException("Unknown cache type '" + TrecConfig.CACHE_TYPE + "' in the configuration.");
        }
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
        else
            log.debug("Cannot commit cache {} because it is read-only.", dbFile);
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
                if (TrecConfig.CACHE_TYPE.equalsIgnoreCase("local") && TrecConfig.CACHE_READ_ONLY && cacheDir.exists()) {
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
