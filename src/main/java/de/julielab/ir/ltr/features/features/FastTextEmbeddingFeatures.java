package de.julielab.ir.ltr.features.features;

import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import de.julielab.ipc.javabridge.Options;
import de.julielab.ipc.javabridge.StdioBridge;
import de.julielab.ir.ltr.Document;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.cas.CAS;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class FastTextEmbeddingFeatures extends DocumentEmbeddingFeatures {
    private static final Logger log = LogManager.getLogger();
    public static final String FT_FEATURE_NAME = "fasttext";
    private static Deque<StdioBridge<byte[]>> activeBridges = new ArrayDeque<>();
    private final StdioBridge<byte[]> bridge;
    private Pattern tokenization = Pattern.compile("([.\\!?,'/()])");

    public FastTextEmbeddingFeatures() {
        super(FT_FEATURE_NAME);
        Options<byte[]> options = new Options(byte[].class);
        options.setExecutable("python");
        options.setExternalProgramTerminationSignal("exit");
        options.setExternalProgramReadySignal("Script is ready");
        bridge = new StdioBridge(options, "-u", "src/main/resources/python/getFastTextEmbeddingScript.py", "resources/dim300.bin");
        try {
            bridge.start();
            activeBridges.add(bridge);
            log.info("Starting a bridge to a python instance. This bridge will be open until FastTextEmbeddingFeatures#shutdown() will be called. The application will not terminate otherwise.");
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        final CAS cas = document.getCas();
        final String documentText = cas.getDocumentText();
        final double[] embedding = getDocumentEmbedding(documentText);
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
