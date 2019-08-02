package de.julielab.ir.cache;

import at.medunigraz.imi.bst.config.TrecConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class RemoteCacheAccess<K, V> extends CacheAccess<K, V> {
    private static Logger log = LogManager.getLogger();
    private final String keySerializer;
    private final String valueSerializer;

    public RemoteCacheAccess(String cacheId, String cacheRegion, String keySerializer, String valueSerializer) {
        super(cacheId, cacheRegion);
        this.keySerializer = keySerializer;
        this.valueSerializer = valueSerializer;
    }

    private Socket getSocket() {
        try {
            return new Socket(InetAddress.getByName(TrecConfig.CACHE_HOST), TrecConfig.CACHE_PORT);
        } catch (IOException e) {
            log.error("Could not create a socket to {}:{}", TrecConfig.CACHE_HOST, TrecConfig.CACHE_PORT, e);
            throw new IllegalStateException(e);
        }
    }

    @Override
    public V get(K key) {
        try (Socket s = getSocket()) {
            final ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
            writeDefaultInformation(CacheServer.METHOD_GET, key, oos);
            final ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
            return (V) ois.readObject();
        } catch (IOException  | ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    private void writeDefaultInformation(String method, K key, ObjectOutputStream oos) throws IOException {
        oos.writeUTF(method);
        oos.writeUTF(cacheId);
        oos.writeUTF(cacheRegion);
        oos.writeUTF(keySerializer);
        oos.writeUTF(valueSerializer);
        oos.writeObject(key);
    }

    @Override
    public boolean put(K key, V value) {
        try (Socket s = getSocket()) {
            final ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
            writeDefaultInformation(CacheServer.METHOD_PUT, key, oos);
            oos.writeObject(value);
            final ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
            final String response = ois.readUTF();
            if (response.equalsIgnoreCase(CacheServer.RESPONSE_FAILURE)) {
                Exception e = (Exception) ois.readObject();
                log.error("Could not put data into the remote cache:", e);
            }
            return response.equals(CacheServer.RESPONSE_OK);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }
}
