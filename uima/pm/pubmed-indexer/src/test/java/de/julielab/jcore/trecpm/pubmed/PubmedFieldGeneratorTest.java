package de.julielab.jcore.trecpm.pubmed;

import de.julielab.jcore.consumer.es.ArrayFieldValue;
import de.julielab.jcore.consumer.es.FieldGenerationException;
import de.julielab.jcore.consumer.es.preanalyzed.Document;
import de.julielab.jcore.consumer.es.preanalyzed.RawToken;
import de.julielab.jcore.types.Header;
import de.julielab.jcore.types.Token;
import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

public class PubmedFieldGeneratorTest {
    @Test
    public void testGenerateFields() throws UIMAException, FieldGenerationException {
        final JCas jCas = JCasFactory.createJCas("de.julielab.jcore.types.jcore-all-types");
        jCas.setDocumentText("There is DNA and some tumors in there.");
        final Header header = new Header(jCas);
        header.setDocId("1");
        header.addToIndexes();
        tokenizeCas(jCas);
        final PubmedFieldGenerator fg = new PubmedFieldGenerator(null);
        final Document doc = fg.addFields(jCas, new Document());

        assertThat((ArrayFieldValue) doc.get("negativeBoosters")).extracting("tokenValue").containsExactly("dna", "tumor");
        assertThat((RawToken) doc.get("numNegativeBoosters")).extracting("tokenValue").containsExactly(2);
    }

    private void tokenizeCas(JCas jCas) {
        final Matcher matcher = Pattern.compile("[^ ]+").matcher(jCas.getDocumentText());
        while (matcher.find()) {
            new Token(jCas, matcher.start(), matcher.end()).addToIndexes();
        }
    }
}
