package de.julielab.ir;

import at.medunigraz.imi.bst.config.TrecConfig;
import com.google.common.base.Functions;
import com.ximpleware.VTDException;
import de.julielab.costosys.configuration.DBConfig;
import de.julielab.costosys.configuration.FieldConfig;
import de.julielab.costosys.dbconnection.CoStoSysConnection;
import de.julielab.costosys.dbconnection.DataBaseConnector;
import de.julielab.ir.ltr.Document;
import de.julielab.ir.ltr.DocumentList;
import de.julielab.java.utilities.FileUtilities;
import de.julielab.java.utilities.cache.CacheAccess;
import de.julielab.java.utilities.cache.CacheService;
import de.julielab.xml.JulieXMLConstants;
import de.julielab.xml.XmiBuilder;
import de.julielab.xml.XmiSplitConstants;
import de.julielab.xml.XmiSplitter;
import de.julielab.xml.binary.BinaryDecodingResult;
import de.julielab.xml.binary.BinaryJeDISNodeDecoder;
import de.julielab.xml.binary.BinaryJeDISNodeEncoder;
import de.julielab.xml.binary.BinaryXmiBuilder;
import de.julielab.xml.util.XMIBuilderException;
import org.apache.commons.io.IOUtils;
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

public class OriginalDocumentRetrieval {

    private static OriginalDocumentRetrieval instance;
    private final CacheAccess<String, String> documentTextCache;
    private final CacheAccess<String, byte[]> xmiCache;
    // private final CacheAccess<String, byte[][]> dbDataCache;
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
    private BinaryXmiBuilder binaryXmiBuilder;
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
     * We use DatabaseConnector objects to communicate with the document database. We have one DatabaseConnector
     * for each document database configuration file.
     */
    private Map<File, DataBaseConnector> dbcs = new HashMap<>();
    /**
     * This list contains fully qualified Java names of the UIMA types. For those types we want to retrieve
     * annotations from the database. This list is read from a configuration file.
     */
    private String[] annotationTypesToRetrieve;
    /**
     * The set of columns from which we want to retrieve information, besides the base document column.This list is
     * derived from the annotationTypesToRetrieve array.
     */
    private List<Map<String, String>> columnsToFetch;
    private FieldConfig tableSchema;
    private String xmiMetaSchema = "public";
    private BinaryJeDISNodeDecoder binaryJeDISNodeDecoder;
    private Map<File, DocumentDatabaseSettings> documentDatabaseSettings = new HashMap<>();

