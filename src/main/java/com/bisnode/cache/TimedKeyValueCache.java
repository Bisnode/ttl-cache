package com.bisnode.cache;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.WillClose;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * Cache implementation where the time-to-live of a key/value item pair is
 * determined when the item is added to cache. The life time is thus
 * "determined" by the item and not by the cache itself as in most cache
 * implementations.
 *
 * @param <K>   The type of keys used for cache retrieval
 * @param <V>   The type of the values this cache contains
 */
@ParametersAreNonnullByDefault
public class TimedKeyValueCache<K, V> implements TTLCache<K, V> {

    private final Map<K, V> entries = new ConcurrentHashMap<>();
    private final TimeUnit timeUnit;
    private final KeyValueCacheEventConsumers<K, V> eventConsumers;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Map<K, ScheduledFuture<?>> scheduled = new ConcurrentHashMap<>();

    private boolean isAlive = true;

    /**
     * Creates a new TimedKeyValueCache with the given {@link TimeUnit}.
     * The cache will evict any item at the TTL given when the item was
     * added.
     *
     * @param timeUnit  The TimeUnit to use for this cache
     */
    @SuppressWarnings("WeakerAccess")
    public TimedKeyValueCache(TimeUnit timeUnit) {
        this(timeUnit, new KeyValueCacheEventConsumers<>());
    }

    /**
     * Creates a new TimedKeyValueCache with the given {@link TimeUnit}.
     * The cache will evict any item at the TTL given when the item was
     * added. Allows for registered {@link BiConsumer} to take action when
     * entries are added or removed from the cache.
     *
     * @param timeUnit          The TimeUnit to use for this cache
     * @param eventConsumers    a KeyValueCacheEventConsumers for notifications
     *                          on cache state changes
     */
    public TimedKeyValueCache(
            TimeUnit timeUnit,
            KeyValueCacheEventConsumers<K, V> eventConsumers
    ) {
        Objects.requireNonNull(timeUnit);
        Objects.requireNonNull(eventConsumers);

        this.timeUnit = timeUnit;
        this.eventConsumers = eventConsumers;
    }

    /**
     * Adds an item to the cache and sets it's time to live. If the key
     * was already present in the cache it will be replaced with the given
     * value and time to live.
     *
     * @param key   The (non null) key to reference this cache entry
     * @param value The (non null) value to add to the cache
     * @param ttl   The time-to-live value for the item, in the time units
     *              provided to the constructor. Given a non-positive number
     *              the entry is discarded immediately
     */
    @Override
    public void add(K key, V value, @Nonnegative long ttl) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);

        ensureAlive("Trying to add entry after termination");

        if (ttl > 0) {
            // Cancel previous task if already exists as new ttl should prevail
            ScheduledFuture<?> previous = scheduled.get(key);
            if (previous != null) {
                previous.cancel(true);
            }

            // Add entry to the cache and notify consumer
            entries.put(key, value);
            eventConsumers.onEntryAdded().accept(key, value);

            // Schedule removal of the item based on the provided ttl
            scheduled.put(key, scheduler.schedule(() -> {
                entries.remove(key);
                scheduled.remove(key);
                eventConsumers.onEntryEvicted().accept(key, value);
            }, ttl, timeUnit));
        }
    }

    /**
     * Retrieves an optional, populated with the value only if the key was
     * present in the cache at the moment of retrieval. The operation does
     * not remove the item from the cache.
     *
     * @param key   The key used to retrieve a value from the cache
     * @return      An Optional containing the value if present or empty if not
     */
    @Override
    @Nonnull
    public Optional<V> get(K key) {
        ensureAlive("Trying to retrieve entry after termination");

        Optional<V> optional = Optional.ofNullable(entries.get(key));
        if (optional.isPresent()) {
            eventConsumers.onEntryRetrieved().accept(key, optional.get());
        } else {
            eventConsumers.onEntryCacheMiss().accept(key);
        }

        return optional;
    }

    /**
     * Whether this cache contains the provided key at that time.
     *
     * @param key   The key to check
     * @return      Whether the cache contains the given key
     */
    @Override
    public boolean containsKey(K key) {
        ensureAlive("Calling containsKey after termination");

        return entries.containsKey(key);
    }

    /**
     * Shut down this cache and terminates any running scheduled jobs after
     * waiting for timeoutInSeconds.
     *
     * @param timeoutInSeconds      The number of seconds to await running jobs
     * @throws InterruptedException If termination fails
     */
    @WillClose
    public void shutdown(@Nonnegative long timeoutInSeconds) throws InterruptedException {
        scheduler.awaitTermination(timeoutInSeconds, TimeUnit.SECONDS);
        isAlive = false;
    }

    private void ensureAlive(String message) {
        if (!isAlive) {
            throw new IllegalStateException(message);
        }
    }

}
