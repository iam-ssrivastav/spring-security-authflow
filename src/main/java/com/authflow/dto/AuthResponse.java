package com.authflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for authentication responses containing JWT tokens.
 * 
 * <h2>Interview Q&A:</h2>
 * <p>
 * <b>Q:</b> What information should be included in an auth response?
 * </p>
 * <p>
 * <b>A:</b> Typically includes:
 * <ul>
 * <li>Access token (short-lived, e.g., 15 minutes)</li>
 * <li>Refresh token (long-lived, e.g., 7 days)</li>
 * <li>Token type (usually "Bearer")</li>
 * <li>Expiration time</li>
 * <li>User information (username, roles)</li>
 * </ul>
 * </p>
 * 
 * @author Shivam Srivastav
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private String username;
    private String[] roles;

    /**
     * MFA-related fields.
     */
    private boolean mfaRequired;
    private String mfaQrCode; // For MFA setup
}
