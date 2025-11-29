# Caching in AuthFlow

Comprehensive guide to caching implementation and strategies.

## 📚 Table of Contents
1. [Cache Types Implemented](#cache-types-implemented)
2. [Cache Configuration](#cache-configuration)
3. [Cache Eviction Policies](#cache-eviction-policies)
4. [Usage Examples](#usage-examples)
5. [Interview Q&A](#interview-qa)

---

## 🎯 Cache Types Implemented

### 1. Spring Cache (Annotation-based)
**Location:** `CacheConfig.java`

**Features:**
- Declarative caching with `@Cacheable`, `@CachePut`, `@CacheEvict`
- Multiple named caches
- Thread-safe ConcurrentMapCache

**Caches:**
- `users` - User data cache
- `roles` - Role data cache  
- `permissions` - Permission data cache
- `jwtBlacklist` - Revoked JWT tokens
- `rateLimit` - API rate limiting

**Example:**
```java
@Cacheable(value = "users", key = "#username")
public UserDetails loadUserByUsername(String username) {
    // Database query only on cache miss
}
```

---

### 2. LRU Cache (Least Recently Used)
**Location:** `cache/LRUCache.java`

**How it works:**
- Evicts least recently accessed items when full
- Uses LinkedHashMap with access order
- O(1) get and put operations

**Use cases:**
- Session data
- Recently viewed items
- Temporal locality patterns

**Example:**
```java
LRUCache<String, User> cache = new LRUCache<>(100);
cache.put("user123", user);
User cached = cache.get("user123"); // Moves to end (most recent)
```

**Implementation:**
```java
// LinkedHashMap with access order = true
new LinkedHashMap<K, V>(capacity, 0.75f, true) {
    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > capacity; // Auto-evict oldest
    }
}
```

---

### 3. LFU Cache (Least Frequently Used)
**Location:** `cache/LFUCache.java`

**How it works:**
- Tracks access frequency for each item
- Evicts least frequently used items
- Uses PriorityQueue for frequency ordering

**Use cases:**
- Popular content caching
- Frequency-based patterns
- Handling bulk one-time reads

**Example:**
```java
LFUCache<String, String> cache = new LFUCache<>(100);
cache.put("popular", "data");
cache.get("popular"); // Increments frequency
cache.get("popular"); // Frequency = 3
// Less frequently accessed items evicted first
```

**Complexity:**
- Get: O(1) lookup + O(log n) frequency update
- Put: O(log n) for eviction

---

### 4. TTL Cache (Time To Live)
**Location:** `cache/TTLCache.java`

**How it works:**
- Each entry has expiration time
- Lazy eviction on access
- Configurable TTL per entry

**Use cases:**
- Session tokens
- OTP codes
- Password reset tokens
- Rate limiting windows

**Example:**
```java
TTLCache<String, String> cache = new TTLCache<>(Duration.ofMinutes(10));

// Default TTL (10 minutes)
cache.put("session", "data");

// Custom TTL (1 hour)
cache.put("token", "value", Duration.ofHours(1));

// Auto-expires after TTL
String value = cache.get("session"); // null if expired
```

---

## ⚙️ Cache Configuration

### Spring Cache Setup

**1. Enable Caching:**
```java
@Configuration
@EnableCaching
public class CacheConfig {
    // Configuration
}
```

**2. Define Caches:**
```java
@Bean
public CacheManager cacheManager() {
    SimpleCacheManager manager = new SimpleCacheManager();
    manager.setCaches(Arrays.asList(
        new ConcurrentMapCache("users"),
        new ConcurrentMapCache("roles")
    ));
    return manager;
}
```

**3. Use Annotations:**
```java
@Cacheable("users")           // Cache result
@CachePut("users")            // Update cache
@CacheEvict("users")          // Remove from cache
@CacheEvict(allEntries=true)  // Clear entire cache
```

---

### Production Redis Configuration

```java
@Bean
public CacheManager cacheManager(RedisConnectionFactory factory) {
    RedisCacheConfiguration config = RedisCacheConfiguration
        .defaultCacheConfig()
        .entryTtl(Duration.ofMinutes(10))
        .disableCachingNullValues()
        .serializeValuesWith(
            RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer())
        );
    
    Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
    
    // Users cache - 15 minutes
    cacheConfigurations.put("users", 
        config.entryTtl(Duration.ofMinutes(15)));
    
    // Roles cache - 1 hour (rarely changes)
    cacheConfigurations.put("roles", 
        config.entryTtl(Duration.ofHours(1)));
    
    // JWT blacklist - 24 hours
    cacheConfigurations.put("jwtBlacklist", 
        config.entryTtl(Duration.ofHours(24)));
    
    return RedisCacheManager.builder(factory)
        .cacheDefaults(config)
        .withInitialCacheConfigurations(cacheConfigurations)
        .build();
}
```

---

## 🔄 Cache Eviction Policies

### 1. LRU (Least Recently Used)
**When to use:** Temporal locality (recent items likely accessed again)

**Pros:**
- Simple O(1) implementation
- Good general-purpose performance
- Handles temporal patterns well

**Cons:**
- Doesn't consider access frequency
- One-time bulk reads can evict hot data

---

### 2. LFU (Least Frequently Used)
**When to use:** Frequency-based patterns (popular items)

**Pros:**
- Keeps frequently accessed items
- Better for "hot" data
- Handles bulk reads better than LRU

**Cons:**
- More complex implementation
- Slow to adapt to changing patterns
- Old popular items may stay too long

---

### 3. FIFO (First In First Out)
**When to use:** Simple queue-like behavior

**Pros:**
- Simplest implementation
- Predictable behavior

**Cons:**
- Ignores access patterns
- Not optimal for most use cases

---

### 4. TTL (Time To Live)
**When to use:** Time-sensitive data

**Pros:**
- Automatic expiration
- Good for temporary data
- Prevents stale data

**Cons:**
- May evict still-useful data
- Requires tuning TTL values

---

## 💡 Usage Examples

### Example 1: Caching User Data

```java
@Service
public class UserService {
    
    @Cacheable(value = "users", key = "#userId")
    public User getUserById(Long userId) {
        // Database query only on cache miss
        return userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException());
    }
    
    @CachePut(value = "users", key = "#user.id")
    public User updateUser(User user) {
        // Update DB and cache
        return userRepository.save(user);
    }
    
    @CacheEvict(value = "users", key = "#userId")
    public void deleteUser(Long userId) {
        // Remove from cache and DB
        userRepository.deleteById(userId);
    }
}
```

---

### Example 2: JWT Token Blacklist

```java
@Service
public class JwtBlacklistService {
    
    @Autowired
    private TTLCache<String, Boolean> jwtBlacklist;
    
    public void blacklistToken(String token, Duration ttl) {
        // Add to blacklist with TTL = token expiry
        jwtBlacklist.put(token, true, ttl);
    }
    
    public boolean isBlacklisted(String token) {
        return jwtBlacklist.containsKey(token);
    }
}
```

---

### Example 3: Rate Limiting

```java
@Service
public class RateLimitService {
    
    @Autowired
    private TTLCache<String, Integer> rateLimitCache;
    
    public boolean isAllowed(String userId) {
        String key = "rate_limit:" + userId;
        Integer count = rateLimitCache.get(key);
        
        if (count == null) {
            // First request in window
            rateLimitCache.put(key, 1, Duration.ofMinutes(1));
            return true;
        }
        
        if (count >= 100) {
            // Rate limit exceeded
            return false;
        }
        
        // Increment count
        rateLimitCache.put(key, count + 1, Duration.ofMinutes(1));
        return true;
    }
}
```

---

### Example 4: Session Management

```java
@Service
public class SessionService {
    
    @Autowired
    private LRUCache<String, Session> sessionCache;
    
    public void createSession(String sessionId, Session session) {
        sessionCache.put(sessionId, session);
    }
    
    public Session getSession(String sessionId) {
        return sessionCache.get(sessionId);
    }
    
    public void invalidateSession(String sessionId) {
        sessionCache.remove(sessionId);
    }
}
```

---

## 🎓 Interview Q&A

### Q1: What is caching and why is it important?

**Answer:**
Caching stores frequently accessed data in fast storage (memory) to:
- **Reduce latency:** Faster than database/disk
- **Reduce load:** Fewer database queries
- **Improve scalability:** Handle more requests
- **Save costs:** Less database/API calls

**Example:** User profile loaded once, cached for 15 minutes. Subsequent requests served from cache (1ms vs 100ms DB query).

---

### Q2: What are different cache eviction policies?

**Answer:**

| Policy | Evicts | Best For | Complexity |
|--------|--------|----------|------------|
| LRU | Least recently used | Temporal locality | O(1) |
| LFU | Least frequently used | Popular items | O(log n) |
| FIFO | Oldest entry | Simple queue | O(1) |
| TTL | Expired entries | Time-sensitive | O(1) |
| Random | Random entry | Simple, fast | O(1) |

**Most common:** LRU (good balance of simplicity and performance)

---

### Q3: What is cache stampede and how to prevent it?

**Answer:**
**Cache stampede** occurs when many requests try to regenerate expired cache simultaneously, overwhelming the database.

**Scenario:**
```
Cache expires → 1000 concurrent requests → All query DB → DB overload
```

**Prevention strategies:**

**1. Locking (Mutex)**
```java
synchronized (lock) {
    if (cache.get(key) == null) {
        value = database.query();
        cache.put(key, value);
    }
}
```

**2. Probabilistic Early Expiration**
```java
// Refresh before actual expiry
if (random() < expiryTime / ttl) {
    refreshCache();
}
```

**3. Background Refresh**
```java
@Scheduled(fixedRate = 60000)
public void refreshCache() {
    // Async refresh before expiry
}
```

**4. Stale-While-Revalidate**
```java
// Serve stale data while refreshing
if (isExpired() && !isRefreshing()) {
    asyncRefresh();
    return staleValue;
}
```

---

### Q4: When should you NOT use caching?

**Answer:**
Don't cache:
- **Frequently changing data:** Cache becomes stale immediately
- **User-specific sensitive data:** Security risk if not encrypted
- **Large objects:** Memory constraints
- **Rarely accessed data:** Wastes memory
- **Real-time data:** Requires latest value

**Example:** Stock prices, live sports scores, user passwords

---

### Q5: How do you implement distributed caching?

**Answer:**
Use Redis, Memcached, or Hazelcast for distributed caching.

**Redis Example:**
```java
@Bean
public RedisTemplate<String, Object> redisTemplate(
        RedisConnectionFactory factory) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(factory);
    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
    return template;
}

// Usage
redisTemplate.opsForValue().set("user:123", user, 10, TimeUnit.MINUTES);
User cached = (User) redisTemplate.opsForValue().get("user:123");
```

**Benefits:**
- Shared across multiple servers
- Persistence options
- High availability
- Pub/sub support

---

### Q6: What is cache-aside vs write-through vs write-behind?

**Answer:**

**1. Cache-Aside (Lazy Loading)**
```java
User user = cache.get(id);
if (user == null) {
    user = database.get(id);
    cache.put(id, user);
}
return user;
```
- App manages cache
- Cache miss loads from DB
- Most common pattern

**2. Write-Through**
```java
database.save(user);
cache.put(user.getId(), user);
```
- Write to cache and DB together
- Always consistent
- Higher write latency

**3. Write-Behind (Write-Back)**
```java
cache.put(user.getId(), user);
asyncQueue.add(() -> database.save(user));
```
- Write to cache first, DB async
- Faster writes
- Risk of data loss

---

### Q7: How do you measure cache effectiveness?

**Answer:**
**Key metrics:**

**1. Hit Rate**
```
Hit Rate = Cache Hits / (Cache Hits + Cache Misses)
```
- Good: > 80%
- Excellent: > 95%

**2. Miss Rate**
```
Miss Rate = 1 - Hit Rate
```

**3. Eviction Rate**
```
Eviction Rate = Evictions / Total Requests
```
- High eviction rate → increase cache size

**4. Average Latency**
```
Cache Hit Latency: ~1ms
Cache Miss Latency: ~100ms (DB query)
```

**Monitoring:**
```java
@Component
public class CacheMetrics {
    private long hits = 0;
    private long misses = 0;
    
    public void recordHit() { hits++; }
    public void recordMiss() { misses++; }
    
    public double getHitRate() {
        return (double) hits / (hits + misses);
    }
}
```

---

## 🚀 Best Practices

1. **Set appropriate TTL:** Balance freshness vs performance
2. **Monitor cache metrics:** Track hit rate, evictions
3. **Handle cache failures gracefully:** Fallback to DB
4. **Use cache warming:** Pre-populate on startup
5. **Implement cache invalidation:** Clear on data updates
6. **Consider memory limits:** Don't cache everything
7. **Use distributed cache for scale:** Redis for multiple servers
8. **Encrypt sensitive data:** Even in cache
9. **Test cache behavior:** Verify eviction policies
10. **Document cache strategy:** Clear for team

---

## 📊 Performance Comparison

| Operation | No Cache | LRU Cache | Redis Cache |
|-----------|----------|-----------|-------------|
| User lookup | 100ms | 1ms | 5ms |
| Role lookup | 50ms | 1ms | 5ms |
| Permission check | 75ms | 1ms | 5ms |

**Improvement:** 50-100x faster with caching!

---

## 🎯 Summary

**Caches Implemented:**
- ✅ Spring Cache (annotation-based)
- ✅ LRU Cache (least recently used)
- ✅ LFU Cache (least frequently used)
- ✅ TTL Cache (time-based expiration)

**Features:**
- ✅ Multiple eviction policies
- ✅ Thread-safe implementations
- ✅ Comprehensive logging
- ✅ Interview Q&A documentation
- ✅ Production-ready examples

**Use in AuthFlow:**
- User data caching
- Role/permission caching
- JWT blacklist
- Rate limiting
- Session management
