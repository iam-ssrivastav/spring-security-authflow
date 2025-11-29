package com.authflow.auth.jwt;

import com.authflow.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * JWT Token Service for generating and validating JWT tokens.
 * 
 * <h2>JWT Best Practices:</h2>
 * <ul>
 * <li>Use strong secrets (256+ bits for HS256)</li>
 * <li>Set appropriate expiration times</li>
 * <li>Include minimal claims (avoid sensitive data)</li>
 * <li>Validate signature, expiration, and issuer</li>
 * <li>Use HTTPS to prevent token interception</li>
 * <li>Store tokens securely (httpOnly cookies)</li>
 * </ul>
 * 
 * <h2>Interview Q&A:</h2>
 * 
 * <p>
 * <b>Q:</b> What claims should you include in a JWT?
 * </p>
 * <p>
 * <b>A:</b> Standard claims:
 * <ul>
 * <li>sub (subject): User identifier</li>
 * <li>iat (issued at): Token creation time</li>
 * <li>exp (expiration): Token expiration time</li>
 * <li>iss (issuer): Who created the token</li>
 * <li>aud (audience): Who the token is intended for</li>
 * </ul>
 * Custom claims: roles, permissions, email (avoid sensitive data like passwords
 * or SSN)
 * </p>
 * 
 * <p>
 * <b>Q:</b> How do you handle token expiration on the client side?
 * </p>
 * <p>
 * <b>A:</b> Strategies:
 * <ul>
 * <li>Decode JWT to check exp claim before making requests</li>
 * <li>Implement automatic token refresh before expiration</li>
 * <li>Handle 401 responses and refresh token</li>
 * <li>Use interceptors to add fresh tokens to requests</li>
 * </ul>
 * </p>
 * 
 * @author Shivam Srivastav
 */
@Service
public class JwtTokenService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate access token for authenticated user.
     * 
     * @param user Authenticated user
     * @return JWT access token
     */
    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        claims.put("roles", user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList()));

        return Jwts.builder()
                .claims(claims)
                .subject(user.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Generate refresh token for token renewal.
     * 
     * @param user Authenticated user
     * @return JWT refresh token
     */
    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .subject(user.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Extract username from JWT token.
     * 
     * @param token JWT token
     * @return Username
     */
    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    /**
     * Extract all claims from JWT token.
     * 
     * @param token JWT token
     * @return Claims
     */
    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Validate JWT token.
     * 
     * @param token JWT token
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("JWT token is expired", e);
        } catch (UnsupportedJwtException e) {
            throw new RuntimeException("JWT token is unsupported", e);
        } catch (MalformedJwtException e) {
            throw new RuntimeException("Invalid JWT token", e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("JWT claims string is empty", e);
        }
    }

    /**
     * Check if token is expired.
     * 
     * @param token JWT token
     * @return true if expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }
}
