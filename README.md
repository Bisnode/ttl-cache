# ttl-cache

A resource effective cache implementation where any entry's lifetime is kept track of separately and evicted when it's 
time-to-live reaches zero. Ideal for storing objects where lifetime differs per object such as tokens.


## Usage

Import it

    compile group: 'com.bisnode.cache', name: 'ttl-cache', version: '1.0.3'
    
Use it

```java
// Instantiate a new cache where both keys and values are of type String
// and object lifetime will be measured in seconds
TTLCache<String, String> cache = new TimedKeyValueCache<>(TimeUnit.SECONDS);

// Adding an item to the cache with automatic eviction after 60 seconds
cache.add("my key", "my value", 60);

// Retrieving an item from the cache (if it's in there)
Optional<String> valueInCache = cache.get("my key");

```
