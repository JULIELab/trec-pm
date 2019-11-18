package de.julielab.ir;

import at.medunigraz.imi.bst.config.TrecConfig;
import de.julielab.java.utilities.cache.CacheConfiguration;
import de.julielab.java.utilities.cache.CacheService;
import org.apache.commons.lang.StringUtils;

import java.io.File;

/**
 * <p>A self-configuring subclass of {@link CacheConfiguration} for use in {@link CacheService#initialize(CacheConfiguration)}.</p>
 * <p>The configuration values are taken from the configuration file.</p>
 */
public class TrecCacheConfiguration extends CacheConfiguration {
    public TrecCacheConfiguration() {
        super(TrecConfig.CACHE_TYPE.equalsIgnoreCase("local") ? CacheService.CacheType.LOCAL : CacheService.CacheType.REMOTE,
                new File(TrecConfig.CACHE_DIR),
                TrecConfig.CACHE_HOST, TrecConfig.CACHE_PORT,
                TrecConfig.CACHE_READ_ONLY);
    }
}
