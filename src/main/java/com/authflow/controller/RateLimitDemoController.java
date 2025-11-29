package com.authflow.controller;

import com.authflow.service.RateLimitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Rate Limiting Demo Controller.
 * 
 * <p>
 * Demonstrates rate limiting using TTL cache.
 * </p>
 * 
 * @author Shivam Srivastav
 */
@RestController
@RequestMapping("/api/demo")
@RequiredArgsConstructor
@Tag(name = "Demo - Rate Limiting", description = "Rate limiting demonstration APIs")
public class RateLimitDemoController {

    private final RateLimitService rateLimitService;

    @GetMapping("/rate-limited")
    @Operation(summary = "Rate Limited Endpoint", description = "Try this endpoint multiple times to see rate limiting in action (10 requests/minute)")
    public ResponseEntity<Map<String, Object>> rateLimitedEndpoint(
            @AuthenticationPrincipal UserDetails userDetails) {

        String identifier = userDetails != null ? userDetails.getUsername() : "anonymous";

        if (!rateLimitService.isAllowed(identifier)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of(
                            "message", "Rate limit exceeded",
                            "limit", 10,
                            "window", "1 minute",
                            "remaining", rateLimitService.getRemainingRequests(identifier),
                            "advice", "Please wait before making more requests"));
        }

        int remaining = rateLimitService.getRemainingRequests(identifier);
        int current = rateLimitService.getCurrentCount(identifier);

        return ResponseEntity.ok(Map.of(
                "message", "Request successful",
                "requestCount", current,
                "remaining", remaining,
                "limit", 10,
                "window", "1 minute"));
    }

    @GetMapping("/rate-limit-status")
    @Operation(summary = "Rate Limit Status", description = "Check current rate limit status")
    public ResponseEntity<Map<String, Object>> rateLimitStatus(
            @AuthenticationPrincipal UserDetails userDetails) {

        String identifier = userDetails != null ? userDetails.getUsername() : "anonymous";

        int current = rateLimitService.getCurrentCount(identifier);
        int remaining = rateLimitService.getRemainingRequests(identifier);

        return ResponseEntity.ok(Map.of(
                "identifier", identifier,
                "currentCount", current,
                "remaining", remaining,
                "limit", 10,
                "window", "1 minute",
                "status", remaining > 0 ? "OK" : "RATE_LIMITED"));
    }
}
