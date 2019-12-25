package at.medunigraz.imi.bst.config;

import java.util.ResourceBundle;

public final class TrecConfig {
    public static final String MEDLINE_FILE_PATTERN = "medline17n0%03d.xml";
    public static final int MEDLINE_FILE_END = 889;
    public static final String SAMPLE_SMALL_XML = "src/main/resources/data/medline-sample.xml";
    public static final String SAMPLE_EXTRA_ABSTRACT_TXT = "src/main/resources/data/extra-abstract-sample.txt";
    private static final ResourceBundle PROPERTIES = ResourceBundle.getBundle("config");
    public static final String ELASTIC_BA_INDEX = getString("ELASTIC_BA_INDEX");
    public static final String ELASTIC_BA_MEDLINE_TYPE = getString("ELASTIC_BA_MEDLINE_TYPE");
    public static final String ELASTIC_BA_EXTRA_TYPE = getString("ELASTIC_BA_EXTRA_TYPE");
    public static final String ELASTIC_CT_INDEX = getString("ELASTIC_CT_INDEX");
    public static final String ELASTIC_CT_TYPE = getString("ELASTIC_CT_TYPE");
    public static final String ELASTIC_HOSTNAME = getString("ELASTIC_HOSTNAME");
    public static final int ELASTIC_PORT = getInteger("ELASTIC_PORT");
    public static final String ELASTIC_CLUSTER = getString("ELASTIC_CLUSTER");
    public static final String LEXIGRAM_APIKEY = getString("LEXIGRAM_APIKEY");
    public static final String COSTOSYS_CONFIG = getString("COSTOSYS_CONFIG");
    public static final String COSTOSYS_ANNOTATIONS_LIST = getString("COSTOSYS_ANNOTATIONS_LIST");
    public static final String UIMA_TYPES_DESCRIPTORNAMES = getString("UIMA_TYPES_DESCRIPTORNAMES");
    public static final boolean CACHE_READ_ONLY = getBoolean("CACHE_READ_ONLY");
    public static final String CACHE_DIR = getString("CACHE_DIR");
    public static final String CACHE_TYPE = getString("CACHE_TYPE");
    public static final String CACHE_HOST = getString("CACHE_HOST");
    public static final int CACHE_PORT = getInteger("CACHE_PORT");
    public static final String GSHEETS_SHEETID = getString("GSHEETS_SHEETID");
    public static final int CONCURRENCY_MAX = getInteger("CONCURRENCY_MAX");
    public static final int SIZE = 1000;
    public static final int MAX_TREATMENTS = 3;
    public static String SUBTEMPLATES_FOLDER = getString("SUBTEMPLATES_FOLDER");

    public static String getString(String key) {
        return PROPERTIES.getString(key);
    }

    public static boolean getBoolean(String key) {
        return Boolean.valueOf(PROPERTIES.getString(key));
    }

    public static int getInteger(String key) {
        return Integer.parseInt(PROPERTIES.getString(key));
    }

}
