package com.authflow.controller;

import com.authflow.dto.AuthResponse;
import com.authflow.dto.ForgotPasswordRequest;
import com.authflow.dto.LoginRequest;
import com.authflow.dto.RefreshTokenRequest;
import com.authflow.dto.RegisterRequest;
import com.authflow.dto.ResetPasswordRequest;
import com.authflow.service.AuthenticationService;
import com.authflow.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Authentication Controller handling login, registration, and token refresh.
 * 
 * <h2>Interview Q&A:</h2>
 * 
 * <p>
 * <b>Q:</b> What is @RestController vs @Controller?
 * </p>
 * <p>
 * <b>A:</b>
 * <ul>
 * <li>@RestController = @Controller + @ResponseBody</li>
 * <li>@RestController automatically serializes return values to JSON/XML</li>
 * <li>@Controller is used for traditional MVC (returns view names)</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <b>Q:</b> Why use @Valid annotation?
 * </p>
 * <p>
 * <b>A:</b> @Valid triggers validation of the request body based on annotations
 * like @NotBlank, @Email, @Size in the DTO. If validation fails, Spring returns
 * 400 Bad Request with validation errors.
 * </p>
 * 
 * <p>
 * <b>Q:</b> What HTTP status codes should authentication endpoints return?
 * </p>
 * <p>
 * <b>A:</b>
 * <ul>
 * <li>200 OK: Successful login/refresh</li>
 * <li>201 Created: Successful registration</li>
 * <li>400 Bad Request: Invalid input/validation errors</li>
 * <li>401 Unauthorized: Invalid credentials</li>
 * <li>403 Forbidden: Account locked/disabled</li>
 * <li>409 Conflict: Username/email already exists</li>
 * </ul>
 * </p>
 * 
 * @author Shivam Srivastav
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final PasswordResetService passwordResetService;

    /**
     * Register a new user.
     * 
     * @param request Registration request
     * @return Authentication response with tokens
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authenticationService.register(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Login with username and password.
     * 
     * @param request Login request
     * @return Authentication response with tokens
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authenticationService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Refresh access token using refresh token.
     * 
     * @param request Refresh token request
     * @return New authentication response
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authenticationService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    /**
     * Forgot password - send reset email.
     * 
     * @param request Forgot password request with email
     * @return Success message
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.forgotPassword(request.getEmail());

        // Always return success to prevent email enumeration
        return ResponseEntity.ok(Map.of(
                "message", "If the email exists, a password reset link has been sent"));
    }

    /**
     * Validate reset token.
     * 
     * @param token Reset token
     * @return Validation result
     */
    @GetMapping("/validate-reset-token")
    public ResponseEntity<Map<String, Boolean>> validateResetToken(@RequestParam String token) {
        boolean isValid = passwordResetService.validateResetToken(token);
        return ResponseEntity.ok(Map.of("valid", isValid));
    }

    /**
     * Reset password using token.
     * 
     * @param request Reset password request
     * @return Success message
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(Map.of(
                "message", "Password has been reset successfully"));
    }
}
