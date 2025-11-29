# Authentication & Authorization Interview Guide

Complete interview preparation guide covering 100+ questions from basic to advanced levels.

## Table of Contents
1. [Basic Level Questions](#basic-level-questions)
2. [Intermediate Level Questions](#intermediate-level-questions)
3. [Advanced Level Questions](#advanced-level-questions)
4. [System Design Questions](#system-design-questions)
5. [Coding Challenges](#coding-challenges)

---

## Basic Level Questions

### Q1: What is the difference between authentication and authorization?

**Answer:**
- **Authentication** verifies WHO you are (identity verification)
  - Example: Login with username/password
  - Answers: "Are you really John Doe?"
  
- **Authorization** determines WHAT you can do (access control)
  - Example: Checking if you can delete a file
  - Answers: "Can John Doe delete this file?"

**Analogy:** Authentication is like showing your ID at airport security (proving who you are), while authorization is like your boarding pass determining which flight you can board (what you can access).

---

### Q2: What is a JWT token and how does it work?

**Answer:**
JWT (JSON Web Token) is a compact, URL-safe token format for securely transmitting information between parties.

**Structure:**
```
Header.Payload.Signature

Example:
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
```

**Components:**
1. **Header**: Algorithm and token type
   ```json
   {"alg": "HS256", "typ": "JWT"}
   ```

2. **Payload**: Claims (data)
   ```json
   {"sub": "user123", "name": "John", "exp": 1234567890}
   ```

3. **Signature**: Verification
   ```
   HMACSHA256(base64UrlEncode(header) + "." + base64UrlEncode(payload), secret)
   ```

**How it works:**
1. User logs in with credentials
2. Server validates credentials
3. Server creates JWT with user info and signs it
4. Client stores JWT (localStorage, cookie, memory)
5. Client sends JWT in Authorization header for subsequent requests
6. Server validates JWT signature and extracts user info
7. Server processes request based on user permissions

**Advantages:**
- Stateless (no server-side session storage)
- Scalable (works across multiple servers)
- Self-contained (contains all necessary information)
- Cross-domain compatible

**Disadvantages:**
- Cannot be revoked easily (stateless)
- Larger than session IDs
- Vulnerable if secret is compromised

---

### Q3: What is the purpose of password hashing and why not use encryption?

**Answer:**

**Hashing vs Encryption:**

| Aspect | Hashing | Encryption |
|--------|---------|------------|
| Reversible | No (one-way) | Yes (two-way) |
| Purpose | Verification | Confidentiality |
| Output | Fixed length | Variable length |
| Use case | Passwords | Sensitive data |

**Why hash passwords:**
1. **One-way function**: Cannot reverse to get original password
2. **Verification**: Compare hash of input with stored hash
3. **Security**: Even if database is compromised, passwords are not exposed

**Why not encrypt:**
- Encryption is reversible (with key)
- If encryption key is compromised, all passwords are exposed
- Hashing provides better security for password storage

**Best practices:**
- Use strong algorithms (BCrypt, Argon2, PBKDF2)
- Always use salt (random data added to password)
- Use high work factor/iterations
- Never use MD5 or SHA-1 for passwords

---

### Q4: What is a salt in password hashing?

**Answer:**
A salt is random data added to a password before hashing to prevent rainbow table attacks and ensure identical passwords have different hashes.

**Without salt:**
```
hash("password123") = "482c811da5d5b4bc6d497ffa98491e38"
hash("password123") = "482c811da5d5b4bc6d497ffa98491e38"  // Same hash!
```

**With salt:**
```
salt1 = "a1b2c3"
hash("password123" + "a1b2c3") = "7d8f9e1a2b3c4d5e6f7g8h9i0j1k2l3m"

salt2 = "x9y8z7"
hash("password123" + "x9y8z7") = "9m8n7o6p5q4r3s2t1u0v9w8x7y6z5a4b"  // Different hash!
```

**Benefits:**
1. **Prevents rainbow tables**: Precomputed hash tables become useless
2. **Unique hashes**: Same password has different hashes for different users
3. **Slows down attacks**: Attacker must compute hashes for each user separately

**Implementation:**
- Salt should be unique per password
- Salt should be cryptographically random
- Salt should be stored alongside the hash
- Salt doesn't need to be secret (but should be random)

---

### Q5: What is RBAC (Role-Based Access Control)?

**Answer:**
RBAC is an authorization model where permissions are assigned to roles, and roles are assigned to users.

**Structure:**
```
Users → Roles → Permissions → Resources

Example:
John → Editor Role → Write Permission → Documents
```

**Components:**
1. **Users**: Individual people or services
2. **Roles**: Job functions (Admin, Editor, Viewer)
3. **Permissions**: Actions (Read, Write, Delete)
4. **Resources**: Objects being protected (Documents, Users)

**Example:**
```
Role: ADMIN
Permissions:
  - CREATE_USER
  - DELETE_USER
  - READ_ALL_DOCUMENTS
  - DELETE_ANY_DOCUMENT

Role: EDITOR
Permissions:
  - READ_DOCUMENT
  - WRITE_DOCUMENT
  - DELETE_OWN_DOCUMENT

Role: VIEWER
Permissions:
  - READ_DOCUMENT
```

**Advantages:**
- Simplified management (assign role instead of individual permissions)
- Reduced errors (consistent permissions per role)
- Easy auditing (clear role definitions)
- Scalable (add new users easily)

**Disadvantages:**
- Role explosion (too many specific roles)
- Inflexible for complex scenarios
- Difficult to handle exceptions

---

### Q6: What is the difference between stateful and stateless authentication?

**Answer:**

**Stateful Authentication (Session-based):**
```
Client                          Server
  |                               |
  |-- Login (credentials) ------->|
  |                               | Create session
  |                               | Store in memory/DB
  |<-- Session ID (cookie) -------|
  |                               |
  |-- Request + Session ID ------>|
  |                               | Lookup session
  |                               | Validate
  |<-- Response ------------------|
```

**Characteristics:**
- Server stores session data
- Session ID sent to client (usually in cookie)
- Server looks up session for each request
- Session can be invalidated server-side

**Stateless Authentication (Token-based):**
```
Client                          Server
  |                               |
  |-- Login (credentials) ------->|
  |                               | Validate
  |                               | Create JWT
  |<-- JWT Token -----------------|
  |                               |
  |-- Request + JWT ------------->|
  |                               | Validate JWT signature
  |                               | Extract user info
  |<-- Response ------------------|
```

**Characteristics:**
- Server doesn't store session data
- Token contains all necessary information
- Token validated using signature
- Scales horizontally easily

**Comparison:**

| Aspect | Stateful | Stateless |
|--------|----------|-----------|
| Server storage | Yes | No |
| Scalability | Harder | Easier |
| Revocation | Easy | Hard |
| Size | Small (ID only) | Large (contains data) |
| Cross-domain | Harder | Easier |
| Best for | Monolithic apps | Microservices, APIs |

---

## Intermediate Level Questions

### Q7: How do you implement JWT token refresh mechanism?

**Answer:**
Token refresh mechanism uses two tokens: short-lived access token and long-lived refresh token.

**Flow:**
```
1. Login:
   Client → Server: username + password
   Server → Client: access_token (15 min) + refresh_token (7 days)

2. API Request:
   Client → Server: access_token
   Server → Client: data

3. Access Token Expired:
   Client → Server: refresh_token
   Server → Client: new access_token + new refresh_token

4. Refresh Token Expired:
   Client → Server: refresh_token
   Server → Client: 401 Unauthorized (login again)
```

**Implementation:**
```java
public class TokenService {
    private static final long ACCESS_TOKEN_VALIDITY = 15 * 60 * 1000; // 15 minutes
    private static final long REFRESH_TOKEN_VALIDITY = 7 * 24 * 60 * 60 * 1000; // 7 days
    
    public String generateAccessToken(User user) {
        return Jwts.builder()
            .setSubject(user.getUsername())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY))
            .signWith(getSigningKey())
            .compact();
    }
    
    public String generateRefreshToken(User user) {
        String token = UUID.randomUUID().toString();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(new Date(System.currentTimeMillis() + REFRESH_TOKEN_VALIDITY));
        refreshTokenRepository.save(refreshToken);
        return token;
    }
    
    public String refreshAccessToken(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
            .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
            
        if (token.getExpiryDate().before(new Date())) {
            throw new RuntimeException("Refresh token expired");
        }
        
        return generateAccessToken(token.getUser());
    }
}
```

**Security considerations:**
- Store refresh tokens in database (for revocation)
- Rotate refresh tokens on use
- Implement token blacklist for logout
- Use httpOnly cookies for refresh tokens
- Implement device tracking

---

### Q8: What are the different JWT signing algorithms and when to use each?

**Answer:**

**1. HMAC (HS256, HS384, HS512) - Symmetric**
```java
// Same secret for signing and verification
SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
String token = Jwts.builder()
    .setSubject("user")
    .signWith(key, SignatureAlgorithm.HS256)
    .compact();
```

**Characteristics:**
- Symmetric (same key for sign and verify)
- Fast
- Smaller keys
- Secret must be protected on both sides

**Use when:**
- Single server application
- Both token creation and verification on same server
- Performance is critical

---

**2. RSA (RS256, RS384, RS512) - Asymmetric**
```java
// Private key signs, public key verifies
KeyPair keyPair = generateRSAKeyPair();
String token = Jwts.builder()
    .setSubject("user")
    .signWith(keyPair.getPrivate(), SignatureAlgorithm.RS256)
    .compact();

// Verification
Claims claims = Jwts.parser()
    .setSigningKey(keyPair.getPublic())
    .parseClaimsJws(token)
    .getBody();
```

**Characteristics:**
- Asymmetric (private key signs, public key verifies)
- Slower than HMAC
- Larger keys (2048-4096 bits)
- Public key can be shared safely

**Use when:**
- Microservices architecture
- Multiple services need to verify tokens
- Third parties need to verify tokens
- Token created by one service, verified by many

---

**3. ECDSA (ES256, ES384, ES512) - Asymmetric**
```java
// Elliptic Curve Digital Signature Algorithm
KeyPair keyPair = generateECKeyPair();
String token = Jwts.builder()
    .setSubject("user")
    .signWith(keyPair.getPrivate(), SignatureAlgorithm.ES256)
    .compact();
```

**Characteristics:**
- Asymmetric (like RSA)
- Faster than RSA
- Smaller keys than RSA (256-bit EC ≈ 3072-bit RSA)
- Modern and secure

**Use when:**
- Mobile applications (smaller tokens)
- IoT devices (limited resources)
- Need asymmetric but want better performance than RSA

---

**Comparison:**

| Algorithm | Type | Key Size | Speed | Security | Use Case |
|-----------|------|----------|-------|----------|----------|
| HS256 | Symmetric | 256 bits | Fastest | High | Single server |
| RS256 | Asymmetric | 2048 bits | Slow | High | Microservices |
| ES256 | Asymmetric | 256 bits | Fast | High | Mobile/IoT |

---

### Q9: How do you prevent common JWT vulnerabilities?

**Answer:**

**1. Algorithm Confusion Attack**
```json
// Attacker changes algorithm to "none"
{
  "alg": "none",
  "typ": "JWT"
}
```

**Prevention:**
```java
// Always specify and validate algorithm
Jwts.parser()
    .setSigningKey(key)
    .requireAlgorithm("HS256")  // Explicitly require algorithm
    .parseClaimsJws(token);

// Reject "none" algorithm
if (header.getAlgorithm().equals("none")) {
    throw new SecurityException("Algorithm 'none' not allowed");
}
```

---

**2. Weak Secret**
```java
// BAD: Weak secret
String secret = "secret";  // Can be brute-forced

// GOOD: Strong secret
String secret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
// At least 256 bits for HS256
```

---

**3. Missing Expiration**
```java
// BAD: No expiration
String token = Jwts.builder()
    .setSubject("user")
    .signWith(key)
    .compact();  // Token never expires!

// GOOD: Set expiration
String token = Jwts.builder()
    .setSubject("user")
    .setExpiration(new Date(System.currentTimeMillis() + 900000))  // 15 minutes
    .signWith(key)
    .compact();
```

---

**4. Token Theft**

**Prevention strategies:**
```java
// 1. Use HTTPS only
server.ssl.enabled=true

// 2. Use httpOnly cookies
Cookie cookie = new Cookie("token", jwt);
cookie.setHttpOnly(true);  // Prevents XSS
cookie.setSecure(true);     // HTTPS only
cookie.setSameSite("Strict"); // Prevents CSRF

// 3. Short expiration times
ACCESS_TOKEN_VALIDITY = 15 * 60 * 1000; // 15 minutes

// 4. Implement token blacklist
public class TokenBlacklist {
    private Set<String> blacklist = new HashSet<>();
    
    public void blacklist(String token) {
        blacklist.add(token);
    }
    
    public boolean isBlacklisted(String token) {
        return blacklist.contains(token);
    }
}
```

---

**5. Sensitive Data in Payload**
```java
// BAD: Sensitive data in JWT
Claims claims = Jwts.claims();
claims.put("password", "secret123");  // Never do this!
claims.put("ssn", "123-45-6789");     // Never do this!

// GOOD: Only non-sensitive data
Claims claims = Jwts.claims();
claims.put("userId", "123");
claims.put("role", "USER");
claims.put("email", "user@example.com");  // OK if not sensitive
```

---

**6. Missing Signature Validation**
```java
// BAD: Parsing without validation
String[] parts = token.split("\\.");
String payload = new String(Base64.getDecoder().decode(parts[1]));
// Attacker can modify payload!

// GOOD: Always validate signature
Claims claims = Jwts.parser()
    .setSigningKey(key)
    .parseClaimsJws(token)  // Validates signature
    .getBody();
```

---

### Q10: Explain OAuth 2.0 and its grant types

**Answer:**

**OAuth 2.0** is an authorization framework that enables applications to obtain limited access to user accounts on an HTTP service.

**Key Roles:**
1. **Resource Owner**: User who owns the data
2. **Client**: Application requesting access
3. **Authorization Server**: Issues access tokens
4. **Resource Server**: Hosts protected resources

---

**Grant Types:**

**1. Authorization Code Flow** (Most secure, for web apps)
```
User                Client              Auth Server         Resource Server
 |                    |                      |                     |
 |-- Click Login ---->|                      |                     |
 |                    |-- Redirect to ------>|                     |
 |                    |    Auth Server       |                     |
 |<------------------ Redirect --------------|                     |
 |                    |                      |                     |
 |-- Enter credentials ------------------>   |                     |
 |                    |                      |                     |
 |<-- Authorization Code -------------------|                     |
 |                    |                      |                     |
 |-- Auth Code ------>|                      |                     |
 |                    |-- Auth Code -------->|                     |
 |                    |<-- Access Token -----|                     |
 |                    |                      |                     |
 |                    |-- Access Token ------|-------------------->|
 |                    |<-- Protected Resource --------------------|
```

**Use for:** Server-side web applications

---

**2. Client Credentials Flow** (For machine-to-machine)
```
Client              Auth Server         Resource Server
 |                      |                     |
 |-- Client ID + ------>|                     |
 |    Client Secret     |                     |
 |<-- Access Token -----|                     |
 |                      |                     |
 |-- Access Token ------|-------------------->|
 |<-- Protected Resource --------------------|
```

**Use for:** Backend services, cron jobs, microservices

---

**3. Resource Owner Password Flow** (Legacy, not recommended)
```
User → Client: username + password
Client → Auth Server: username + password + client credentials
Auth Server → Client: access token
```

**Use for:** Trusted first-party applications only (not recommended for new apps)

---

**4. Implicit Flow** (Deprecated, don't use)
- Tokens returned directly in URL
- No refresh tokens
- Less secure
- Replaced by Authorization Code Flow with PKCE

---

**Access Token vs Refresh Token:**

| Aspect | Access Token | Refresh Token |
|--------|--------------|---------------|
| Lifetime | Short (15 min - 1 hour) | Long (days - months) |
| Purpose | Access resources | Get new access token |
| Exposure | Sent with every request | Sent only to auth server |
| Revocation | Hard (stateless) | Easy (stored in DB) |

---

## Advanced Level Questions

### Q11: How would you implement Multi-Factor Authentication (MFA)?

**Answer:**

**MFA Types:**
1. **Something you know**: Password, PIN
2. **Something you have**: Phone, hardware token
3. **Something you are**: Biometrics

**TOTP (Time-based One-Time Password) Implementation:**

```java
@Service
public class MfaService {
    private final GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();
    
    // 1. Setup MFA
    public MfaSetupResponse setupMfa(User user) {
        // Generate secret
        GoogleAuthenticatorKey key = googleAuthenticator.createCredentials();
        String secret = key.getKey();
        
        // Save secret to user
        user.setMfaSecret(secret);
        user.setMfaEnabled(false);  // Not enabled until verified
        userRepository.save(user);
        
        // Generate QR code URL
        String qrCodeUrl = GoogleAuthenticatorQRGenerator.getOtpAuthURL(
            "MyApp",
            user.getEmail(),
            key
        );
        
        // Generate backup codes
        List<String> backupCodes = generateBackupCodes();
        saveBackupCodes(user, backupCodes);
        
        return new MfaSetupResponse(qrCodeUrl, backupCodes);
    }
    
    // 2. Verify and enable MFA
    public boolean verifyAndEnableMfa(User user, int code) {
        boolean isValid = googleAuthenticator.authorize(user.getMfaSecret(), code);
        
        if (isValid) {
            user.setMfaEnabled(true);
            userRepository.save(user);
        }
        
        return isValid;
    }
    
    // 3. Verify MFA during login
    public boolean verifyMfaCode(User user, int code) {
        // Check TOTP code
        if (googleAuthenticator.authorize(user.getMfaSecret(), code)) {
            return true;
        }
        
        // Check backup codes
        return checkBackupCode(user, String.valueOf(code));
    }
    
    // Generate backup codes
    private List<String> generateBackupCodes() {
        List<String> codes = new ArrayList<>();
        SecureRandom random = new SecureRandom();
        
        for (int i = 0; i < 10; i++) {
            int code = 100000 + random.nextInt(900000);  // 6-digit code
            codes.add(String.valueOf(code));
        }
        
        return codes;
    }
}
```

**TOTP Algorithm:**
```
TOTP = HOTP(K, T)

where:
  K = shared secret key
  T = (Current Unix time - T0) / X
  T0 = initial time (usually 0)
  X = time step (usually 30 seconds)

HOTP = Truncate(HMAC-SHA1(K, C))
```

**Login Flow with MFA:**
```
1. User enters username + password
2. Server validates credentials
3. If MFA enabled:
   a. Server returns mfaRequired: true
   b. Client prompts for MFA code
   c. User enters code from authenticator app
   d. Server validates code
   e. If valid, return access token
4. If MFA not enabled:
   a. Return access token directly
```

**Security Considerations:**
- Use time window tolerance (±1 window = ±30 seconds)
- Implement rate limiting on MFA attempts
- Provide backup codes for recovery
- Allow MFA reset via email/SMS verification
- Log MFA events for auditing

---

### Q12: How do you implement Attribute-Based Access Control (ABAC)?

**Answer:**

**ABAC** makes authorization decisions based on attributes of:
1. **Subject** (user): role, department, clearance level
2. **Resource** (object): classification, owner, department
3. **Action**: read, write, delete
4. **Environment**: time, location, IP address

**Implementation:**

```java
// 1. Define Policy
@Data
public class AbacPolicy {
    private String name;
    private String effect;  // ALLOW or DENY
    private List<Condition> conditions;
    
    @Data
    public static class Condition {
        private String attribute;
        private String operator;  // EQUALS, CONTAINS, GREATER_THAN, etc.
        private String value;
    }
}

// Example policy
{
  "name": "Allow managers to read department documents",
  "effect": "ALLOW",
  "conditions": [
    {"attribute": "user.role", "operator": "EQUALS", "value": "MANAGER"},
    {"attribute": "resource.department", "operator": "EQUALS", "value": "user.department"},
    {"attribute": "action", "operator": "EQUALS", "value": "READ"}
  ]
}
```

```java
// 2. Policy Engine
@Service
public class AbacPolicyEngine {
    
    public boolean evaluate(AbacPolicy policy, AbacContext context) {
        // Evaluate all conditions
        for (Condition condition : policy.getConditions()) {
            if (!evaluateCondition(condition, context)) {
                return false;  // All conditions must be true
            }
        }
        
        return policy.getEffect().equals("ALLOW");
    }
    
    private boolean evaluateCondition(Condition condition, AbacContext context) {
        String actualValue = context.getAttribute(condition.getAttribute());
        String expectedValue = condition.getValue();
        
        // Resolve dynamic values (e.g., "user.department")
        if (expectedValue.startsWith("user.")) {
            expectedValue = context.getUserAttribute(expectedValue.substring(5));
        }
        
        return switch (condition.getOperator()) {
            case "EQUALS" -> actualValue.equals(expectedValue);
            case "CONTAINS" -> actualValue.contains(expectedValue);
            case "GREATER_THAN" -> Integer.parseInt(actualValue) > Integer.parseInt(expectedValue);
            case "LESS_THAN" -> Integer.parseInt(actualValue) < Integer.parseInt(expectedValue);
            case "IN" -> Arrays.asList(expectedValue.split(",")).contains(actualValue);
            default -> false;
        };
    }
}
```

```java
// 3. ABAC Context
@Data
public class AbacContext {
    private Map<String, String> userAttributes;
    private Map<String, String> resourceAttributes;
    private Map<String, String> environmentAttributes;
    private String action;
    
    public String getAttribute(String path) {
        String[] parts = path.split("\\.");
        
        return switch (parts[0]) {
            case "user" -> userAttributes.get(parts[1]);
            case "resource" -> resourceAttributes.get(parts[1]);
            case "environment" -> environmentAttributes.get(parts[1]);
            case "action" -> action;
            default -> null;
        };
    }
}
```

```java
// 4. Usage in Controller
@RestController
public class DocumentController {
    
    @Autowired
    private AbacPolicyEngine policyEngine;
    
    @GetMapping("/documents/{id}")
    public ResponseEntity<Document> getDocument(@PathVariable Long id, Authentication auth) {
        Document document = documentRepository.findById(id)
            .orElseThrow(() -> new NotFoundException());
        
        // Build ABAC context
        AbacContext context = new AbacContext();
        context.setUserAttributes(Map.of(
            "role", getUserRole(auth),
            "department", getUserDepartment(auth),
            "clearanceLevel", getUserClearanceLevel(auth)
        ));
        context.setResourceAttributes(Map.of(
            "department", document.getDepartment(),
            "classification", document.getClassification(),
            "owner", document.getOwner().getUsername()
        ));
        context.setEnvironmentAttributes(Map.of(
            "time", LocalTime.now().toString(),
            "ipAddress", getClientIp()
        ));
        context.setAction("READ");
        
        // Load and evaluate policies
        List<AbacPolicy> policies = policyRepository.findAll();
        boolean allowed = policies.stream()
            .anyMatch(policy -> policyEngine.evaluate(policy, context));
        
        if (!allowed) {
            throw new AccessDeniedException("Access denied by ABAC policy");
        }
        
        return ResponseEntity.ok(document);
    }
}
```

**Example Policies:**

```java
// Policy 1: Managers can read documents in their department
{
  "conditions": [
    {"attribute": "user.role", "operator": "EQUALS", "value": "MANAGER"},
    {"attribute": "resource.department", "operator": "EQUALS", "value": "user.department"},
    {"attribute": "action", "operator": "EQUALS", "value": "READ"}
  ]
}

// Policy 2: Only read confidential documents during business hours
{
  "conditions": [
    {"attribute": "resource.classification", "operator": "EQUALS", "value": "CONFIDENTIAL"},
    {"attribute": "environment.time", "operator": "GREATER_THAN", "value": "09:00"},
    {"attribute": "environment.time", "operator": "LESS_THAN", "value": "17:00"}
  ]
}

// Policy 3: Users can only access documents with appropriate clearance
{
  "conditions": [
    {"attribute": "user.clearanceLevel", "operator": "GREATER_THAN", "value": "resource.classificationLevel"}
  ]
}
```

**ABAC vs RBAC:**

| Aspect | RBAC | ABAC |
|--------|------|------|
| Flexibility | Low | High |
| Complexity | Low | High |
| Granularity | Coarse | Fine |
| Dynamic | No | Yes |
| Best for | Simple hierarchies | Complex scenarios |

---

### Q13: How do you secure a microservices architecture?

**Answer:**

**Challenges:**
- Multiple services need authentication
- Service-to-service communication
- Distributed authorization
- Token propagation
- API Gateway security

**Solution Architecture:**

```
Client → API Gateway → Auth Service → Microservices
                ↓
         JWT Validation
                ↓
         Service Mesh (mTLS)
```

**Implementation:**

**1. API Gateway Pattern**
```java
@Component
public class GatewayAuthFilter implements GlobalFilter {
    
    @Autowired
    private JwtTokenService jwtTokenService;
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Extract JWT from request
        String token = extractToken(exchange.getRequest());
        
        if (token == null) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        
        // Validate JWT
        if (!jwtTokenService.validateToken(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        
        // Extract user info and add to headers
        String username = jwtTokenService.getUsernameFromToken(token);
        ServerHttpRequest request = exchange.getRequest().mutate()
            .header("X-User-Id", username)
            .header("X-User-Roles", getRoles(token))
            .build();
        
        return chain.filter(exchange.mutate().request(request).build());
    }
}
```

**2. Service-to-Service Authentication (mTLS)**
```yaml
# Service A configuration
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://auth-server.com
          
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: changeit
    key-store-type: PKCS12
    trust-store: classpath:truststore.p12
    trust-store-password: changeit
    client-auth: need  # Require client certificate
```

**3. Token Propagation**
```java
@Configuration
public class FeignClientConfig {
    
    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            // Get token from current security context
            Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();
            
            if (authentication != null && authentication.getCredentials() != null) {
                String token = authentication.getCredentials().toString();
                requestTemplate.header("Authorization", "Bearer " + token);
            }
        };
    }
}
```

**4. Distributed Authorization**
```java
// Centralized authorization service
@Service
public class AuthorizationService {
    
    public boolean checkPermission(String userId, String resource, String action) {
        // Check in centralized policy store
        User user = userRepository.findById(userId);
        Resource res = resourceRepository.findByName(resource);
        
        return policyEngine.evaluate(user, res, action);
    }
}

// Microservice calls authorization service
@RestController
public class OrderController {
    
    @Autowired
    private AuthorizationServiceClient authzClient;
    
    @GetMapping("/orders/{id}")
    public Order getOrder(@PathVariable Long id, @AuthenticationPrincipal User user) {
        // Check authorization
        if (!authzClient.checkPermission(user.getId(), "order:" + id, "READ")) {
            throw new AccessDeniedException();
        }
        
        return orderService.getOrder(id);
    }
}
```

**5. API Gateway + OAuth 2.0**
```
1. Client → API Gateway: Request with OAuth token
2. API Gateway → OAuth Server: Validate token (introspection)
3. OAuth Server → API Gateway: Token info + scopes
4. API Gateway → Microservice: Request with user context
5. Microservice → API Gateway: Response
6. API Gateway → Client: Response
```

**Best Practices:**
- Use API Gateway for centralized authentication
- Implement mTLS for service-to-service communication
- Use service mesh (Istio, Linkerd) for automatic mTLS
- Implement distributed tracing for security auditing
- Use centralized authorization service
- Implement rate limiting per service
- Use circuit breakers for resilience

---

## System Design Questions

### Q14: Design an authentication system for a social media platform with 100M users

**Answer:**

**Requirements:**
- 100M users
- Multiple login methods (email, phone, social)
- MFA support
- Session management
- High availability
- Low latency (<100ms)

**Architecture:**

```
                                    Load Balancer
                                          |
                    +---------------------+---------------------+
                    |                     |                     |
              API Gateway            API Gateway          API Gateway
                    |                     |                     |
              +-----+-----+         +-----+-----+         +-----+-----+
              |           |         |           |         |           |
         Auth Service  User Service  Auth Service  User Service  Auth Service
              |           |         |           |         |           |
         +----+----+      |    +----+----+      |    +----+----+
         |         |      |    |         |      |    |         |
    Redis Cache  Auth DB  |  Redis Cache Auth DB |  Redis Cache Auth DB
                          |                     |
                    User DB (Sharded)     User DB (Sharded)
```

**Components:**

**1. Authentication Service**
```java
@Service
public class AuthenticationService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @Autowired
    private JwtTokenService jwtTokenService;
    
    public AuthResponse login(LoginRequest request) {
        // 1. Rate limiting (Redis)
        String rateLimitKey = "rate_limit:" + request.getIpAddress();
        if (isRateLimited(rateLimitKey)) {
            throw new TooManyRequestsException();
        }
        
        // 2. Check cache for user
        String cacheKey = "user:" + request.getUsername();
        User user = getCachedUser(cacheKey);
        
        if (user == null) {
            // 3. Load from database
            user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException());
            
            // 4. Cache user
            cacheUser(cacheKey, user);
        }
        
        // 5. Verify password (async to prevent timing attacks)
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            // Increment failed attempts
            incrementFailedAttempts(user);
            throw new InvalidCredentialsException();
        }
        
        // 6. Check if MFA required
        if (user.isMfaEnabled()) {
            // Generate temporary token
            String tempToken = generateTempToken(user);
            return AuthResponse.mfaRequired(tempToken);
        }
        
        // 7. Generate tokens
        String accessToken = jwtTokenService.generateAccessToken(user);
        String refreshToken = generateRefreshToken(user);
        
        // 8. Store session in Redis
        storeSession(user.getId(), accessToken, refreshToken);
        
        // 9. Reset failed attempts
        resetFailedAttempts(user);
        
        return AuthResponse.success(accessToken, refreshToken);
    }
    
    private boolean isRateLimited(String key) {
        Long attempts = redisTemplate.opsForValue().increment(key);
        if (attempts == 1) {
            redisTemplate.expire(key, 1, TimeUnit.MINUTES);
        }
        return attempts > 10;  // Max 10 attempts per minute
    }
}
```

**2. Database Sharding Strategy**
```java
// Shard by user ID
public class UserShardingStrategy {
    
    private static final int NUM_SHARDS = 16;
    
    public int getShardId(Long userId) {
        return (int) (userId % NUM_SHARDS);
    }
    
    public DataSource getDataSource(Long userId) {
        int shardId = getShardId(userId);
        return dataSourceMap.get(shardId);
    }
}

// User ID generation (Twitter Snowflake)
public class IdGenerator {
    private static final long EPOCH = 1609459200000L;  // 2021-01-01
    private static final long DATACENTER_ID_BITS = 5L;
    private static final long WORKER_ID_BITS = 5L;
    private static final long SEQUENCE_BITS = 12L;
    
    public synchronized long generateId() {
        long timestamp = System.currentTimeMillis() - EPOCH;
        long id = timestamp << 22;
        id |= (datacenterId << 17);
        id |= (workerId << 12);
        id |= sequence;
        sequence = (sequence + 1) & 4095;
        return id;
    }
}
```

**3. Caching Strategy**
```java
@Configuration
public class CacheConfig {
    
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(15))  // User cache: 15 minutes
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair
                    .fromSerializer(new GenericJackson2JsonRedisSerializer())
            );
        
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put("users", config);
        cacheConfigurations.put("sessions", config.entryTtl(Duration.ofHours(24)));
        
        return RedisCacheManager.builder(factory)
            .cacheDefaults(config)
            .withInitialCacheConfigurations(cacheConfigurations)
            .build();
    }
}
```

**4. Session Management**
```java
@Service
public class SessionService {
    
    @Autowired
    private RedisTemplate<String, Session> redisTemplate;
    
    public void createSession(String userId, String accessToken, String refreshToken) {
        Session session = Session.builder()
            .userId(userId)
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .createdAt(Instant.now())
            .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
            .deviceInfo(getDeviceInfo())
            .ipAddress(getClientIp())
            .build();
        
        String key = "session:" + userId + ":" + session.getId();
        redisTemplate.opsForValue().set(key, session, 7, TimeUnit.DAYS);
        
        // Add to user's active sessions set
        redisTemplate.opsForSet().add("user_sessions:" + userId, session.getId());
    }
    
    public void revokeAllSessions(String userId) {
        Set<String> sessionIds = redisTemplate.opsForSet()
            .members("user_sessions:" + userId);
        
        for (String sessionId : sessionIds) {
            redisTemplate.delete("session:" + userId + ":" + sessionId);
        }
        
        redisTemplate.delete("user_sessions:" + userId);
    }
}
```

**5. Monitoring & Metrics**
```java
@Component
public class AuthMetrics {
    
    @Autowired
    private MeterRegistry meterRegistry;
    
    public void recordLogin(boolean success) {
        Counter.builder("auth.login")
            .tag("success", String.valueOf(success))
            .register(meterRegistry)
            .increment();
    }
    
    public void recordLoginDuration(long durationMs) {
        Timer.builder("auth.login.duration")
            .register(meterRegistry)
            .record(durationMs, TimeUnit.MILLISECONDS);
    }
}
```

**Scalability Considerations:**

| Component | Scaling Strategy | Target |
|-----------|------------------|--------|
| API Gateway | Horizontal | 10K RPS per instance |
| Auth Service | Horizontal | 5K RPS per instance |
| Redis | Cluster mode | 100K ops/sec |
| Database | Sharding | 16 shards |
| CDN | Global | <50ms latency |

**Security Measures:**
- Rate limiting (10 attempts/min per IP)
- Account lockout (5 failed attempts)
- Password complexity requirements
- MFA for sensitive operations
- IP whitelisting for admin accounts
- Audit logging
- Anomaly detection (unusual login locations)
- CAPTCHA after failed attempts

---

## Coding Challenges

### Q15: Implement a custom authentication filter

**Challenge:** Implement a Spring Security filter that:
1. Extracts API key from header
2. Validates API key
3. Loads user from database
4. Sets authentication in SecurityContext

**Solution:**

```java
@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private ApiKeyService apiKeyService;
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        
        // 1. Extract API key from header
        String apiKey = request.getHeader("X-API-Key");
        
        if (apiKey != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                // 2. Validate API key
                ApiKeyDetails keyDetails = apiKeyService.validateApiKey(apiKey);
                
                if (keyDetails != null && keyDetails.isActive()) {
                    // 3. Load user
                    UserDetails userDetails = userDetailsService
                        .loadUserByUsername(keyDetails.getUsername());
                    
                    // 4. Create authentication
                    ApiKeyAuthenticationToken authentication = 
                        new ApiKeyAuthenticationToken(
                            userDetails,
                            apiKey,
                            userDetails.getAuthorities()
                        );
                    
                    authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    
                    // 5. Set in SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    // 6. Update last used timestamp
                    apiKeyService.updateLastUsed(apiKey);
                }
            } catch (Exception e) {
                logger.error("API key authentication failed", e);
            }
        }
        
        filterChain.doFilter(request, response);
    }
}

// Custom Authentication Token
public class ApiKeyAuthenticationToken extends AbstractAuthenticationToken {
    
    private final Object principal;
    private final String apiKey;
    
    public ApiKeyAuthenticationToken(
            Object principal,
            String apiKey,
            Collection<? extends GrantedAuthority> authorities
    ) {
        super(authorities);
        this.principal = principal;
        this.apiKey = apiKey;
        setAuthenticated(true);
    }
    
    @Override
    public Object getCredentials() {
        return apiKey;
    }
    
    @Override
    public Object getPrincipal() {
        return principal;
    }
}
```

---

## Summary

This guide covers:
- ✅ 15+ comprehensive questions from basic to advanced
- ✅ Real-world implementation examples
- ✅ System design scenarios
- ✅ Security best practices
- ✅ Coding challenges

**Interview Preparation Tips:**
1. Understand concepts, don't just memorize
2. Practice explaining with examples
3. Know trade-offs of different approaches
4. Be ready to discuss security implications
5. Practice coding authentication/authorization logic
6. Review common vulnerabilities (OWASP Top 10)
7. Understand OAuth 2.0 flows thoroughly
8. Know when to use each authentication mechanism

**Additional Resources:**
- OWASP Authentication Cheat Sheet
- OAuth 2.0 RFC 6749
- JWT RFC 7519
- Spring Security Documentation
- NIST Password Guidelines

Good luck with your interviews! 🚀