    private OriginalDocumentRetrieval() {

        try {
            // Create a CasPool.
            String[] typeSystemDescriptorNames;
            try (BufferedReader br = FileUtilities.getReaderFromFile(new File(TrecConfig.UIMA_TYPES_DESCRIPTORNAMES))) {
                typeSystemDescriptorNames = br.lines().filter(line -> !line.startsWith("#")).filter(Predicate.not(String::isBlank)).map(String::trim).toArray(String[]::new);
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
        // dbDataCache = CacheService.getInstance().getCacheAccess("uimaDBData.db", "XmiBytesCache", CacheAccess.STRING, CacheAccess.JAVA);
    }

    public static OriginalDocumentRetrieval getInstance() {
        if (instance == null) {
            instance = new OriginalDocumentRetrieval();
        }
        return instance;
    }

    /**
     * @return
     * @deprecated We no longer use a single DB connection but multiple. This is required by the fact that we train
     * on some document sets to rank another which may very well reside in different DBs.
     */
    public String getDatabaseUrl() {
        try {
            final DBConfig dbConfig = new DBConfig(IOUtils.toByteArray(new FileInputStream(TrecConfig.COSTOSYS_CONFIG)));
            return dbConfig.getUrl();
        } catch (VTDException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void initializeDatabaseConnection(File costosysConfig, String table) throws SQLException {
        DataBaseConnector dbc;
        try {
            dbc = new DataBaseConnector(costosysConfig.getAbsolutePath());
            try (BufferedReader br = FileUtilities.getReaderFromFile(new File(TrecConfig.COSTOSYS_ANNOTATIONS_LIST))) {
                annotationTypesToRetrieve = br.lines().filter(Predicate.not(String::isBlank)).map(String::trim).toArray(String[]::new);
            }
            dbcs.put(costosysConfig, dbc);
            DocumentDatabaseSettings documentDatabaseSettings = determineDataFormat(costosysConfig, table);

            boolean doGzip = documentDatabaseSettings.isDoGzip();
            boolean useBinaryFormat = documentDatabaseSettings.isUseBinaryFormat();

            columnsToFetch = new ArrayList<>();
            for (String qualifiedAnnotation : annotationTypesToRetrieve) {
                final String columnName = qualifiedAnnotation.toLowerCase().replace('.', '_').replace(':', '$');
                final Map<String, String> field = FieldConfig.createField(
                        JulieXMLConstants.NAME, columnName,
                        JulieXMLConstants.GZIP, String.valueOf(doGzip),
                        JulieXMLConstants.RETRIEVE, "true",
                        JulieXMLConstants.TYPE, doGzip || documentDatabaseSettings.isUseBinaryFormat() ? "bytea" : "xml"
                );
                columnsToFetch.add(field);
            }


            final List<Map<String, String>> primaryKeyFields = dbc.getActiveTableFieldConfiguration().getPrimaryKeyFields().collect(Collectors.toList());
            primaryKeyLength = primaryKeyFields.size();
            // The XMI schema has always the same structure: The primary key of the documents plus some fields for the actual XMI data
            // and other stuff. This is why we don't define the XMI schemas manually in costosys.xml but use this method.
            // This is just convenience.
            tableSchema = dbc.addXmiTextFieldConfiguration(primaryKeyFields, columnsToFetch, true);
            try (CoStoSysConnection ignored = dbc.obtainOrReserveConnection()) {
                namespaceMap = getNamespaceMap(dbc);
            }
            if (!useBinaryFormat)
                xmiBuilder = new XmiBuilder(namespaceMap, annotationTypesToRetrieve);
            if (useBinaryFormat) {
                binaryJeDISNodeDecoder = new BinaryJeDISNodeDecoder(Arrays.stream(annotationTypesToRetrieve).collect(Collectors.toSet()), false);
                binaryXmiBuilder = new BinaryXmiBuilder(namespaceMap);
                documentDatabaseSettings.setFeaturesToMapBinaryFromDb(getFeaturesToMapBinaryFromDb(dbc));
                documentDatabaseSettings.setReverseBinaryMappingFromDb(getReverseBinaryMappingFromDb(dbc));
            }
        } catch (IOException e) {
            log.error("Could not create the OriginalDocumentRetrieval because the DBC configuration {} could not be read", costosysConfig, e);
        }

    }

    public Stream<String> getDocumentText(DocumentList<?> documents, String table) {
        setXmiCasDataToDocuments(documents, null, table);
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
    private Iterator<byte[][]> retrieveXmiDataFromDB(File costosysConfig, List<Object[]> ids, String table) throws SQLException {
        if (!dbcs.containsKey(costosysConfig))
            initializeDatabaseConnection(costosysConfig, table);
        DataBaseConnector dbc = dbcs.get(costosysConfig);
        try {
            log.debug("Requesting {} documents from the database configured in the configuration file at {}", ids.size(), costosysConfig);
            return dbc.retrieveColumnsByTableSchema(ids, table, tableSchema.getName());
        } finally {
            log.debug("Returned iterator to database documents.");
        }
    }

    private Map<String, String> getNamespaceMap(DataBaseConnector dbc) {
        Map<String, String> map = null;
        if (dbc.tableExists(xmiMetaSchema + "." + XmiSplitConstants.XMI_NS_TABLE)) {
            try (CoStoSysConnection conn = dbc.obtainOrReserveConnection()) {
                map = new HashMap<>();
                conn.setAutoCommit(true);
                Statement stmt = conn.createStatement();
                String sql = String.format("SELECT %s,%s FROM %s", XmiSplitConstants.PREFIX, XmiSplitConstants.NS_URI,
                        xmiMetaSchema + "." + XmiSplitConstants.XMI_NS_TABLE);
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

    private Map<Integer, String> getReverseBinaryMappingFromDb(DataBaseConnector dbc) {
        Map<Integer, String> map = null;
        final String mappingTableName = xmiMetaSchema + "." + XmiSplitConstants.BINARY_MAPPING_TABLE;
        if (dbc.tableExists(mappingTableName)) {
            try (CoStoSysConnection conn = dbc.obtainOrReserveConnection()) {
                map = new HashMap<>();
                conn.setAutoCommit(true);
                Statement stmt = conn.createStatement();
                String sql = String.format("SELECT %s,%s FROM %s", XmiSplitConstants.BINARY_MAPPING_COL_ID, XmiSplitConstants.BINARY_MAPPING_COL_STRING,
                        mappingTableName);
                ResultSet rs = stmt.executeQuery(String.format(sql));
                while (rs.next())
                    map.put(rs.getInt(1), rs.getString(2));
            } catch (SQLException e) {
                e.printStackTrace();
                SQLException ne = e.getNextException();
                if (null != ne)
                    ne.printStackTrace();
            }
        } else {
            log.warn(
                    "JeDIS XMI annotation module meta table \"{}\" was not found. It is assumed that the table from which is read contains complete XMI documents.",
                    xmiMetaSchema + "." + XmiSplitConstants.BINARY_MAPPING_TABLE);
        }
        return map;
    }

    private Map<String, Boolean> getFeaturesToMapBinaryFromDb(DataBaseConnector dbc) {
        Map<String, Boolean> map = null;
        final String mappingTableName = xmiMetaSchema + "." + XmiSplitConstants.BINARY_FEATURES_TO_MAP_TABLE;
        if (dbc.tableExists(mappingTableName)) {
            try (CoStoSysConnection conn = dbc.obtainOrReserveConnection()) {
                map = new HashMap<>();
                conn.setAutoCommit(true);
                Statement stmt = conn.createStatement();
                String sql = String.format("SELECT %s,%s FROM %s", XmiSplitConstants.BINARY_FEATURES_TO_MAP_COL_FEATURE, XmiSplitConstants.BINARY_FEATURES_TO_MAP_COL_MAP,
                        mappingTableName);
                ResultSet rs = stmt.executeQuery(String.format(sql));
                while (rs.next())
                    map.put(rs.getString(1), rs.getBoolean(2));
            } catch (SQLException e) {
                e.printStackTrace();
                SQLException ne = e.getNextException();
                if (null != ne)
                    ne.printStackTrace();
            }
        } else {
            log.warn(
                    "JeDIS XMI annotation module meta table \"{}\" was not found. It is assumed that the table from which is read contains complete XMI documents.",
                    xmiMetaSchema + "." + XmiSplitConstants.BINARY_FEATURES_TO_MAP_TABLE);
        }
        return map;
    }

    public CAS parseXmiDataIntoJCas(byte[] xmiData) {
        try {
            final CAS cas = casPool.getCas(300000);
            // In some error cases the data is not available. Just return the empty CAS.
            if (xmiData != null && xmiData.length > 0)
                XmiCasDeserializer.deserialize(new ByteArrayInputStream(xmiData), cas);
            else
                cas.setDocumentText("");
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
     * @param documents     The documents to populate with the UIMA XMI CAS data.
     * @param dbConnections The canonical file paths pointing to the available database connections to get the
     *                      XMI data of the <code>documents</code> from.
     * @param table         The JeDIS data table name containing the document data. Must be the same table for all
     *                      database connections specified in <code>dbConnections</code>.
     */
    public void setXmiCasDataToDocuments(DocumentList<?> documents, List<File> dbConnections, String table) {
        List<? extends Document<?>> docsWithoutXmiData = documents.stream().filter(d -> d.getFullDocumentData() == null).collect(Collectors.toList());
        Set<? extends Document<?>> docsWithoutXmiDataNotInCache = docsWithoutXmiData.stream()
                .filter(d -> xmiCache.get(d.getId()) == null)
                .collect(Collectors.toSet());
        // For group the documents to be retrieved by the database they reside in. In this way we can fetch the documents
        // from one database batch-wise.
        Map<File, List<Document<?>>> docsByDbConfig = docsWithoutXmiDataNotInCache.stream()
                .filter(d -> d.getDocumentDbConfiguration() != null)
                .collect(Collectors.groupingBy(Document::getDocumentDbConfiguration));
//                .docsByDbConfig(Collectors.groupingBy(d -> {
//            if (d.getDocumentDbConfiguration() == null && (dbConnections.isEmpty() || dbConnections == null))
//                throw new IllegalStateException("The XMI data for document with ID " + d.getId() + " should be fetched but it specifies no document database connection configuration and no database connection files have been passed to this method.");
//            return d.getDocumentDbConfiguration();
//        }));

        List<Document<?>> docsWithoutDbConfig = docsWithoutXmiDataNotInCache.stream()
                .filter(d -> d.getDocumentDbConfiguration() == null)
                .collect(Collectors.toList());

        if (!docsWithoutDbConfig.isEmpty() && (dbConnections == null || dbConnections.isEmpty())) {
            throw new IllegalStateException("The XMI data for " + docsWithoutDbConfig.size() + " documents must be fetched from a database but the respective " +
                    "documents do not specify a database connection and no DB connections are passed to the method as " +
                    "an argument.");
        } else if (dbConnections != null && !dbConnections.isEmpty()) {
            for (File dbConnectionFile : dbConnections) {
                docsByDbConfig.put(dbConnectionFile, docsWithoutDbConfig);
            }
        }

        for (File costosysConfig : docsByDbConfig.keySet()) {
            List<Object[]> docsForCurrentDbConfig = docsByDbConfig.get(costosysConfig).stream().map(d -> new Object[]{d.getId()}).distinct().collect(Collectors.toList());
//            log.debug("Got {} documents whose XMI data is not present in the cache. Fetching them from the database.", docsForCurrentDbConfig.size());
//            Iterator<Object[]> it = docsForCurrentDbConfig.iterator();
//            while (it.hasNext()) {
//                Object[] key = it.next();
//                String id = (String) key[0];
//                byte[][] bytes = dbDataCache.get(id);
//                if (bytes != null) {
//                    dataByDocId.put(id, bytes);
//                    it.remove();
//                }
//            }
            Map<String, byte[][]> dataByDocId = new HashMap<>();
            try {
                if (!docsForCurrentDbConfig.isEmpty()) {
                    final Iterator<byte[][]> xmiData = retrieveXmiDataFromDB(costosysConfig, docsForCurrentDbConfig, table);
                    int i = 0;
                    while (xmiData.hasNext()) {
                        byte[][] data = xmiData.next();
                        String id = new String(data[0], UTF_8);
                        dataByDocId.put(id, data);
                        // dbDataCache.put(id, data);
                        ++i;
                        double reportFraction = 0.1;
                        if (i % (docsForCurrentDbConfig.size() * reportFraction) == 0)
                            log.debug("Retrieved {}% of the documents from the database.", i / (docsForCurrentDbConfig.size() * reportFraction) * 10);
                    }
                    log.debug("Retrieved {} documents from the database", i);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }


            final CAS cas = casPool.getCas(300000);
            try {
                Iterator<? extends Document<?>> docsWithoutXmiDataIt = docsWithoutXmiData.iterator();
                log.debug("Assembling the actual XMI documents from the database data.");
                while (docsWithoutXmiDataIt.hasNext()) {
                    final Document<?> doc = docsWithoutXmiDataIt.next();
                    docsWithoutXmiDataIt.remove();
                    byte[] docXmiData = xmiCache.get(doc.getId());
                    if (docXmiData == null) {
                        if (!dataByDocId.containsKey(doc.getId())) {
                            String requested = documents.stream().map(Document::getId).collect(Collectors.toSet()).contains(doc.getId()) ? "" : "not ";
                           // log.warn("The document with ID " + doc.getId() + " is not contained in the cache and was not returned from a database.");
                            continue;
                        }
                        final byte[][] documentData = dataByDocId.get(doc.getId());
                        LinkedHashMap<String, InputStream> dataMap = new LinkedHashMap<>();
                        List<String> xmiColumnNames = new ArrayList<>();
                        xmiColumnNames.add(XmiSplitConstants.BASE_DOC_COLUMN);
                        columnsToFetch.stream().map(m -> m.get(JulieXMLConstants.NAME)).forEach(xmiColumnNames::add);
                        for (int i = 0; i < xmiColumnNames.size(); i++) {
                            String columnName = xmiColumnNames.get(i);
                            // Here we need to calculate with the offset of the primary key elements that is contained
                            // in the xmiData byte[][] structure: The first positions of the array are just the primary
                            // key elements.
                            if (documentData[i + primaryKeyLength] == null)
                                throw new IllegalStateException("There is no data in table " + columnName + " for document with ID " + doc.getId() + ". In the current version of the framework, all documents should have data in all tables.");
                            String xmiModuleKey = columnName;
                            if (xmiModuleKey.equals(XmiSplitConstants.BASE_DOC_COLUMN))
                                xmiModuleKey = XmiSplitter.DOCUMENT_MODULE_LABEL;
                            dataMap.put(xmiModuleKey, new ByteArrayInputStream(documentData[i + primaryKeyLength]));
                        }
                        ByteArrayOutputStream xmiBaos;
                        DocumentDatabaseSettings documentDatabaseSettings = this.documentDatabaseSettings.get(costosysConfig);
                        if (documentDatabaseSettings.isUseBinaryFormat()) {
                            BinaryDecodingResult decode = binaryJeDISNodeDecoder.decode(dataMap, cas.getTypeSystem(), documentDatabaseSettings.getReverseBinaryMappingFromDb(), documentDatabaseSettings.getFeaturesToMapBinaryFromDb(), namespaceMap);
                            xmiBaos = binaryXmiBuilder.buildXmi(decode);
                        } else {
                            xmiBaos = xmiBuilder.buildXmi(dataMap, cas.getTypeSystem());
                        }
                        docXmiData = xmiBaos.toByteArray();
                        xmiCache.put(doc.getId(), docXmiData);
                    }
                    doc.setFullDocumentData(docXmiData);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XMIBuilderException e) {
                e.printStackTrace();
            }
            log.debug("Setting the XMI data to {} documents has been finished.", docsForCurrentDbConfig.size());
            releaseCas(cas);
        }
        for (Document<?> d : documents) {
            if (d.getFullDocumentData() == null)
                log.warn("Document with ID {} did not get its XMI data set. The document specified the following " +
                        "database connection: {}. If it is null, the following database connections are passed to search " +
                        "for the document: {}. In none of those the document was found.", d.getId(), d.getDocumentDbConfiguration(), dbConnections);
        }
    }

    public void shutdown() {
        log.info("Shutting down {}", getClass().getSimpleName());
        for (DataBaseConnector dbc : dbcs.values())
            dbc.close();
    }

    private DocumentDatabaseSettings determineDataFormat(File costosysConfig, String table) throws SQLException {
        boolean doGzip = true;
        boolean useBinaryFormat = false;
        log.debug("Fetching a single row from data table {} in order to determine whether data is in GZIP format", table);
        DataBaseConnector dbc = dbcs.get(costosysConfig);
        try (CoStoSysConnection conn = dbc.obtainOrReserveConnection()) {
            final String documentColumnName = XmiSplitConstants.BASE_DOC_COLUMN;
            ResultSet rs = conn.createStatement().executeQuery(String.format("SELECT %s FROM %s LIMIT 1", documentColumnName, table));
            while (rs.next()) {
                byte[] xmiData = rs.getBytes(documentColumnName);
                try (GZIPInputStream gzis = new GZIPInputStream(new ByteArrayInputStream(xmiData))) {
                    byte[] firstTwoBytes = new byte[2];
                    gzis.read(firstTwoBytes);
                    useBinaryFormat = checkForJeDISBinaryFormat(firstTwoBytes);
                } catch (IOException e) {
                    log.debug("Attempt to read XMI data in GZIP format failed. Assuming non-gzipped XMI data.");
                    doGzip = false;
                    useBinaryFormat = checkForJeDISBinaryFormat(xmiData);
                }
            }
            DocumentDatabaseSettings settings = new DocumentDatabaseSettings(doGzip, useBinaryFormat);
            documentDatabaseSettings.put(costosysConfig, settings);
            return settings;
        } catch (SQLException e) {
            if (e.getMessage().contains("does not exist"))
                log.error("An exception occurred when trying to read the xmi column of the data table \"{}\". It seems the table does not contain XMI data and this is invalid to use with this reader.", table);
            throw e;
        }
    }

    private boolean checkForJeDISBinaryFormat(byte[] firstTwoBytes) {
        short header = (short) ((firstTwoBytes[0] << 8) | (0xff & firstTwoBytes[1]));
        boolean useBinaryFormat = false;
        if (header != BinaryJeDISNodeEncoder.JEDIS_BINARY_MAGIC) {
            log.debug("Is data encoded in JeDIS binary format: false");
        } else {
            useBinaryFormat = true;
            log.debug("Is data encoded in JeDIS binary format: true");
        }
        return useBinaryFormat;
    }

    private class DocumentDatabaseSettings {
        private boolean doGzip;
        private boolean useBinaryFormat;
        private Map<String, Boolean> featuresToMapBinaryFromDb;
        private Map<Integer, String> reverseBinaryMappingFromDb;

        public DocumentDatabaseSettings(boolean doGzip, boolean useBinaryFormat) {
            this.doGzip = doGzip;
            this.useBinaryFormat = useBinaryFormat;
        }

        public Map<String, Boolean> getFeaturesToMapBinaryFromDb() {
            return featuresToMapBinaryFromDb;
        }

        public void setFeaturesToMapBinaryFromDb(Map<String, Boolean> featuresToMapBinaryFromDb) {
            this.featuresToMapBinaryFromDb = featuresToMapBinaryFromDb;
        }

        public Map<Integer, String> getReverseBinaryMappingFromDb() {
            return reverseBinaryMappingFromDb;
        }

        public void setReverseBinaryMappingFromDb(Map<Integer, String> reverseBinaryMappingFromDb) {
            this.reverseBinaryMappingFromDb = reverseBinaryMappingFromDb;
        }

        public boolean isDoGzip() {
            return doGzip;
        }

        public boolean isUseBinaryFormat() {
            return useBinaryFormat;
        }
    }
}
