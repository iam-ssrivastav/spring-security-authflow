package com.authflow.cache;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * LFU (Least Frequently Used) Cache Implementation.
 * 
 * <h2>Interview Q&A:</h2>
 * 
 * <p>
 * <b>Q:</b> How does LFU cache work?
 * </p>
 * <p>
 * <b>A:</b> LFU cache evicts the least frequently used item when capacity is
 * full.
 * 
 * Implementation:
 * <ul>
 * <li>Track access frequency for each key</li>
 * <li>Evict item with lowest frequency</li>
 * <li>If tie, evict least recently used among them</li>
 * </ul>
 * 
 * Time Complexity:
 * <ul>
 * <li>Get: O(1) for lookup + O(log n) for frequency update</li>
 * <li>Put: O(log n) for eviction</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <b>Q:</b> LRU vs LFU - which is better?
 * </p>
 * <p>
 * <b>A:</b>
 * <ul>
 * <li><b>LRU:</b> Better for temporal locality (recent items likely accessed
 * again)</li>
 * <li><b>LFU:</b> Better for frequency-based patterns (popular items stay
 * cached)</li>
 * <li><b>LRU:</b> Simpler implementation, O(1) operations</li>
 * <li><b>LFU:</b> More complex, handles "one-time bulk reads" better</li>
 * </ul>
 * Most systems use LRU due to simplicity and good general performance.
 * </p>
 * 
 * @author Shivam Srivastav
 */
@Component
@Slf4j
public class LFUCache<K, V> {

    private final int capacity;
    private final Map<K, CacheNode<K, V>> cache;
    private final PriorityQueue<CacheNode<K, V>> frequencyQueue;
    private long timestamp = 0;

    @Data
    @AllArgsConstructor
    private static class CacheNode<K, V> implements Comparable<CacheNode<K, V>> {
        K key;
        V value;
        int frequency;
        long lastAccessTime;

        @Override
        public int compareTo(CacheNode<K, V> other) {
            // First compare by frequency
            int freqCompare = Integer.compare(this.frequency, other.frequency);
            if (freqCompare != 0) {
                return freqCompare;
            }
            // If same frequency, compare by access time (LRU within same frequency)
            return Long.compare(this.lastAccessTime, other.lastAccessTime);
        }
    }

    public LFUCache() {
        this(100);
    }

    public LFUCache(int capacity) {
        this.capacity = capacity;
        this.cache = new HashMap<>();
        this.frequencyQueue = new PriorityQueue<>();
    }

    /**
     * Get value from cache and increment frequency.
     */
    public synchronized V get(K key) {
        CacheNode<K, V> node = cache.get(key);
        if (node == null) {
            log.debug("LFU cache MISS: {}", key);
            return null;
        }

        // Update frequency and timestamp
        frequencyQueue.remove(node);
        node.setFrequency(node.getFrequency() + 1);
        node.setLastAccessTime(++timestamp);
        frequencyQueue.offer(node);

        log.debug("LFU cache HIT: {} (freq: {})", key, node.getFrequency());
        return node.getValue();
    }

    /**
     * Put value in cache.
     * Evicts LFU item if capacity exceeded.
     */
    public synchronized void put(K key, V value) {
        if (capacity <= 0) {
            return;
        }

        // Update existing key
        if (cache.containsKey(key)) {
            CacheNode<K, V> node = cache.get(key);
            frequencyQueue.remove(node);
            node.setValue(value);
            node.setFrequency(node.getFrequency() + 1);
            node.setLastAccessTime(++timestamp);
            frequencyQueue.offer(node);
            log.debug("LFU cache UPDATE: {}", key);
            return;
        }

        // Evict if at capacity
        if (cache.size() >= capacity) {
            CacheNode<K, V> evicted = frequencyQueue.poll();
            if (evicted != null) {
                cache.remove(evicted.getKey());
                log.debug("LFU evicting: {} (freq: {})", evicted.getKey(), evicted.getFrequency());
            }
        }

        // Add new entry
        CacheNode<K, V> newNode = new CacheNode<>(key, value, 1, ++timestamp);
        cache.put(key, newNode);
        frequencyQueue.offer(newNode);
        log.debug("LFU cache PUT: {} (size: {})", key, cache.size());
    }

    /**
     * Remove from cache.
     */
    public synchronized void remove(K key) {
        CacheNode<K, V> node = cache.remove(key);
        if (node != null) {
            frequencyQueue.remove(node);
            log.debug("LFU cache REMOVE: {}", key);
        }
    }

    /**
     * Clear entire cache.
     */
    public synchronized void clear() {
        cache.clear();
        frequencyQueue.clear();
        timestamp = 0;
        log.debug("LFU cache CLEARED");
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
