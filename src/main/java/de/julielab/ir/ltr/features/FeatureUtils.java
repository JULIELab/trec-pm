package de.julielab.ir.ltr.features;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.io.InputStreamReader;

import static java.nio.charset.StandardCharsets.UTF_8;

public class FeatureUtils {
    private static FeatureUtils instance;
    private StandardAnalyzer standardAnalyzer;

    private FeatureUtils() throws IOException {
        standardAnalyzer = new StandardAnalyzer(new InputStreamReader(FeatureUtils.class.getResourceAsStream("/data/stopwords.txt"), UTF_8));
    }

    public static FeatureUtils getInstance() {
        try {
            if (instance == null)
                instance = new FeatureUtils();
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
    public String normalizeString(String input)  {
        try {
            final TokenStream ts = standardAnalyzer.tokenStream("none", input);
            final SnowballFilter sbf = new SnowballFilter(ts, "English");
            sbf.reset();
            final CharTermAttribute cta = sbf.addAttribute(CharTermAttribute.class);
            StringBuilder sb = new StringBuilder();
            while (sbf.incrementToken()) {
                sb.append(cta.buffer(), 0, cta.length()).append(' ');
            }
            sb.deleteCharAt(sb.length() - 1);
            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
