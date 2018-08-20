/**
 * A resource effective cache implementation where any entry's lifetime is
 * kept track of separately and evicted when it's time-to-live reaches
 * zero. Ideal for storing objects where lifetime differs per object.
 */
package com.bisnode.cache;