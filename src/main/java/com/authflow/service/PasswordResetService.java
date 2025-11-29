package com.authflow.service;

import com.authflow.model.PasswordResetToken;
import com.authflow.model.User;
import com.authflow.repository.PasswordResetTokenRepository;
import com.authflow.repository.RefreshTokenRepository;
import com.authflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Password Reset Service for forgot password functionality.
 * 
 * <h2>Interview Q&A:</h2>
 * 
 * <p>
 * <b>Q:</b> How do you implement forgot password securely?
 * </p>
 * <p>
 * <b>A:</b> Secure implementation:
 * <ol>
 * <li>Generate cryptographically random token (UUID)</li>
 * <li>Store token with expiration (1 hour)</li>
 * <li>Send reset link via email only</li>
 * <li>Don't reveal if email exists (prevent enumeration)</li>
 * <li>One-time use tokens</li>
 * <li>Invalidate all sessions after reset</li>
 * <li>Rate limit requests</li>
 * </ol>
 * </p>
 * 
 * <p>
 * <b>Q:</b> What are common password reset vulnerabilities?
 * </p>
 * <p>
 * <b>A:</b>
 * <ul>
 * <li>Predictable tokens (use UUID or secure random)</li>
 * <li>No expiration (set short expiration)</li>
 * <li>Reusable tokens (mark as used)</li>
 * <li>Email enumeration (same response for all emails)</li>
 * <li>No rate limiting (implement rate limits)</li>
 * <li>Token in URL logs (use POST for reset)</li>
 * </ul>
 * </p>
 * 
 * @author Shivam Srivastav
 */
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private static final int EXPIRATION_HOURS = 1;

    /**
     * Initiate password reset process.
     * 
     * @param email User's email
     */
    @Transactional
    public void forgotPassword(String email) {
        // Find user by email
        User user = userRepository.findByEmail(email).orElse(null);

        // Always return success to prevent email enumeration
        if (user == null) {
            // Log for security monitoring but don't reveal to user
            return;
        }

        // Delete any existing reset tokens for this user
        tokenRepository.deleteByUser(user);

        // Generate new reset token
        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(EXPIRATION_HOURS))
                .used(false)
                .build();

        tokenRepository.save(resetToken);

        // Send reset email
        emailService.sendPasswordResetEmail(user.getEmail(), token, user.getUsername());
    }

    /**
     * Validate reset token.
     * 
     * @param token Reset token
     * @return true if valid, false otherwise
     */
    public boolean validateResetToken(String token) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token).orElse(null);

        if (resetToken == null) {
            return false;
        }

        return resetToken.isValid();
    }

    /**
     * Reset password using token.
     * 
     * @param token       Reset token
     * @param newPassword New password
     */
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid reset token"));

        if (!resetToken.isValid()) {
            throw new RuntimeException("Reset token is expired or already used");
        }

        User user = resetToken.getUser();

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Mark token as used
        resetToken.setUsed(true);
        resetToken.setUsedAt(LocalDateTime.now());
        tokenRepository.save(resetToken);

        // Invalidate all refresh tokens (logout from all devices)
        refreshTokenRepository.deleteByUser(user);

        // Send confirmation email
        emailService.sendPasswordChangedEmail(user.getEmail(), user.getUsername());
    }

    /**
     * Clean up expired tokens (should be run periodically).
     */
    @Transactional
    public void cleanupExpiredTokens() {
        tokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());
    }
}
