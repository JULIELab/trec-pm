package de.julielab.ir.cache;

import at.medunigraz.imi.bst.config.TrecConfig;
import org.mapdb.Serializer;

import java.io.File;
import java.nio.file.Path;

public class LocalFileCacheAccess<K, V> extends CacheAccess<K, V> {

    private final CacheService cacheService;
    private final File cacheFile;
    private final Serializer<K> keySerializer;
    private final Serializer<V> valueSerializer;

    public LocalFileCacheAccess(String cacheId, String cacheRegion, String keySerializer, String valueSerializer) {
        super(cacheId, cacheRegion);
        this.keySerializer = getSerializerByName(keySerializer);
        this.valueSerializer = getSerializerByName(valueSerializer);
        cacheService = CacheService.getInstance();
        cacheFile = Path.of(TrecConfig.CACHE_DIR, cacheId).toFile();
    }

    @Override
    public V get(K key) {
        return cacheService.getCache(cacheFile, cacheRegion, keySerializer, valueSerializer).get(key);
    }

    @Override
    public void put(K key, V value) {
        cacheService.getCache(cacheFile, cacheRegion, keySerializer, valueSerializer).put(key, value);
        cacheService.commitCache(cacheFile);
    }

}
