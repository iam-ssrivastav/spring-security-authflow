package com.authflow.service;

import com.authflow.cache.TTLCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Rate Limiting Service using TTL Cache.
 * 
 * <h2>Interview Q&A:</h2>
 * 
 * <p>
 * <b>Q:</b> What is rate limiting and why is it important?
 * </p>
 * <p>
 * <b>A:</b> Rate limiting controls the number of requests a user can make in a
 * time window.
 * 
 * Benefits:
 * <ul>
 * <li><b>Prevent abuse:</b> Stop DoS attacks and API abuse</li>
 * <li><b>Fair usage:</b> Ensure resources are shared fairly</li>
 * <li><b>Cost control:</b> Limit expensive operations</li>
 * <li><b>Stability:</b> Prevent system overload</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <b>Q:</b> What are different rate limiting algorithms?
 * </p>
 * <p>
 * <b>A:</b>
 * <ul>
 * <li><b>Fixed Window:</b> Count requests in fixed time windows (this
 * implementation)</li>
 * <li><b>Sliding Window:</b> More accurate, uses weighted counts</li>
 * <li><b>Token Bucket:</b> Allows bursts, refills tokens over time</li>
 * <li><b>Leaky Bucket:</b> Smooths traffic, processes at constant rate</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <b>Q:</b> How do you implement distributed rate limiting?
 * </p>
 * <p>
 * <b>A:</b> Use Redis with atomic operations:
 * 
 * <pre>
 * INCR key
 * EXPIRE key 60
 * if count > limit: reject
 * </pre>
 * 
 * Redis ensures atomicity across multiple servers.
 * </p>
 * 
 * @author Shivam Srivastav
 */
@Service
@Slf4j
public class RateLimitService {

    private final TTLCache<String, Integer> rateLimitCache;

    // Rate limit: 10 requests per minute
    private static final int MAX_REQUESTS = 10;
    private static final Duration WINDOW_DURATION = Duration.ofMinutes(1);

    public RateLimitService() {
        this.rateLimitCache = new TTLCache<>(WINDOW_DURATION);
    }

    /**
     * Check if request is allowed for the given identifier (user ID, IP, etc.).
     * 
     * @param identifier Unique identifier (userId, IP address, API key)
     * @return true if allowed, false if rate limit exceeded
     */
    public boolean isAllowed(String identifier) {
        String key = "rate_limit:" + identifier;
        Integer count = rateLimitCache.get(key);

        if (count == null) {
            // First request in window
            rateLimitCache.put(key, 1, WINDOW_DURATION);
            log.debug("Rate limit: {}/{}  for {}", 1, MAX_REQUESTS, identifier);
            return true;
        }

        if (count >= MAX_REQUESTS) {
            // Rate limit exceeded
            log.warn("Rate limit exceeded for {}: {}/{}", identifier, count, MAX_REQUESTS);
            return false;
        }

        // Increment count
        rateLimitCache.put(key, count + 1, WINDOW_DURATION);
        log.debug("Rate limit: {}/{} for {}", count + 1, MAX_REQUESTS, identifier);
        return true;
    }

    /**
     * Get remaining requests for identifier.
     */
    public int getRemainingRequests(String identifier) {
        String key = "rate_limit:" + identifier;
        Integer count = rateLimitCache.get(key);

        if (count == null) {
            return MAX_REQUESTS;
        }

        return Math.max(0, MAX_REQUESTS - count);
    }

    /**
     * Get current request count for identifier.
     */
    public int getCurrentCount(String identifier) {
        String key = "rate_limit:" + identifier;
        Integer count = rateLimitCache.get(key);
        return count != null ? count : 0;
    }

    /**
     * Reset rate limit for identifier (admin operation).
     */
    public void reset(String identifier) {
        String key = "rate_limit:" + identifier;
        rateLimitCache.remove(key);
        log.info("Rate limit reset for {}", identifier);
    }
}
