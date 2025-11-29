package com.authflow.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * User entity representing a user in the authentication system.
 * 
 * <p>
 * This entity demonstrates several important concepts:
 * </p>
 * <ul>
 * <li>User credential storage</li>
 * <li>Role-based access control (RBAC)</li>
 * <li>MFA support</li>
 * <li>Account status management</li>
 * </ul>
 * 
 * <h2>Interview Q&A:</h2>
 * <p>
 * <b>Q:</b> Should passwords be stored in plain text?
 * </p>
 * <p>
 * <b>A:</b> Never! Passwords should always be hashed using strong algorithms
 * like
 * BCrypt, Argon2, or PBKDF2. These are one-way hashing functions with salt to
 * prevent
 * rainbow table attacks.
 * </p>
 * 
 * <p>
 * <b>Q:</b> What is the difference between authentication and authorization?
 * </p>
 * <p>
 * <b>A:</b> Authentication verifies WHO you are (identity), while authorization
 * determines WHAT you can do (permissions/access rights).
 * </p>
 * 
 * @author Shivam Srivastav
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    /**
     * Password stored as a hash (never plain text).
     * Uses BCrypt by default, but can be configured for Argon2 or PBKDF2.
     */
    @Column(nullable = false)
    private String password;

    /**
     * Roles assigned to the user for RBAC.
     * Many-to-Many relationship with Role entity.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    /**
     * Account status flags for security control.
     */
    @Builder.Default
    private boolean enabled = true;

    @Builder.Default
    private boolean accountNonExpired = true;

    @Builder.Default
    private boolean accountNonLocked = true;

    @Builder.Default
    private boolean credentialsNonExpired = true;

    /**
     * Multi-Factor Authentication (MFA) fields.
     */
    @Builder.Default
    private boolean mfaEnabled = false;

    private String mfaSecret; // TOTP secret for Google Authenticator

    /**
     * OAuth 2.0 related fields.
     */
    private String oauthProvider; // google, github, etc.
    private String oauthId;

    /**
     * Audit fields.
     */
    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime lastLoginAt;

    /**
     * Attributes for Attribute-Based Access Control (ABAC).
     * Stored as JSON string for flexibility.
     */
    @Column(columnDefinition = "TEXT")
    private String attributes; // JSON: {"department": "IT", "level": "senior"}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
