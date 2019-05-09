//package de.julielab.ir.cache;
//
//import at.medunigraz.imi.bst.config.TrecConfig;
//import org.mapdb.DB;
//import org.mapdb.DBMaker;
//import org.springframework.cache.Cache;
//
//import java.io.File;
//import java.util.Map;
//
//public class CacheService {
//    private static CacheService service;
//    private Map<String, DB> dbs;
//
//    private CacheService() {
//        File cacheDir = new File("cache/map.db");
//        final DBMaker.Maker dbmaker = DBMaker
//                .fileDB(cacheDir.getAbsolutePath())
//                .fileMmapEnable()
//                .transactionEnable()
//                .closeOnJvmShutdown();
//        if (TrecConfig.DOCUMENT_DB_CACHE_READ_ONLY && cacheDir.exists())
//            dbmaker.readOnly();
//    }
//
//    public static CacheService getInstance() {
//        if (service == null)
//            service = new CacheService();
//        return service;
//    }
//
//    public DB getFiledb(String file) {
//        return filedb;
//    }
//}
