package com.authflow.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * RefreshToken entity for JWT refresh token mechanism.
 * 
 * <h2>Interview Q&A:</h2>
 * <p>
 * <b>Q:</b> Why do we need refresh tokens?
 * </p>
 * <p>
 * <b>A:</b> Access tokens have short expiration times (e.g., 15 minutes) for
 * security.
 * Refresh tokens allow users to get new access tokens without
 * re-authenticating. Benefits:
 * <ul>
 * <li>Better security: Short-lived access tokens reduce attack window</li>
 * <li>Better UX: Users don't need to login frequently</li>
 * <li>Revocability: Can revoke refresh tokens from database</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <b>Q:</b> Can JWT tokens be revoked?
 * </p>
 * <p>
 * <b>A:</b> Pure JWTs cannot be revoked (they're stateless). Solutions:
 * <ul>
 * <li>Use short expiration times</li>
 * <li>Maintain a token blacklist in Redis</li>
 * <li>Store refresh tokens in database for revocation</li>
 * <li>Include a version number in JWT and increment on logout</li>
 * </ul>
 * </p>
 * 
 * @author Shivam Srivastav
 */
@Entity
@Table(name = "refresh_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    /**
     * Revoked flag for token invalidation.
     */
    @Builder.Default
    private boolean revoked = false;

    /**
     * Device information for security tracking.
     */
    private String deviceInfo;
    private String ipAddress;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }
}
