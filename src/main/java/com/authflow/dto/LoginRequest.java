package com.authflow.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user login requests.
 * 
 * <h2>Interview Q&A:</h2>
 * <p>
 * <b>Q:</b> Why use DTOs instead of entities directly?
 * </p>
 * <p>
 * <b>A:</b> DTOs (Data Transfer Objects) provide:
 * <ul>
 * <li>Separation of concerns (API layer vs persistence layer)</li>
 * <li>Security (don't expose sensitive entity fields)</li>
 * <li>Flexibility (API can change without affecting database)</li>
 * <li>Validation (can have different validation rules)</li>
 * <li>Performance (can reduce data transfer)</li>
 * </ul>
 * </p>
 * 
 * @author Shivam Srivastav
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    /**
     * Optional MFA code for two-factor authentication.
     */
    private String mfaCode;
}
