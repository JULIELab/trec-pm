package de.julielab.ir;

import at.medunigraz.imi.bst.trec.utils.ConnectionUtils;
import de.julielab.jcore.types.AutoDescriptor;
import de.julielab.jcore.types.pubmed.Header;
import org.apache.uima.UIMAException;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.junit.Assume;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

public class OriginalDocumentRetrievalTest {

    public OriginalDocumentRetrievalTest() throws FileNotFoundException, MalformedURLException {
        Assume.assumeTrue(ConnectionUtils.checkPostgresOpenPort());
    }

    @Test
    public void getSingleDocument() throws CASException {
        final OriginalDocumentRetrieval retrieval = OriginalDocumentRetrieval.getInstance();
        Object[] id = {"10065107"};
        final Iterator<byte[][]> documents = retrieval.getDocuments(Arrays.<Object[]>asList(id));
        assertTrue(documents.hasNext());
        final JCas cas = retrieval.parseXmiDataIntoJCas(documents.next()).getJCas();
        final Header header = JCasUtil.selectSingle(cas, Header.class);
        assertNotNull(header);
        assertEquals("10065107", header.getDocId());
        final AutoDescriptor ad = JCasUtil.selectSingle(cas, AutoDescriptor.class);
        assertNotNull(ad);
        assertNotNull(ad.getDocumentClasses(0));
    }
}