package de.julielab.ir.ltr.features;

import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.tartarus.snowball.SnowballProgram;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.charset.StandardCharsets.UTF_8;

public class FeatureUtils {
    private static Map<Thread, FeatureUtils> instances = new ConcurrentHashMap<>();
    private final SnowballProgram englishStemmer;
    private StandardAnalyzer standardAnalyzer;

    private FeatureUtils() throws IOException {
        standardAnalyzer = new StandardAnalyzer(new InputStreamReader(FeatureUtils.class.getResourceAsStream("/data/stopwords.txt"), UTF_8));
        try {
            Class<? extends SnowballProgram> stemClass = Class.forName("org.tartarus.snowball.ext." + "English" + "Stemmer").asSubclass(SnowballProgram.class);
            englishStemmer = stemClass.getConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static FeatureUtils getInstance() {
        Thread thread = Thread.currentThread();
        FeatureUtils instance = instances.get(thread);
        try {
            if (instance == null) {
                instance = new FeatureUtils();
                instances.put(thread, instance);
            }
        } catch (IOException e) {
            throw new IllegalStateException("The stopwords file could not be read from the resource path /data/stopwords.txt");
        }
        return instance;
    }

    /**
     * <p>Normalizes the input string by
     * <ul>
     * <li>Removing stopwords on the stopword list at /data/stopwords.txt</li>
     * <li>Lowercasing the input</li>
     * <li>Applying an English Snowball Stemmer to the input.</li>
     * </ul></p>
     *
     * @param input The text to normalize.
     * @return The normalized text.
     * @throws IOException If the stopwords cannot be read.
     */
    public String normalizeString(String input) {
        if (null == input || input.isBlank())
            return "";
        try {
            final SnowballFilter sbf = new SnowballFilter(standardAnalyzer.tokenStream("none", input), englishStemmer);
            sbf.reset();
            final CharTermAttribute cta = sbf.addAttribute(CharTermAttribute.class);
            StringBuilder sb = new StringBuilder();
            while (sbf.incrementToken()) {
                sb.append(cta.buffer(), 0, cta.length()).append(' ');
            }
            sbf.end();
            sbf.close();
            sb.deleteCharAt(sb.length() - 1);
            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
