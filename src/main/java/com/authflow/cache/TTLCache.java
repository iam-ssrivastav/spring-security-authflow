package com.authflow.cache;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TTL (Time To Live) Cache Implementation.
 * 
 * <h2>Interview Q&A:</h2>
 * 
 * <p>
 * <b>Q:</b> What is TTL in caching?
 * </p>
 * <p>
 * <b>A:</b> TTL (Time To Live) is the duration for which a cache entry remains
 * valid.
 * After TTL expires, the entry is considered stale and removed.
 * 
 * Use cases:
 * <ul>
 * <li>Session data (expire after inactivity)</li>
 * <li>JWT tokens (expire after fixed time)</li>
 * <li>API rate limiting (reset after time window)</li>
 * <li>Temporary data (OTP codes, reset tokens)</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <b>Q:</b> How to implement TTL cache?
 * </p>
 * <p>
 * <b>A:</b> Two approaches:
 * <ul>
 * <li><b>Lazy eviction:</b> Check expiry on access (this implementation)</li>
 * <li><b>Active eviction:</b> Background thread removes expired entries</li>
 * </ul>
 * 
 * Lazy eviction:
 * <ul>
 * <li>Pros: No background threads, simple</li>
 * <li>Cons: Expired entries stay in memory until accessed</li>
 * </ul>
 * 
 * Active eviction:
 * <ul>
 * <li>Pros: Frees memory immediately</li>
 * <li>Cons: Requires background thread, more complex</li>
 * </ul>
 * </p>
 * 
 * @author Shivam Srivastav
 */
@Component
@Slf4j
public class TTLCache<K, V> {

    @Data
    @AllArgsConstructor
    private static class CacheEntry<V> {
        V value;
        Instant expiryTime;

        boolean isExpired() {
            return Instant.now().isAfter(expiryTime);
        }
    }

    private final Map<K, CacheEntry<V>> cache;
    private final Duration defaultTTL;

    public TTLCache() {
        this(Duration.ofMinutes(10));
    }

    public TTLCache(Duration defaultTTL) {
        this.cache = new ConcurrentHashMap<>();
        this.defaultTTL = defaultTTL;
    }

    /**
     * Get value from cache.
     * Returns null if expired or not found.
     */
    public V get(K key) {
        CacheEntry<V> entry = cache.get(key);

        if (entry == null) {
            log.debug("TTL cache MISS: {}", key);
            return null;
        }

        if (entry.isExpired()) {
            cache.remove(key);
            log.debug("TTL cache EXPIRED: {}", key);
            return null;
        }

        log.debug("TTL cache HIT: {}", key);
        return entry.getValue();
    }

    /**
     * Put value in cache with default TTL.
     */
    public void put(K key, V value) {
        put(key, value, defaultTTL);
    }

    /**
     * Put value in cache with custom TTL.
     */
    public void put(K key, V value, Duration ttl) {
        Instant expiryTime = Instant.now().plus(ttl);
        cache.put(key, new CacheEntry<>(value, expiryTime));
        log.debug("TTL cache PUT: {} (expires in {} seconds)", key, ttl.getSeconds());
    }

    /**
     * Remove from cache.
     */
    public void remove(K key) {
        cache.remove(key);
        log.debug("TTL cache REMOVE: {}", key);
    }

    /**
     * Clear entire cache.
     */
    public void clear() {
        cache.clear();
        log.debug("TTL cache CLEARED");
    }

    /**
     * Clean up expired entries (call periodically).
     */
    public void cleanupExpired() {
        int removed = 0;
        for (Map.Entry<K, CacheEntry<V>> entry : cache.entrySet()) {
            if (entry.getValue().isExpired()) {
                cache.remove(entry.getKey());
                removed++;
            }
        }
        if (removed > 0) {
            log.debug("TTL cache cleanup: removed {} expired entries", removed);
        }
    }

    /**
     * Get current cache size (includes expired entries).
     */
    public int size() {
        return cache.size();
    }

    /**
     * Check if key exists and is not expired.
     */
    public boolean containsKey(K key) {
        CacheEntry<V> entry = cache.get(key);
        return entry != null && !entry.isExpired();
    }
}
