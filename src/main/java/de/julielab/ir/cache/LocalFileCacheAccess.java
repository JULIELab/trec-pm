package de.julielab.ir.cache;

import at.medunigraz.imi.bst.config.TrecConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mapdb.Serializer;

import java.io.File;

public class LocalFileCacheAccess<K, V> extends CacheAccess<K, V> {
    private static final Logger log = LogManager.getLogger();
    private final CacheService cacheService;
    private final File cacheFile;
    private final Serializer<K> keySerializer;
    private final Serializer<V> valueSerializer;

    public LocalFileCacheAccess(String cacheId, String cacheRegion, String keySerializer, String valueSerializer) {
        super(cacheId, cacheRegion);
        this.keySerializer = getSerializerByName(keySerializer);
        this.valueSerializer = getSerializerByName(valueSerializer);
        cacheService = CacheService.getInstance();
        cacheFile = new File(getCacheDir(), cacheId);
    }

    private File getCacheDir() {
        final File cacheDir = new File(TrecConfig.CACHE_DIR);
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        return cacheDir;
    }

    @Override
    public V get(K key) {
        return cacheService.getCache(cacheFile, cacheRegion, keySerializer, valueSerializer).get(key);
    }

    @Override
    public boolean put(K key, V value) {
        if (!cacheService.isDbReadOnly(cacheFile)) {
            cacheService.getCache(cacheFile, cacheRegion, keySerializer, valueSerializer).put(key, value);
            cacheService.commitCache(cacheFile);
            return true;
        } else {
            log.debug("Could not write value to cache {} because it is read-only.", cacheFile);
        }
        return false;
    }

    @Override
    public boolean isReadOnly() {
        return cacheService.isDbReadOnly(cacheFile);
    }

}
