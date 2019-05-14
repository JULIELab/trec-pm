package de.julielab.ir.cache;

import at.medunigraz.imi.bst.config.TrecConfig;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CacheServer {
    private File cacheDir;
    private String host;
    private int port;
    private ExecutorService executorService;

    public static final String METHOD_GET = "get";
    public static final String METHOD_PUT = "put";
    public static final String RESPONSE_OK = "OK";


    public CacheServer(File cacheDir, String host, int port, int numThreads) {
        this.cacheDir = cacheDir;
        this.host = host;
        this.port = port;
        executorService = Executors.newFixedThreadPool(numThreads);
    }

    public static void main(String args[]) throws IOException {
        final File cacheDir = new File(TrecConfig.CACHE_DIR);
        final String host = TrecConfig.CACHE_HOST;
        final int port = TrecConfig.CACHE_PORT;
        final int numThreads = Integer.valueOf(args[0]);
        final CacheServer cacheServer = new CacheServer(cacheDir, host, port, numThreads);
        cacheServer.run();
    }

    private void run() throws IOException {
        final ServerSocket serverSocket = new ServerSocket(port, 1000, InetAddress.getByName(host));
        try {
            while (true) {
                final Socket socket = serverSocket.accept();
                executorService.submit(new RequestServer(socket));
            }
        } finally {
            serverSocket.close();
        }
    }

    private class RequestServer extends Thread {
        private Socket socket;

        public RequestServer(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (ObjectInputStream ois = new ObjectInputStream(socket.getInputStream()); ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())) {
                try {
                    final String method = ois.readUTF();
                    final String cacheName = ois.readUTF();
                    final String cacheRegion = ois.readUTF();
                    final String keySerializerName = ois.readUTF();
                    final String valueSerializerName = ois.readUTF();
                    final Object key = ois.readObject();
                    Object value = null;
                    if (method.equalsIgnoreCase(METHOD_PUT))
                        value = ois.readObject();

                    Serializer<?> keySerializer = CacheAccess.getSerializerByName(keySerializerName);
                    Serializer<?> valueSerializer = CacheAccess.getSerializerByName(valueSerializerName);
                    final CacheService cacheService = CacheService.getInstance();
                    final File cacheFile = Path.of(cacheDir.getAbsolutePath(), cacheName).toFile();
                    final HTreeMap cache = cacheService.getCache(cacheFile, cacheRegion, keySerializer, valueSerializer);

                    if (method.equalsIgnoreCase(METHOD_GET)) {
                        final Object o = cache.get(key);
                        oos.writeObject(o);
                    } else if (method.equalsIgnoreCase(METHOD_PUT)) {
                        cache.put(key, value);
                        cacheService.commitCache(cacheFile);
                        oos.writeObject("OK");
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    if (oos != null) {
                        try {
                            oos.writeObject(e);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
