package com.authflow.controller;

import com.authflow.dto.ChangePasswordRequest;
import com.authflow.dto.UpdateProfileRequest;
import com.authflow.model.RefreshToken;
import com.authflow.model.User;
import com.authflow.repository.RefreshTokenRepository;
import com.authflow.repository.UserRepository;
import com.authflow.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * User Profile Controller.
 * 
 * <p>
 * Handles user profile management, password changes, and session management.
 * </p>
 * 
 * @author Shivam Srivastav
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "User profile management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class UserProfileController {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @GetMapping("/profile")
    @Operation(summary = "Get Profile", description = "Get current user profile")
    public ResponseEntity<Map<String, Object>> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("username", user.getUsername());
        profile.put("email", user.getEmail());
        profile.put("mfaEnabled", user.isMfaEnabled());
        profile.put("createdAt", user.getCreatedAt());
        profile.put("lastLoginAt", user.getLastLoginAt());
        profile.put("roles", user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList()));
        profile.put("attributes", user.getAttributes());

        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    @Operation(summary = "Update Profile", description = "Update user profile information")
    @Transactional
    public ResponseEntity<Map<String, String>> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update email if provided
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            // Check if email already exists
            if (userRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Email already in use"));
            }
            user.setEmail(request.getEmail());
        }

        // Update attributes if provided
        if (request.getAttributes() != null) {
            user.setAttributes(request.getAttributes());
        }

        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Profile updated successfully"));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change Password", description = "Change user password")
    @Transactional
    public ResponseEntity<Map<String, String>> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Current password is incorrect"));
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Invalidate all refresh tokens (logout from all devices)
        refreshTokenRepository.deleteByUser(user);

        // Send confirmation email
        emailService.sendPasswordChangedEmail(user.getEmail(), user.getUsername());

        return ResponseEntity.ok(Map.of(
                "message", "Password changed successfully. Please login again."));
    }

    @GetMapping("/sessions")
    @Operation(summary = "List Sessions", description = "List all active sessions (refresh tokens)")
    public ResponseEntity<List<Map<String, Object>>> listSessions(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<RefreshToken> tokens = refreshTokenRepository.findByUser(user);

        List<Map<String, Object>> sessions = tokens.stream()
                .filter(token -> !token.isRevoked())
                .map(token -> {
                    Map<String, Object> session = new HashMap<>();
                    session.put("id", token.getId());
                    session.put("createdAt", token.getCreatedAt());
                    session.put("expiryDate", token.getExpiryDate());
                    session.put("deviceInfo", token.getDeviceInfo());
                    session.put("ipAddress", token.getIpAddress());
                    return session;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(sessions);
    }

    @DeleteMapping("/sessions/{id}")
    @Operation(summary = "Revoke Session", description = "Revoke a specific session (refresh token)")
    @Transactional
    public ResponseEntity<Map<String, String>> revokeSession(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        RefreshToken token = refreshTokenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        // Verify token belongs to current user
        if (!token.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403)
                    .body(Map.of("message", "Unauthorized"));
        }

        refreshTokenRepository.delete(token);

        return ResponseEntity.ok(Map.of("message", "Session revoked successfully"));
    }

    @DeleteMapping("/sessions")
    @Operation(summary = "Revoke All Sessions", description = "Revoke all sessions except current")
    @Transactional
    public ResponseEntity<Map<String, String>> revokeAllSessions(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Delete all refresh tokens
        refreshTokenRepository.deleteByUser(user);

        return ResponseEntity.ok(Map.of(
                "message", "All sessions revoked successfully. Please login again."));
    }
}
