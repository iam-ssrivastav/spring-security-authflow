package com.authflow.controller;

import com.authflow.auth.mfa.MfaService;
import com.authflow.dto.MfaEnableResponse;
import com.authflow.dto.MfaVerifyRequest;
import com.authflow.model.User;
import com.authflow.repository.UserRepository;
import com.authflow.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * MFA (Multi-Factor Authentication) Controller.
 * 
 * <h2>Interview Q&A:</h2>
 * 
 * <p>
 * <b>Q:</b> What is MFA and why is it important?
 * </p>
 * <p>
 * <b>A:</b> MFA (Multi-Factor Authentication) requires multiple forms of
 * verification:
 * <ul>
 * <li><b>Something you know:</b> Password</li>
 * <li><b>Something you have:</b> Phone/authenticator app</li>
 * <li><b>Something you are:</b> Biometrics</li>
 * </ul>
 * Benefits:
 * <ul>
 * <li>Prevents unauthorized access even if password is compromised</li>
 * <li>Reduces phishing attacks</li>
 * <li>Compliance requirement (PCI-DSS, HIPAA)</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <b>Q:</b> How does TOTP (Time-based OTP) work?
 * </p>
 * <p>
 * <b>A:</b> TOTP generates codes based on:
 * <ul>
 * <li><b>Shared secret:</b> Known to both server and client</li>
 * <li><b>Current time:</b> 30-second window</li>
 * <li><b>HMAC algorithm:</b> SHA-1 hash</li>
 * </ul>
 * Formula: TOTP = HMAC(Secret, Time / 30)
 * </p>
 * 
 * @author Shivam Srivastav
 */
@RestController
@RequestMapping("/api/mfa")
@RequiredArgsConstructor
@Tag(name = "MFA", description = "Multi-Factor Authentication APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class MfaController {

    private final MfaService mfaService;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @PostMapping("/enable")
    @Operation(summary = "Enable MFA", description = "Generate MFA secret and QR code for user")
    public ResponseEntity<MfaEnableResponse> enableMfa(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate secret
        String secret = mfaService.generateSecret();

        // Generate QR code URL
        String qrCodeUrl = mfaService.generateQrCodeUrl(user.getUsername(), secret);

        // Save secret (will be activated after verification)
        user.setMfaSecret(secret);
        userRepository.save(user);

        return ResponseEntity.ok(new MfaEnableResponse(
                secret,
                qrCodeUrl,
                "Scan QR code with Google Authenticator and verify with code"));
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify MFA", description = "Verify MFA code and activate MFA")
    public ResponseEntity<Map<String, String>> verifyMfa(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody MfaVerifyRequest request) {

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getMfaSecret() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "MFA not initialized. Call /enable first"));
        }

        // Verify code (parse string to int)
        int code;
        try {
            code = Integer.parseInt(request.getCode());
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Invalid MFA code format"));
        }

        boolean isValid = mfaService.verifyCode(user.getMfaSecret(), code);

        if (!isValid) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Invalid MFA code"));
        }

        // Activate MFA
        user.setMfaEnabled(true);
        userRepository.save(user);

        // Send confirmation email
        emailService.sendMfaEnabledEmail(user.getEmail(), user.getUsername());

        return ResponseEntity.ok(Map.of(
                "message", "MFA enabled successfully",
                "mfaEnabled", "true"));
    }

    @PostMapping("/disable")
    @Operation(summary = "Disable MFA", description = "Disable MFA for user account")
    public ResponseEntity<Map<String, String>> disableMfa(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody MfaVerifyRequest request) {

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isMfaEnabled()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "MFA is not enabled"));
        }

        // Verify current code before disabling
        int code;
        try {
            code = Integer.parseInt(request.getCode());
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Invalid MFA code format"));
        }

        boolean isValid = mfaService.verifyCode(user.getMfaSecret(), code);

        if (!isValid) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Invalid MFA code"));
        }

        // Disable MFA
        user.setMfaEnabled(false);
        user.setMfaSecret(null);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "message", "MFA disabled successfully",
                "mfaEnabled", "false"));
    }

    @GetMapping("/status")
    @Operation(summary = "Get MFA Status", description = "Check if MFA is enabled for current user")
    public ResponseEntity<Map<String, Object>> getMfaStatus(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(Map.of(
                "mfaEnabled", user.isMfaEnabled(),
                "username", user.getUsername()));
    }
}
