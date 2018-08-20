package com.bisnode.cache;

import javax.annotation.Nonnegative;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

/**
 * A cache where any entry's lifetime is kept track of separately. Ideal for
 * storing objects where lifetime differs, such as access tokens.
 *
 * @param <K> The key identifying the object
 * @param <V> The value to be cached
 */
@SuppressWarnings("InterfaceWithOnlyOneDirectInheritor")
@ParametersAreNonnullByDefault
public interface TTLCache<K, V> {

    /**
     * Adds an item to the cache and sets it's time to live.
     *
     * @param key   The (non null) key to reference this cache entry
     * @param value The (non null) value to add to the cache
     * @param ttl   The time-to-live value for the item, in the time units
     *              provided by the implementation
     */
    void add(K key, V value, @Nonnegative long ttl);

    /**
     * Retrieves an optional, populated with the value only if the key was
     * present in the cache at the moment of retrieval.
     *
     * @param key   The key used to retrieve a value from the cache
     * @return      An Optional containing the value if present or empty if not
     */
    Optional<V> get(K key);

    /**
     * Whether this cache contains the provided key at that time of the call.
     *
     * @param key   The key to check
     * @return      Whether the cache contains the given key
     */
    boolean containsKey(K key);

}
