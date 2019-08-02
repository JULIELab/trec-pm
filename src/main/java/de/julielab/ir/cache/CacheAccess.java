package de.julielab.ir.cache;

import org.mapdb.Serializer;

public abstract class CacheAccess<K, V> {
    protected String cacheId;
    protected String cacheRegion;

    public static final String STRING = "string";
    public static final String JAVA = "java";
    public static final String BYTEARRAY = "bytearray";

    public CacheAccess(String cacheId, String cacheRegion) {
        this.cacheId = cacheId;
        this.cacheRegion = cacheRegion;
    }

    public static <T> Serializer<T> getSerializerByName(String name) {
        switch (name.toLowerCase()) {
            case STRING:
                return (Serializer<T>) Serializer.STRING;
            case JAVA:
                return Serializer.JAVA;
            case BYTEARRAY:
                return (Serializer<T>) Serializer.BYTE_ARRAY;
            default:
                throw new IllegalArgumentException("Unsupported cache serializer '" + name + "'.");
        }
    }

    public abstract V get(K key);

    public abstract boolean put(K key, V value);

    public abstract boolean isReadOnly();
}
