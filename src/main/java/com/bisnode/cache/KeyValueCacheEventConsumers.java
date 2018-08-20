package com.bisnode.cache;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Class containing event consumers for the various events happening
 * in the lifetime of a cache entry.
 *
 * @param <K> The key of the cache entry triggering the event
 * @param <V> The value of the cache entry triggering the event
 */
@SuppressWarnings("WeakerAccess")
@ParametersAreNonnullByDefault
public class KeyValueCacheEventConsumers<K, V> {

    private final BiConsumer<K, V> onEntryAdded;
    private final BiConsumer<K, V> onEntryEvicted;
    private final BiConsumer<K, V> onEntryRetrieved;
    private final Consumer<K> onEntryCacheMiss;

    /**
     * No-args constructor creates an object where all consumers are no-op's
     */
    public KeyValueCacheEventConsumers() {
        this(null, null, null, null);
    }

    /**
     * Create a KeyValueCacheEventConsumers object given any consumers that
     * might be of interest. Null values are OK and will be turned into no-op
     * consumers.
     *
     * @param onEntryAdded      BiConsumer for listening to added entry events
     * @param onEntryEvicted    BiConsumer for listening to eviction events
     * @param onEntryRetrieved  BiConsumer for listening to new entry events
     * @param onEntryCacheMiss  Consumer for listening to cache miss events
     */
    @SuppressWarnings({"squid:RightCurlyBraceStartLineCheck", "squid:S00108"})
    public KeyValueCacheEventConsumers(
            @Nullable BiConsumer<K, V> onEntryAdded,
            @Nullable BiConsumer<K, V> onEntryEvicted,
            @Nullable BiConsumer<K, V> onEntryRetrieved,
            @Nullable Consumer<K> onEntryCacheMiss
    ) {
        // Turn null consumers into no-op operations
        this.onEntryAdded =  onEntryAdded == null ? (k, v) -> {} : onEntryAdded;
        this.onEntryEvicted = onEntryEvicted== null ? (k, v) -> {} : onEntryEvicted;
        this.onEntryRetrieved = onEntryRetrieved == null ? (k, v) -> {} : onEntryRetrieved;
        this.onEntryCacheMiss = onEntryCacheMiss == null ? k -> {} : onEntryCacheMiss;
    }

    BiConsumer<K, V> onEntryAdded() {
        return onEntryAdded;
    }

    BiConsumer<K, V> onEntryEvicted() {
        return onEntryEvicted;
    }

    BiConsumer<K, V> onEntryRetrieved() {
        return onEntryRetrieved;
    }

    Consumer<K> onEntryCacheMiss() {
        return onEntryCacheMiss;
    }

}
