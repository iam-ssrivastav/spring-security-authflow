package com.authflow.controller;

import com.authflow.dto.AdminUpdateUserRequest;
import com.authflow.model.Role;
import com.authflow.model.User;
import com.authflow.repository.RefreshTokenRepository;
import com.authflow.repository.RoleRepository;
import com.authflow.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Admin User Management Controller.
 * 
 * <p>
 * Provides administrative operations for user management.
 * </p>
 * 
 * @author Shivam Srivastav
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin - User Management", description = "Admin APIs for managing users")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @GetMapping("/{id}")
    @Operation(summary = "Get User Details", description = "Get detailed information about a user")
    public ResponseEntity<Map<String, Object>> getUserDetails(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(toDetailedMap(user));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update User", description = "Update user information (admin only)")
    @Transactional
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody AdminUpdateUserRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update email if provided
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Email already in use"));
            }
            user.setEmail(request.getEmail());
        }

        // Update account status
        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }

        if (request.getAccountNonLocked() != null) {
            user.setAccountNonLocked(request.getAccountNonLocked());
        }

        // Update roles if provided
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            Set<Role> newRoles = new HashSet<>();
            for (String roleName : request.getRoles()) {
                try {
                    Role.RoleName roleEnum = Role.RoleName.valueOf(roleName);
                    Role role = roleRepository.findByName(roleEnum)
                            .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
                    newRoles.add(role);
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("message", "Invalid role: " + roleName));
                }
            }
            user.setRoles(newRoles);
        }

        user = userRepository.save(user);

        return ResponseEntity.ok(toDetailedMap(user));
    }

    @PostMapping("/{id}/lock")
    @Operation(summary = "Lock/Unlock Account", description = "Lock or unlock user account")
    @Transactional
    public ResponseEntity<Map<String, String>> toggleLock(
            @PathVariable Long id,
            @RequestParam boolean lock) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setAccountNonLocked(!lock);
        userRepository.save(user);

        // If locking, revoke all sessions
        if (lock) {
            refreshTokenRepository.deleteByUser(user);
        }

        return ResponseEntity.ok(Map.of(
                "message", lock ? "Account locked successfully" : "Account unlocked successfully",
                "accountNonLocked", String.valueOf(!lock)));
    }

    @PostMapping("/{id}/enable")
    @Operation(summary = "Enable/Disable Account", description = "Enable or disable user account")
    @Transactional
    public ResponseEntity<Map<String, String>> toggleEnable(
            @PathVariable Long id,
            @RequestParam boolean enable) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setEnabled(enable);
        userRepository.save(user);

        // If disabling, revoke all sessions
        if (!enable) {
            refreshTokenRepository.deleteByUser(user);
        }

        return ResponseEntity.ok(Map.of(
                "message", enable ? "Account enabled successfully" : "Account disabled successfully",
                "enabled", String.valueOf(enable)));
    }

    @GetMapping("/{id}/sessions")
    @Operation(summary = "Get User Sessions", description = "Get all active sessions for a user")
    public ResponseEntity<Map<String, Object>> getUserSessions(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        long sessionCount = refreshTokenRepository.findByUser(user).stream()
                .filter(token -> !token.isRevoked())
                .count();

        return ResponseEntity.ok(Map.of(
                "userId", user.getId(),
                "username", user.getUsername(),
                "activeSessions", sessionCount));
    }

    @DeleteMapping("/{id}/sessions")
    @Operation(summary = "Revoke All User Sessions", description = "Revoke all sessions for a user (force logout)")
    @Transactional
    public ResponseEntity<Map<String, String>> revokeAllUserSessions(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        refreshTokenRepository.deleteByUser(user);

        return ResponseEntity.ok(Map.of(
                "message", "All sessions revoked for user: " + user.getUsername()));
    }

    /**
     * Convert User to detailed map for admin view.
     */
    private Map<String, Object> toDetailedMap(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("username", user.getUsername());
        map.put("email", user.getEmail());
        map.put("enabled", user.isEnabled());
        map.put("accountNonLocked", user.isAccountNonLocked());
        map.put("accountNonExpired", user.isAccountNonExpired());
        map.put("credentialsNonExpired", user.isCredentialsNonExpired());
        map.put("mfaEnabled", user.isMfaEnabled());
        map.put("createdAt", user.getCreatedAt());
        map.put("lastLoginAt", user.getLastLoginAt());
        map.put("roles", user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList()));
        map.put("oauthProvider", user.getOauthProvider());
        return map;
    }
}
