package de.julielab.ir;

import at.medunigraz.imi.bst.trec.utils.ConnectionUtils;
import de.julielab.ir.ltr.Document;
import de.julielab.ir.ltr.DocumentList;
import de.julielab.ir.model.QueryDescription;
import de.julielab.jcore.types.AutoDescriptor;
import de.julielab.jcore.types.pubmed.Header;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


// Ignore when the database is there but not set up correctly
@Ignore
public class OriginalDocumentRetrievalTest {

    public OriginalDocumentRetrievalTest() throws FileNotFoundException, MalformedURLException {
        Assume.assumeTrue(ConnectionUtils.checkPostgresOpenPort());
    }

    @Test
    public void getSingleDocument() throws CASException {
        final OriginalDocumentRetrieval retrieval = OriginalDocumentRetrieval.getInstance();
        final Document<QueryDescription> document = new Document<>();
        document.setId("10065107");
        final DocumentList<QueryDescription> dl = new DocumentList<>();
        dl.add(document);
        retrieval.setXmiCasDataToDocuments(dl,null, "_data_xmi.documents");
        final JCas cas = retrieval.parseXmiDataIntoJCas(document.getFullDocumentData()).getJCas();
        final Header header = JCasUtil.selectSingle(cas, Header.class);
        assertNotNull(header);
        assertEquals("10065107", header.getDocId());
        final AutoDescriptor ad = JCasUtil.selectSingle(cas, AutoDescriptor.class);
        assertNotNull(ad);
        assertNotNull(ad.getDocumentClasses(0));
    }
}