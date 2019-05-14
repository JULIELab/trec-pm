package de.julielab.ir.cache;

public class CacheAccess<K, V> {
    private CacheService cacheService;
    private String cacheId;

    public CacheAccess(String cacheId) {
        this.cacheId = cacheId;
        this.cacheService = CacheService.getInstance();
    }

    public V get(K key) {

    }
}
