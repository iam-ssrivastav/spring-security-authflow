package com.authflow.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * Cache Configuration for the application.
 * 
 * <h2>Interview Q&A:</h2>
 * 
 * <p>
 * <b>Q:</b> What is caching and why is it important?
 * </p>
 * <p>
 * <b>A:</b> Caching stores frequently accessed data in memory to:
 * <ul>
 * <li>Reduce database queries (improve performance)</li>
 * <li>Decrease response time (faster API responses)</li>
 * <li>Reduce server load (less CPU/DB usage)</li>
 * <li>Improve scalability (handle more requests)</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <b>Q:</b> What are different cache eviction policies?
 * </p>
 * <p>
 * <b>A:</b>
 * <ul>
 * <li><b>LRU (Least Recently Used):</b> Evicts least recently accessed
 * items</li>
 * <li><b>LFU (Least Frequently Used):</b> Evicts least frequently accessed
 * items</li>
 * <li><b>FIFO (First In First Out):</b> Evicts oldest items first</li>
 * <li><b>TTL (Time To Live):</b> Items expire after fixed time</li>
 * <li><b>Size-based:</b> Evicts when cache reaches max size</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <b>Q:</b> What are cache invalidation strategies?
 * </p>
 * <p>
 * <b>A:</b>
 * <ul>
 * <li><b>Time-based:</b> Expire after fixed duration (TTL)</li>
 * <li><b>Event-based:</b> Invalidate on data update/delete</li>
 * <li><b>Manual:</b> Explicit cache clear</li>
 * <li><b>Write-through:</b> Update cache and DB together</li>
 * <li><b>Write-behind:</b> Update cache first, DB async</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <b>Q:</b> When should you use caching?
 * </p>
 * <p>
 * <b>A:</b> Use caching for:
 * <ul>
 * <li>Frequently read, rarely updated data</li>
 * <li>Expensive computations</li>
 * <li>External API calls</li>
 * <li>Database query results</li>
 * <li>Session data</li>
 * </ul>
 * Don't cache:
 * <ul>
 * <li>Frequently changing data</li>
 * <li>User-specific sensitive data (unless encrypted)</li>
 * <li>Large objects (memory constraints)</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <b>Q:</b> What is cache stampede and how to prevent it?
 * </p>
 * <p>
 * <b>A:</b> Cache stampede occurs when many requests try to regenerate
 * expired cache simultaneously, overwhelming the database.
 * 
 * Prevention:
 * <ul>
 * <li><b>Locking:</b> First request locks, others wait</li>
 * <li><b>Probabilistic early expiration:</b> Refresh before actual expiry</li>
 * <li><b>Background refresh:</b> Async cache warming</li>
 * <li><b>Stale-while-revalidate:</b> Serve stale data while refreshing</li>
 * </ul>
 * </p>
 * 
 * @author Shivam Srivastav
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Cache names used in the application.
     */
    public static final String USERS_CACHE = "users";
    public static final String ROLES_CACHE = "roles";
    public static final String PERMISSIONS_CACHE = "permissions";
    public static final String JWT_BLACKLIST_CACHE = "jwtBlacklist";
    public static final String RATE_LIMIT_CACHE = "rateLimit";

    /**
     * Configure cache manager with multiple caches.
     * 
     * <p>
     * In production, use Redis or other distributed cache:
     * 
     * <pre>
     * &#64;Bean
     * public CacheManager cacheManager(RedisConnectionFactory factory) {
     *     RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
     *             .entryTtl(Duration.ofMinutes(10))
     *             .disableCachingNullValues();
     * 
     *     return RedisCacheManager.builder(factory)
     *             .cacheDefaults(config)
     *             .build();
     * }
     * </pre>
     * </p>
     */
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();

        cacheManager.setCaches(Arrays.asList(
                // User cache - frequently accessed
                new ConcurrentMapCache(USERS_CACHE),

                // Role cache - rarely changes
                new ConcurrentMapCache(ROLES_CACHE),

                // Permission cache - rarely changes
                new ConcurrentMapCache(PERMISSIONS_CACHE),

                // JWT blacklist - for logout/revoked tokens
                new ConcurrentMapCache(JWT_BLACKLIST_CACHE),

                // Rate limiting - track API usage
                new ConcurrentMapCache(RATE_LIMIT_CACHE)));

        return cacheManager;
    }
}
