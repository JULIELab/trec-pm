package de.julielab.ir;

import at.medunigraz.imi.bst.config.TrecConfig;
import de.julielab.java.utilities.cache.CacheServer;

import java.io.IOException;

public class TrecCacheServer {

    public static void main(String args[]) throws IOException {
        CacheServer.main(new String[]{TrecConfig.CACHE_DIR, TrecConfig.CACHE_HOST, String.valueOf(TrecConfig.CACHE_PORT), args[0]});
    }
}
