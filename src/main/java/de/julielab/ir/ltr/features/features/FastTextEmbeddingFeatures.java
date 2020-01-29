package de.julielab.ir.ltr.features.features;

import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import de.julielab.ipc.javabridge.Options;
import de.julielab.ipc.javabridge.StdioBridge;
import de.julielab.ir.ltr.Document;
import de.julielab.java.utilities.cache.CacheAccess;
import de.julielab.java.utilities.cache.CacheService;
import org.apache.uima.cas.CAS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class FastTextEmbeddingFeatures extends DocumentEmbeddingFeatures {
    public static final String FT_FEATURE_NAME = "fasttext";
    private static final int MAX_FT_BRIDGES = 4;
    private static final Logger log = LoggerFactory.getLogger(FastTextEmbeddingFeatures.class);
    private static ArrayBlockingQueue<StdioBridge<byte[]>> activeBridges = new ArrayBlockingQueue<>(MAX_FT_BRIDGES);
    private final String EMBEDDING_PATH = "resources/dim300.bin";
    private transient Pattern tokenization;
    private transient CacheAccess<String, double[]> cache;
    private transient static int numActiveBridges = 0;

    public FastTextEmbeddingFeatures() {
        super(FT_FEATURE_NAME);
    }

    public static void shutdown() {
        try {
            while (!activeBridges.isEmpty()) {
                activeBridges.remove().stop();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void init() {
        if (tokenization == null) {
            synchronized (activeBridges) {
                if (numActiveBridges < MAX_FT_BRIDGES) {
                    Options<byte[]> options = new Options(byte[].class);
                    options.setExecutable("python3");
                    options.setExternalProgramTerminationSignal("exit");
                    options.setExternalProgramReadySignal("Script is ready");
                    StdioBridge<byte[]> bridge = new StdioBridge(options, "-u", "src/main/resources/python/getFastTextEmbeddingScript.py", EMBEDDING_PATH);
                    try {
                        bridge.start();
                        activeBridges.add(bridge);
                        ++numActiveBridges;
                        log.info("Starting a bridge to a python instance. This bridge will be open until FastTextEmbeddingFeatures#shutdown() will be called. The application will not terminate otherwise.");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            initCache();
            tokenization = Pattern.compile("([.\\!?,'/()])");
        }
    }

    private void initCache() {
        if (cache == null)
            cache = CacheService.getInstance().getCacheAccess("Embeddings", "FastTextDocs", CacheAccess.STRING, CacheAccess.DOUBLEARRAY);
    }

    @Override
    protected void assignDocumentEmbeddingFeatures(Instance inst) {
        initCache();
        final Document document = (Document) inst.getSource();
        final String cachekey = EMBEDDING_PATH + "-" + document.getId();
        double[] embedding = cache.get(cachekey);
        if (embedding == null) {
            init();
            final CAS cas = document.getCas();
            final String documentText = cas.getDocumentText();
            embedding = getDocumentEmbedding(documentText);
            if (!cache.isReadOnly())
                cache.put(cachekey, embedding);
        }
        addDocumentEmbeddingFeatures((Token) inst.getData(), embedding);
    }

    @Override
    protected double[] getDocumentEmbedding(String documentText) {
        init();
        try {
            // Do some simple normalization, actually inspired by some sed and tr command example from the FastText text classification tutorial
            String tokenizedDocumentText = tokenization.matcher(documentText).replaceAll(" $1 ");
            tokenizedDocumentText = tokenizedDocumentText.toLowerCase();
            StdioBridge<byte[]> bridge = activeBridges.take();
            Stream<byte[]> response = bridge.sendAndReceive(tokenizedDocumentText);
            byte[] bytes = response.findAny().get();
            activeBridges.add(bridge);
            DoubleBuffer db = ByteBuffer.wrap(bytes).asDoubleBuffer();
            double[] array = new double[db.capacity()];
            for (int i = 0; i < db.capacity(); i++)
                array[i] = db.get(i);
            return array;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
    }
}
