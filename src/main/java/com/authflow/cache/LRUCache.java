package com.authflow.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * LRU (Least Recently Used) Cache Implementation.
 * 
 * <h2>Interview Q&A:</h2>
 * 
 * <p>
 * <b>Q:</b> How does LRU cache work?
 * </p>
 * <p>
 * <b>A:</b> LRU cache evicts the least recently used item when capacity is
 * full.
 * 
 * Implementation:
 * <ul>
 * <li>Uses LinkedHashMap with access order</li>
 * <li>Most recently used items at tail</li>
 * <li>Least recently used items at head</li>
 * <li>On capacity full, remove head</li>
 * </ul>
 * 
 * Time Complexity:
 * <ul>
 * <li>Get: O(1)</li>
 * <li>Put: O(1)</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <b>Q:</b> Implement LRU cache from scratch
 * </p>
 * <p>
 * <b>A:</b> See implementation below using LinkedHashMap
 * or implement with HashMap + Doubly Linked List for full control.
 * </p>
 * 
 * @author Shivam Srivastav
 */
@Component
@Slf4j
public class LRUCache<K, V> {

    private final int capacity;
    private final Map<K, V> cache;

    public LRUCache() {
        this(100); // Default capacity
    }

    public LRUCache(int capacity) {
        this.capacity = capacity;
        // LinkedHashMap with access order (true)
        this.cache = new LinkedHashMap<K, V>(capacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                boolean shouldRemove = size() > LRUCache.this.capacity;
                if (shouldRemove) {
                    log.debug("LRU evicting: {}", eldest.getKey());
                }
                return shouldRemove;
            }
        };
    }

    /**
     * Get value from cache.
     * Updates access order (moves to end).
     */
    public synchronized V get(K key) {
        V value = cache.get(key);
        if (value != null) {
            log.debug("LRU cache HIT: {}", key);
        } else {
            log.debug("LRU cache MISS: {}", key);
        }
        return value;
    }

    /**
     * Put value in cache.
     * Evicts LRU item if capacity exceeded.
     */
    public synchronized void put(K key, V value) {
        cache.put(key, value);
        log.debug("LRU cache PUT: {} (size: {})", key, cache.size());
    }

    /**
     * Remove from cache.
     */
    public synchronized void remove(K key) {
        cache.remove(key);
        log.debug("LRU cache REMOVE: {}", key);
    }

    /**
     * Clear entire cache.
     */
    public synchronized void clear() {
        cache.clear();
        log.debug("LRU cache CLEARED");
    }

    /**
     * Get current cache size.
     */
    public synchronized int size() {
        return cache.size();
    }

    /**
     * Check if key exists.
     */
    public synchronized boolean containsKey(K key) {
        return cache.containsKey(key);
    }
}
