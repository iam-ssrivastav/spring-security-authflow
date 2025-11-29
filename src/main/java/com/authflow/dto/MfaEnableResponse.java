package com.authflow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response for MFA enable request.
 * 
 * @author Shivam Srivastav
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MfaEnableResponse {
    private String secret;
    private String qrCodeUrl;
    private String message;
}
