package com.authflow.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Demo Controller showcasing different authorization mechanisms.
 * 
 * <h2>Interview Q&A:</h2>
 * 
 * <p>
 * <b>Q:</b> What is @PreAuthorize and how does it work?
 * </p>
 * <p>
 * <b>A:</b> @PreAuthorize is a method-level security annotation that uses SpEL
 * (Spring Expression Language) to define access control rules. It's evaluated
 * before
 * the method executes. Examples:
 * <ul>
 * <li>@PreAuthorize("hasRole('ADMIN')")</li>
 * <li>@PreAuthorize("hasAuthority('READ_DOCUMENT')")</li>
 * <li>@PreAuthorize("authentication.name == #username")</li>
 * <li>@PreAuthorize("@customSecurity.check(#id, authentication)")</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <b>Q:</b> What's the difference between hasRole() and hasAuthority()?
 * </p>
 * <p>
 * <b>A:</b>
 * <ul>
 * <li>hasRole('ADMIN'): Checks for "ROLE_ADMIN" authority (adds ROLE_
 * prefix)</li>
 * <li>hasAuthority('ROLE_ADMIN'): Checks for exact "ROLE_ADMIN" authority</li>
 * <li>hasAuthority() is more flexible for permissions</li>
 * </ul>
 * </p>
 * 
 * @author Shivam Srivastav
 */
@RestController
@RequestMapping("/api")
public class DemoController {

    /**
     * Public endpoint - no authentication required.
     */
    @GetMapping("/public/info")
    public ResponseEntity<Map<String, String>> publicInfo() {
        return ResponseEntity.ok(Map.of(
                "message", "This is a public endpoint",
                "authentication", "Not required"));
    }

    /**
     * Admin-only endpoint - requires ADMIN role.
     */
    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> adminUsers() {
        return ResponseEntity.ok(Map.of(
                "message", "Admin endpoint - RBAC protected",
                "access", "ROLE_ADMIN required"));
    }

    /**
     * Manager or Admin endpoint - requires MANAGER or ADMIN role.
     */
    @GetMapping("/manager/reports")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Map<String, String>> managerReports() {
        return ResponseEntity.ok(Map.of(
                "message", "Manager endpoint - RBAC protected",
                "access", "ROLE_MANAGER or ROLE_ADMIN required"));
    }
}
