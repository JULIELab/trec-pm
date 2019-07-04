package de.julielab.ir;

import at.medunigraz.imi.bst.config.TrecConfig;
import de.julielab.ir.cache.CacheAccess;
import de.julielab.ir.cache.CacheService;
import de.julielab.ir.ltr.Document;
import de.julielab.ir.ltr.DocumentList;
import de.julielab.java.utilities.FileUtilities;
import de.julielab.xml.XmiBuilder;
import de.julielab.xml.XmiSplitConstants;
import de.julielab.xmlData.dataBase.CoStoSysConnection;
import de.julielab.xmlData.dataBase.DataBaseConnector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.impl.ResourceManager_impl;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.resource.metadata.impl.ProcessingResourceMetaData_impl;
import org.apache.uima.util.CasPool;
import org.xml.sax.SAXException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OriginalDocumentRetrieval {

    private static OriginalDocumentRetrieval instance;
    private final CacheAccess<String, String> documentTextCache;
    private final CacheAccess<String, byte[]> xmiCache;
    /**
     * This map contains the XMI namespace definitions required to rebuild valid XMI documents from the
     * XMI elements we will retrieve from the database. The base document and the annotations are all stored
     * without this information which is why it has to be added when assembling the final XMI document.
     */
    private Map<String, String> namespaceMap;
    /**
     * This object takes the XMI document parts: The base document and possibly annotations retrieved additionally
     * from the database. It also makes takes the namespace map to then assembly complete XMI documents.
     */
    private XmiBuilder xmiBuilder;
    /**
     * The length of the primary key of the original documents. Used to determine the correct locations in the
     * retrieved data for the keys, the document and the annotation data.
     */
    private int primaryKeyLength;
    /**
     * CAS objects are expensive to create, especially in terms of memory, but also in CPU time. Reusing them
     * is much more appropriate which is why we use a CasPool.
     */
    private CasPool casPool;
    private Logger log = LogManager.getLogger();
    /**
     * The object that offers convenience methods to retrieve documents from a Postgres database.
     */
    private DataBaseConnector dbc;
    /**
     * This list contains fully qualified Java names of the UIMA types. For those types we want to retrieve
     * annotations from the database. This list is read from a configuration file.
     */
    private String[] annotationTypesToRetrieve;
    /**
     * The set of tables from which we want to retrieve information. That is the table with the base document (document
     * text plus document meta data and structures) plus any annotation tables we want to use. This array is
     * derived from the annotationTypesToRetrieve array.
     */
    private String[] tablesToJoin;
    /**
     * This is a parallel array to tablesToJoin and lists the database table schemas, as defined in the DBC,
     * to be used when retrieving data from the respective tables.
     */
    private String[] schemaNames;

    private OriginalDocumentRetrieval() {

        try {
            // Create a CasPool.
            String[] typeSystemDescriptorNames;
            try (BufferedReader br = FileUtilities.getReaderFromFile(new File(TrecConfig.UIMA_TYPES_DESCRIPTORNAMES))) {
                typeSystemDescriptorNames = br.lines().filter(Predicate.not(String::isBlank)).map(String::trim).toArray(String[]::new);
            }
            final TypeSystemDescription tsDesc = TypeSystemDescriptionFactory.createTypeSystemDescription(typeSystemDescriptorNames);
            final ProcessingResourceMetaData_impl metaData = new ProcessingResourceMetaData_impl();
            metaData.setTypeSystem(tsDesc);
            try {
                casPool = new CasPool(10, metaData, new ResourceManager_impl());
            } catch (ResourceInitializationException e) {
                log.error("Could not create CAS pool", e);
            }
        } catch (IOException e) {
            log.error("The CAS pool could not be created because the file with the UIMA types to load could not be read", e);
        }


        documentTextCache = CacheService.getInstance().getCacheAccess("uimaDocText.db", "UIMACasDocumentTextCache", CacheAccess.STRING, CacheAccess.STRING);
        xmiCache = CacheService.getInstance().getCacheAccess("uimaDocText.db", "UIMACasXMICache", CacheAccess.STRING, CacheAccess.BYTEARRAY);
    }

    public static OriginalDocumentRetrieval getInstance() {
        if (instance == null) {
            instance = new OriginalDocumentRetrieval();
        }
        return instance;
    }

    private void initializeDatabaseConnection() {
        try {
            dbc = new DataBaseConnector(TrecConfig.COSTOSYS_CONFIG);
            try (BufferedReader br = FileUtilities.getReaderFromFile(new File(TrecConfig.COSTOSYS_ANNOTATIONS_LIST))) {
                annotationTypesToRetrieve = br.lines().filter(Predicate.not(String::isBlank)).map(String::trim).toArray(String[]::new);
            }
        } catch (IOException e) {
            log.error("Could not create the OriginalDocumentRetrieval because the DBC configuration could not be read", e);
        }
        // This looks more complicated as it is. We just:
        // 1. Add the base document table be the first stream
        // 2. Add the the annotation tables by converting the Java names of the annotation types into their
        //    table names and prepend the active Postgres data schema. That the tables will be found there is
        //    actually just a convention and rather unflexible. Perhaps we will need to make this configurable in
        //    the future.
        // 3. Now we have the list of schema-qualified tables to query from the database.
        tablesToJoin = Stream.concat(Stream.of(TrecConfig.COSTOSYS_BASEDOCUMENTS),
                Stream.of(annotationTypesToRetrieve)
                        .map(type -> type.replaceAll("\\.", "_").toLowerCase())
                        .map(table -> dbc.getActiveDataPGSchema() + "." + table))
                .toArray(String[]::new);
        final List<Map<String, String>> primaryKeyFields = dbc.getActiveTableFieldConfiguration().getPrimaryKeyFields().collect(Collectors.toList());
        primaryKeyLength = primaryKeyFields.size();
        // The XMI schema has always the same structure: The primary key of the documents plus some fields for the actual XMI data
        // and other stuff. This is why we don't define the XMI schemas manually in costosys.xml but use this method.
        // This is just convenience.
        final String baseDocumentTableSchema = dbc.addXmiDocumentFieldConfiguration(primaryKeyFields, false).getName();
        final String annotationTableSchema = dbc.addXmiAnnotationFieldConfiguration(primaryKeyFields, false).getName();
        schemaNames = new String[tablesToJoin.length];
        schemaNames[0] = baseDocumentTableSchema;
        for (int i = 1; i < schemaNames.length; i++) {
            schemaNames[i] = annotationTableSchema;
        }
        try (CoStoSysConnection ignored = dbc.obtainOrReserveConnection()) {
            namespaceMap = getNamespaceMap();
        }
        xmiBuilder = new XmiBuilder(namespaceMap, annotationTypesToRetrieve);
    }

    public Stream<String> getDocumentText(DocumentList<?> documents) {
        setXmiCasDataToDocuments(documents);
        return documents.stream().map(d -> {
            String text = documentTextCache.get(d.getId());
            if (text == null) {
                final CAS cas = parseXmiDataIntoJCas(d.getFullDocumentData());
                text = cas.getDocumentText();
                documentTextCache.put(d.getId(), text);
                releaseCas(cas);
            }
            return text;
        });
    }

    /**
     * Releases the given CAS back into the CAS pool.
     *
     * @param cas The CAS to be released for reuse.
     */
    public void releaseCas(CAS cas) {
        casPool.releaseCas(cas);
    }

    /**
     * Retrieves CAS data from the Postgres database for the given document IDs.
     *
     * @param ids The IDs of the documents to retrieve from the database.
     * @return An iterator for the retrieved document data.
     */
    Iterator<byte[][]> getDocuments(List<Object[]> ids) {
        if (dbc == null)
            initializeDatabaseConnection();
        return dbc.retrieveColumnsByTableSchema(ids, tablesToJoin, schemaNames);
    }

    private Map<String, String> getNamespaceMap() {
        Map<String, String> map = null;
        if (dbc.tableExists(dbc.getActiveDataPGSchema() + "." + XmiSplitConstants.XMI_NS_TABLE)) {
            try (CoStoSysConnection conn = dbc.obtainOrReserveConnection()) {
                map = new HashMap<>();
                conn.setAutoCommit(true);
                Statement stmt = conn.createStatement();
                String sql = String.format("SELECT %s,%s FROM %s", XmiSplitConstants.PREFIX, XmiSplitConstants.NS_URI,
                        dbc.getActiveDataPGSchema() + "." + XmiSplitConstants.XMI_NS_TABLE);
                ResultSet rs = stmt.executeQuery(String.format(sql));
                while (rs.next())
                    map.put(rs.getString(1), rs.getString(2));
            } catch (SQLException e) {
                e.printStackTrace();
                SQLException ne = e.getNextException();
                if (null != ne)
                    ne.printStackTrace();
            }
        } else {
            log.warn(
                    "Table \"{}\" was not found it is assumed that the table from which is read contains complete XMI documents.",
                    dbc.getActiveDataPGSchema() + "." + XmiSplitConstants.XMI_NS_TABLE);
        }
        return map;
    }

    public CAS parseXmiDataIntoJCas(byte[] xmiData) {
        try {
            final CAS cas = casPool.getCas(300000);
            XmiCasDeserializer.deserialize(new ByteArrayInputStream(xmiData), cas);
            return cas;
        } catch (SAXException | IOException e) {
            log.error("Could not deserialize XMI data into a CAS", e);
        }
        return null;
    }

    /**
     * Retrieves the XMI data from the database for the document IDs of the passed documents. This methods skips
     * documents that already have their full document data set.
     *
     * @param documents The documents to populate with the UIMA XMI CAS data.
     */
    public void setXmiCasDataToDocuments(DocumentList<?> documents) {
        final List<Object[]> documentIDs = documents.stream().filter(d -> d.getFullDocumentData() == null).filter(d -> xmiCache.get(d.getId()) == null).map(d -> new String[]{d.getId()}).collect(Collectors.toList());


        Map<String, byte[][]> dataByDocId = new HashMap<>();
        if (!documentIDs.isEmpty()) {
            final Iterator<byte[][]> xmiData = getDocuments(documentIDs);
            while (xmiData.hasNext()) {
                byte[][] data = xmiData.next();
                String id = new String(data[0], StandardCharsets.UTF_8);
                dataByDocId.put(id, data);
            }
        }

        final CAS cas = casPool.getCas(300000);
        final Iterator<? extends Document<?>> docsIt = documents.stream().filter(d -> d.getFullDocumentData() == null).iterator();
        while (docsIt.hasNext()) {
            final Document doc = docsIt.next();
            byte[] docXmiData = xmiCache.get(doc.getId());
            if (docXmiData == null) {
                if (!dataByDocId.containsKey(doc.getId()))
                    throw new IllegalStateException("The document with ID " + doc.getId() + " was not returned from the database.");
                final byte[][] documentData = dataByDocId.get(doc.getId());
                LinkedHashMap<String, InputStream> dataMap = new LinkedHashMap<>();
                for (int i = 0; i < tablesToJoin.length; i++) {
                    // Here we need to calculate with the offset of the primary key elements that is contained
                    // in the xmiData byte[][] structure: The first positions of the array are just the primary
                    // key elements.
                    if (documentData[i + primaryKeyLength] == null)
                        throw new IllegalStateException("There is no data in table " + tablesToJoin[i] + " for document with ID " + doc.getId() + ". In the current version of the framework, all documents should have data in all tables.");
                    dataMap.put(tablesToJoin[i], new ByteArrayInputStream(documentData[i + primaryKeyLength]));
                }
                final ByteArrayOutputStream xmiBaos = xmiBuilder.buildXmi(dataMap, TrecConfig.COSTOSYS_BASEDOCUMENTS, cas.getTypeSystem());
                docXmiData = xmiBaos.toByteArray();
                xmiCache.put(doc.getId(), docXmiData);
            }
            doc.setFullDocumentData(docXmiData);
        }
        releaseCas(cas);
    }
}
