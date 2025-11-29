package com.authflow.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Password Reset Token entity for forgot password functionality.
 * 
 * <h2>Interview Q&A:</h2>
 * 
 * <p>
 * <b>Q:</b> How does password reset work securely?
 * </p>
 * <p>
 * <b>A:</b> Secure password reset flow:
 * <ol>
 * <li>User requests password reset with email</li>
 * <li>System generates unique, random token</li>
 * <li>Token stored in database with expiration (e.g., 1 hour)</li>
 * <li>Email sent with reset link containing token</li>
 * <li>User clicks link, enters new password</li>
 * <li>System validates token (exists, not expired, not used)</li>
 * <li>Password updated, token marked as used</li>
 * </ol>
 * </p>
 * 
 * <p>
 * <b>Q:</b> What security measures should be implemented?
 * </p>
 * <p>
 * <b>A:</b>
 * <ul>
 * <li>Token should be cryptographically random (UUID or secure random)</li>
 * <li>Short expiration time (15-60 minutes)</li>
 * <li>One-time use only</li>
 * <li>Rate limit reset requests per email</li>
 * <li>Don't reveal if email exists in system</li>
 * <li>Invalidate all sessions after password change</li>
 * </ul>
 * </p>
 * 
 * @author Shivam Srivastav
 */
@Entity
@Table(name = "password_reset_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken {

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

    @Builder.Default
    private boolean used = false;

    private LocalDateTime usedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }

    public boolean isValid() {
        return !used && !isExpired();
    }
}
