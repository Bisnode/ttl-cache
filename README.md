# ttl-cache

[![Build Status](https://travis-ci.com/Bisnode/ttl-cache.svg?branch=master)](https://travis-ci.com/Bisnode/ttl-cache)
![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=ttl-cache&metric=alert_status)

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

## License

Copyright 2018 Bisnode

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
