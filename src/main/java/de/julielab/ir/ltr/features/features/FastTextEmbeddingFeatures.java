package de.julielab.ir.ltr.features.features;

import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import de.julielab.ipc.javabridge.Options;
import de.julielab.ipc.javabridge.StdioBridge;
import de.julielab.ir.cache.CacheAccess;
import de.julielab.ir.cache.CacheService;
import de.julielab.ir.ltr.Document;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.cas.CAS;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class FastTextEmbeddingFeatures extends DocumentEmbeddingFeatures {
    public static final String FT_FEATURE_NAME = "fasttext";
    private static final Logger log = LogManager.getLogger();
    private static Deque<StdioBridge<byte[]>> activeBridges = new ArrayDeque<>();
    private final StdioBridge<byte[]> bridge;
    private Pattern tokenization = Pattern.compile("([.\\!?,'/()])");
    private String EMBEDDING_PATH = "resources/dim300.bin";
    private CacheAccess<String, double[]> cache;

    public FastTextEmbeddingFeatures() {
        super(FT_FEATURE_NAME);
        Options<byte[]> options = new Options(byte[].class);
        options.setExecutable("python3");
        options.setExternalProgramTerminationSignal("exit");
        options.setExternalProgramReadySignal("Script is ready");
        bridge = new StdioBridge(options, "-u", "src/main/resources/python/getFastTextEmbeddingScript.py", EMBEDDING_PATH);
        try {
            bridge.start();
            activeBridges.add(bridge);
            log.info("Starting a bridge to a python instance. This bridge will be open until FastTextEmbeddingFeatures#shutdown() will be called. The application will not terminate otherwise.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        cache = CacheService.getInstance().getCacheAccess("Embeddings", "FastTextDocs", CacheAccess.STRING, CacheAccess.DOUBLEARRAY);
    }

    public static void shutdown() {
        try {
            while (!activeBridges.isEmpty()) {
                activeBridges.removeLast().stop();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void assignDocumentEmbeddingFeatures(Instance inst) {
        final Document document = (Document) inst.getSource();
        final String cachekey = EMBEDDING_PATH + "-" + document.getId();
        double[] embedding = cache.get(cachekey);
        if (embedding == null) {
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
        try {
            // Do some simple normalization, actually inspired by some sed and tr command example from the FastText text classification tutorial
            String tokenizedDocumentText = tokenization.matcher(documentText).replaceAll(" $1 ");
            tokenizedDocumentText = tokenizedDocumentText.toLowerCase();
            Stream<byte[]> response = bridge.sendAndReceive(tokenizedDocumentText);
            byte[] bytes = response.findAny().get();
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
}
