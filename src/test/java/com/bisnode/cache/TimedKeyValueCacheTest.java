package com.bisnode.cache;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class TimedKeyValueCacheTest {

    private static final String ENTRY_ADDED = "entry added";
    private static final String ENTRY_EVICTED = "entry evicted";
    private static final String ENTRY_RETRIEVED = "entry retrieved";
    private static final String CACHE_MISS = "cache miss";

    private TimedKeyValueCache<String, String> cache;
    private TimedKeyValueCache<String, String> cacheWithConsumers;

    @SuppressWarnings({"resource", "IOResourceOpenedButNotSafelyClosed", "ImplicitDefaultCharsetUsage"})
    private final PrintStream original = new PrintStream(System.out);
    private final List<String> consumerMessages = new LinkedList<>();

    @Before
    public void setUp() {
        consumerMessages.clear();
        KeyValueCacheEventConsumers<String, String> consumers =
                new KeyValueCacheEventConsumers<>(
                        (k, v) -> consumerMessages.add(ENTRY_ADDED),
                        (k, v) -> consumerMessages.add(ENTRY_EVICTED),
                        (k, v) -> consumerMessages.add(ENTRY_RETRIEVED),
                        k -> consumerMessages.add(CACHE_MISS)
                );

        cache = new TimedKeyValueCache<>(TimeUnit.MILLISECONDS);
        cacheWithConsumers = new TimedKeyValueCache<>(
                TimeUnit.MILLISECONDS,
                consumers
        );
    }

    @After
    public void tearDown() throws InterruptedException {
        cache.shutdown(0);
        System.setOut(original);
    }

    @Test(expected = NullPointerException.class)
    @SuppressWarnings("all")
    public void timeUnitGivenToConstructorCanNotBeNull() {
        new TimedKeyValueCache<String, String>(null);
    }

    @Test(expected = NullPointerException.class)
    @SuppressWarnings("all")
    public void eventConsumersCanNotBeNull() {
        new TimedKeyValueCache<>(TimeUnit.DAYS, null);
    }

    @Test
    public void anItemAddedIsImmediatelyRetrievable() {
        cache.add("key", "value", 200);
        assertTrue(cache.get("key").isPresent());
    }

    @Test
    public void anItemAddedIsRemovedAfterItsExpiry() throws InterruptedException {
        cache.add("key", "value", 1);
        Thread.sleep(10);
        assertFalse(cache.containsKey("key"));
        assertFalse(cache.get("key").isPresent());
    }

    @Test
    public void addingTheSameValueMultipleTimesPreservesLastTTL() throws InterruptedException {
        cache.add("key", "value", 1);
        cache.add("key", "value", 1);
        cache.add("key", "value", 50);
        Thread.sleep(10);
        assertTrue(cache.containsKey("key"));
    }

    @Test
    public void addingTheSameValueMultipleTimesPreservesLastTTLEvenIfLower() throws InterruptedException {
        cache.add("key", "value", 50);
        cache.add("key", "value", 1);
        Thread.sleep(10);
        assertFalse(cache.containsKey("key"));
    }

    @Test
    public void providingANegativeTTLDiscardsTheItem() {
        cache.add("key", "value", -1000);
        assertFalse(cache.containsKey("key"));
    }

    @Test
    public void providingANegativeTTLDoesNotDiscardPreviousItems() {
        cache.add("key", "value", 1000);
        cache.add("key", "value", -1000);
        assertTrue(cache.containsKey("key"));
    }

    @Test(expected = IllegalStateException.class)
    public void callingAddOnShutdownCacheThrows() throws InterruptedException {
        cache.shutdown(0);
        cache.add("key", "value", 1);
    }

    @Test(expected = IllegalStateException.class)
    public void callingGetOnShutdownCacheThrows() throws InterruptedException {
        cache.shutdown(0);
        cache.get("key");
    }

    @Test(expected = IllegalStateException.class)
    public void callingContainsKeyOnShutdownCacheThrows() throws InterruptedException {
        cache.shutdown(0);
        cache.containsKey("key");
    }

    @Test
    public void shuttingDownWhenNotEmptyShouldWork() throws InterruptedException {
        cacheWithConsumers.add("key", "value", 500000);
        cacheWithConsumers.shutdown(0);
        assertTrue(true);
    }

    @Test
    public void cacheEvictionDuringShutdownShouldNotCauseErrors() throws InterruptedException {
        cacheWithConsumers.add("key", "value", 10);
        cacheWithConsumers.shutdown(0);
        Thread.sleep(100);
        assertTrue(true);
    }

    @Test
    public void addingAnItemToTheCacheNotifiesConsumer() {
        cacheWithConsumers.add("key", "value", 5);
        assertTrue(consumerMessages.contains(ENTRY_ADDED));
    }

    @Test
    public void updatingAnItemToTheCacheNotifiesConsumerAgain() {
        cacheWithConsumers.add("key", "value", 5);
        cacheWithConsumers.add("key", "another value", 5);
        String[] expecteds = new String[] {ENTRY_ADDED, ENTRY_ADDED};
        assertArrayEquals(expecteds, consumerMessages.toArray());
    }

    @Test
    public void anEvictedItemNotifiesConsumer() throws InterruptedException {
        cacheWithConsumers.add("key", "value", 1);
        Thread.sleep(20);
        assertTrue(consumerMessages.contains(ENTRY_EVICTED));
    }

    @Test
    public void retrievingAnItemNotifiesConsumer() {
        cacheWithConsumers.add("key", "value", 10);
        cacheWithConsumers.get("key");
        assertTrue(consumerMessages.contains(ENTRY_RETRIEVED));
    }

    @Test
    public void cacheMissNotifiesConsumer() {
        cacheWithConsumers.get("key");
        assertTrue(consumerMessages.contains(CACHE_MISS));
    }
}
