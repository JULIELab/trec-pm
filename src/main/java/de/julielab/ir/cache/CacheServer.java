package de.julielab.ir.cache;

import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CacheServer {
    private File cacheDir;
    private int port;
    private ExecutorService executorService;


    public CacheServer(File cacheDir, int port, int numThreads) {
        this.cacheDir = cacheDir;
        this.port = port;
        executorService = Executors.newFixedThreadPool(numThreads);
    }

    public static void main(String args[]) throws IOException {
        final File cacheDir = new File(args[0]);
        final int port = Integer.valueOf(args[1]);
        final int numThreads = Integer.valueOf(args[2]);
        final CacheServer cacheServer = new CacheServer(cacheDir, port, numThreads);
        cacheServer.run();
    }

    private void run() throws IOException {
        final ServerSocket serverSocket = new ServerSocket(port);
        while (true) {
            final Socket socket = serverSocket.accept();
            executorService.submit(new RequestServer(socket));
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
                    if (method.equalsIgnoreCase("put"))
                        value = ois.readObject();

                    Serializer<?> keySerializer = null;
                    Serializer<?> valueSerializer = null;
                    if (keySerializerName.equalsIgnoreCase("string"))
                        keySerializer = Serializer.STRING;
                    else if (keySerializerName.equalsIgnoreCase("java"))
                        keySerializer = Serializer.JAVA;
                    if (valueSerializerName.equalsIgnoreCase("string"))
                        valueSerializer = Serializer.STRING;
                    else if (valueSerializerName.equalsIgnoreCase("java"))
                        valueSerializer = Serializer.JAVA;
                    final CacheService cacheService = CacheService.getInstance();
                    final HTreeMap cache = cacheService.getCache(Path.of(cacheDir.getAbsolutePath(), cacheName).toFile(), cacheRegion, keySerializer, valueSerializer);

                    if (method.equalsIgnoreCase("get")) {
                        final Object o = cache.get(key);
                        oos.writeObject(o);
                    } else if (method.equalsIgnoreCase("put")) {
                        cache.put(key, value);
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
            }
        }
    }
}
