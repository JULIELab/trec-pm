
package de.julielab.jcore.reader.trecpmextra;

import de.julielab.jcore.types.AbstractText;
import de.julielab.jcore.types.Header;
import de.julielab.jcore.types.Title;
import org.apache.uima.UIMAException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for jcore-trecpm-extraabstracts-reader.
 * @author 
 *
 */
public class TRECPMExtraReaderTest{

    private final static Logger log = LoggerFactory.getLogger(TRECPMExtraReaderTest.class);

    @Test
    public void testReader() throws UIMAException, IOException {
        JCas jCas = JCasFactory.createJCas("de.julielab.jcore.types.jcore-document-meta-types",
                "de.julielab.jcore.types.jcore-document-structure-types");
        CollectionReader reader = CollectionReaderFactory.createReader("de.julielab.jcore.reader.trecpmextra.desc.jcore-trecpm-extraabstracts-reader", TRECPMExtraReader.PARAM_INPUT, "src/test/resources/test.zip");
        Set<String> docIds = new HashSet<>();
        while (reader.hasNext()) {
            reader.getNext(jCas.getCas());
            Header header;
            Title title;
            AbstractText abstractText;
            assertThat((header = JCasUtil.selectSingle(jCas, Header.class))).isNotNull();
            assertThat((title = JCasUtil.selectSingle(jCas, Title.class))).isNotNull();
            assertThat((abstractText = JCasUtil.selectSingle(jCas, AbstractText.class))).isNotNull();
            assertThat(header.getDocId()).isNotBlank();
            assertThat(header.getPubTypeList()).isNotNull();
            assertThat(header.getPubTypeList()).hasSize(1);
            assertThat(header.getPubTypeList(0).getName()).isNotBlank();
            assertThat(header.getPubTypeList(0).getPubDate()).isNotNull();
            assertThat(header.getPubTypeList(0).getPubDate().getYear()).isGreaterThan(2000);
            assertThat(header.getPubTypeList(0).getPubDate().getMonth()).isEqualTo(1);
            assertThat(header.getPubTypeList(0).getPubDate().getDay()).isEqualTo(1);
            assertThat(title.getCoveredText()).isNotBlank();
            assertThat(abstractText.getCoveredText()).isNotBlank();
            assertThat(abstractText.getCoveredText().length()).isGreaterThan(100);
            if (header != null)
                docIds.add(header.getDocId());

            jCas.reset();
        }
        assertThat(docIds).containsExactlyInAnyOrder("AACR_2012-2093", "ASCO_115244-132");
    }
}
