/**
 * <p>The <tt>cache</tt> package organizes the usage of local or remote (socket-connected) caches based on the
 * <tt>MapDB</tt> library (http://www.mapdb.org/).</p>
 * <p>
 * Local and remote caches use the very same file cache layout in the end so that the cache files are compatible.
 * The only difference between local and remote caching is that for the local cache only one JVM can write into
 * the cache files. Otherwise, data corruption could happen due to unsynchronized concurrent data modification.
 * </p>
 * <h2>Local Caches</h2>
 * <p>
 * To alleviate this situation - without resorting to a remote cache - caching can be configured to be read-only
 * in the configuration file. It is important to note that the first JVM to access to a caching directory will still
 * be granted writing rights if the directory did not already exist. Subsequent access will be read only, if set so
 * in the configuration.
 * </p>
 * <p>This mechanism can prove quite useful if multiple experiments use the same data. Then, the first experiment
 * will fill the caches and subsequently running experiment will use the cached data.</p>
 * <h2>Remote Caches</h2>
 * <p>
 * If the local caching is not appropriate, for example due to very different data needs of the running experiments,
 * it is relatively straight forward to set up a remote cache. Just use the main method of {@link de.julielab.ir.cache.CacheServer}
 * to start up a simple server and configure <tt>cache.type=remove</tt> in the configuration file together
 * with the <tt>cache.host</tt> and <tt>cache.port</tt> properties pointing to the cache server. This will
 * automatically cause classes using caching to use the {@link de.julielab.ir.cache.RemoteCacheAccess} class
 * internally. No other changes are required since the object to be cached must be {@link java.io.Serializable} in
 * both, file or remote caching.
 * </p>
 */
package de.julielab.ir.cache;