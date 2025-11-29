package com.authflow.security.algorithms;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Date;
import java.util.Map;

/**
 * JWT Token Signing Algorithms demonstration.
 * 
 * <h2>JWT Structure:</h2>
 * 
 * <pre>
 * Header.Payload.Signature
 * 
 * Header: {"alg": "HS256", "typ": "JWT"}
 * Payload: {"sub": "user123", "exp": 1234567890, "iat": 1234567890}
 * Signature: HMACSHA256(base64UrlEncode(header) + "." + base64UrlEncode(payload), secret)
 * </pre>
 * 
 * <h2>Signing Algorithms:</h2>
 * 
 * <h3>1. HMAC (HS256, HS384, HS512)</h3>
 * <ul>
 * <li>Symmetric algorithm (same key for signing and verification)</li>
 * <li>Fast and simple</li>
 * <li>Secret must be kept secure on both client and server</li>
 * <li>Best for: Single server applications</li>
 * </ul>
 * 
 * <h3>2. RSA (RS256, RS384, RS512)</h3>
 * <ul>
 * <li>Asymmetric algorithm (private key signs, public key verifies)</li>
 * <li>Public key can be shared safely</li>
 * <li>Slower than HMAC</li>
 * <li>Best for: Distributed systems, microservices, third-party
 * verification</li>
 * </ul>
 * 
 * <h3>3. ECDSA (ES256, ES384, ES512)</h3>
 * <ul>
 * <li>Asymmetric algorithm using elliptic curves</li>
 * <li>Smaller keys than RSA with same security level</li>
 * <li>Faster than RSA</li>
 * <li>Best for: Mobile applications, IoT devices</li>
 * </ul>
 * 
 * <h2>Interview Q&A:</h2>
 * 
 * <p>
 * <b>Q:</b> What is JWT and how does it work?
 * </p>
 * <p>
 * <b>A:</b> JWT (JSON Web Token) is a compact, URL-safe token format for
 * securely
 * transmitting information between parties. It's stateless (server doesn't need
 * to store
 * session data) and contains all necessary information in the token itself.
 * </p>
 * 
 * <p>
 * <b>Q:</b> When should you use HMAC vs RSA for JWT?
 * </p>
 * <p>
 * <b>A:</b>
 * <ul>
 * <li>HMAC: When both token creation and verification happen on the same
 * server</li>
 * <li>RSA: When tokens are created by one service but verified by multiple
 * services,
 * or when you need to share public key with third parties</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <b>Q:</b> What are the security concerns with JWT?
 * </p>
 * <p>
 * <b>A:</b>
 * <ul>
 * <li>Token theft: Use HTTPS, httpOnly cookies, short expiration</li>
 * <li>Algorithm confusion: Validate algorithm in header</li>
 * <li>None algorithm attack: Reject tokens with "alg": "none"</li>
 * <li>Weak secrets: Use strong, random secrets (256+ bits for HS256)</li>
 * <li>Token size: JWTs can be large, increasing bandwidth</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <b>Q:</b> Where should you store JWT tokens in a web application?
 * </p>
 * <p>
 * <b>A:</b>
 * <ul>
 * <li>httpOnly cookies: Prevents XSS but vulnerable to CSRF (use CSRF
 * tokens)</li>
 * <li>localStorage: Vulnerable to XSS attacks</li>
 * <li>sessionStorage: Vulnerable to XSS, lost on tab close</li>
 * <li>Memory: Most secure but lost on refresh</li>
 * </ul>
 * Best practice: httpOnly, Secure, SameSite cookies with CSRF protection.
 * </p>
 * 
 * @author Shivam Srivastav
 */
@Component
public class JwtSigningAlgorithms {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * Generate JWT token using HMAC-SHA256 (symmetric).
     */
    public String generateHmacToken(String username, Map<String, Object> claims) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Validate and parse HMAC-signed JWT token.
     */
    public Claims validateHmacToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("Token expired", e);
        } catch (SignatureException e) {
            throw new RuntimeException("Invalid signature", e);
        } catch (MalformedJwtException e) {
            throw new RuntimeException("Malformed token", e);
        }
    }

    /**
     * Generate JWT token using RSA (asymmetric).
     * Private key signs, public key verifies.
     */
    public String generateRsaToken(String username, Map<String, Object> claims, PrivateKey privateKey) {
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    /**
     * Validate RSA-signed JWT token using public key.
     */
    public Claims validateRsaToken(String token, PublicKey publicKey) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Generate RSA key pair for demonstration.
     */
    public KeyPair generateRsaKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048); // 2048-bit key
        return keyPairGenerator.generateKeyPair();
    }

    /**
     * Generate ECDSA key pair for demonstration.
     */
    public KeyPair generateEcdsaKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
        keyPairGenerator.initialize(256); // 256-bit key (equivalent to 3072-bit RSA)
        return keyPairGenerator.generateKeyPair();
    }

    /**
     * Demonstrates different JWT signing algorithms.
     */
    public void demonstrateSigningAlgorithms(String username) throws NoSuchAlgorithmException {
        Map<String, Object> claims = Map.of("role", "USER", "email", "user@example.com");

        System.out.println("=== JWT Signing Algorithms Demonstration ===\n");

        // HMAC-SHA256
        long startTime = System.nanoTime();
        String hmacToken = generateHmacToken(username, claims);
        long hmacTime = System.nanoTime() - startTime;
        System.out.println("HMAC-SHA256 (Symmetric):");
        System.out.println("  Token: " + hmacToken);
        System.out.println("  Time: " + hmacTime / 1_000_000 + " ms");
        System.out.println("  Length: " + hmacToken.length() + " characters\n");

        // RSA
        KeyPair rsaKeyPair = generateRsaKeyPair();
        startTime = System.nanoTime();
        String rsaToken = generateRsaToken(username, claims, rsaKeyPair.getPrivate());
        long rsaTime = System.nanoTime() - startTime;
        System.out.println("RSA-SHA256 (Asymmetric):");
        System.out.println("  Token: " + rsaToken);
        System.out.println("  Time: " + rsaTime / 1_000_000 + " ms");
        System.out.println("  Length: " + rsaToken.length() + " characters\n");

        // Verification
        System.out.println("Verification:");
        Claims hmacClaims = validateHmacToken(hmacToken);
        System.out.println("  HMAC verified: " + hmacClaims.getSubject());
        Claims rsaClaims = validateRsaToken(rsaToken, rsaKeyPair.getPublic());
        System.out.println("  RSA verified: " + rsaClaims.getSubject());
    }
}
