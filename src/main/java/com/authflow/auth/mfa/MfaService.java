package com.authflow.auth.mfa;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import org.springframework.stereotype.Service;

/**
 * Multi-Factor Authentication (MFA) Service using TOTP (Time-based One-Time
 * Password).
 * 
 * <h2>TOTP Algorithm:</h2>
 * 
 * <pre>
 * TOTP = HOTP(K, T)
 * where:
 *   K = shared secret key
 *   T = (Current Unix time - T0) / X
 *   T0 = initial time (usually 0)
 *   X = time step (usually 30 seconds)
 * 
 * HOTP = Truncate(HMAC-SHA1(K, C))
 * where:
 *   C = counter value (T for TOTP)
 * </pre>
 * 
 * <h2>Interview Q&A:</h2>
 * 
 * <p>
 * <b>Q:</b> How does Google Authenticator work?
 * </p>
 * <p>
 * <b>A:</b> Google Authenticator implements TOTP (RFC 6238):
 * <ol>
 * <li>Server generates a secret key</li>
 * <li>Secret is shared with user via QR code</li>
 * <li>Both server and app generate codes using same algorithm and secret</li>
 * <li>Codes change every 30 seconds based on current time</li>
 * <li>Server validates user's code matches expected code</li>
 * </ol>
 * </p>
 * 
 * <p>
 * <b>Q:</b> What are the different types of MFA?
 * </p>
 * <p>
 * <b>A:</b>
 * <ul>
 * <li>Something you know: Password, PIN</li>
 * <li>Something you have: Phone (SMS/TOTP), Hardware token, Smart card</li>
 * <li>Something you are: Biometrics (fingerprint, face, iris)</li>
 * <li>Somewhere you are: Geolocation, IP address</li>
 * </ul>
 * Best practice: Combine factors from different categories.
 * </p>
 * 
 * <p>
 * <b>Q:</b> Why is TOTP better than SMS for MFA?
 * </p>
 * <p>
 * <b>A:</b>
 * <ul>
 * <li>SMS can be intercepted (SIM swapping, SS7 attacks)</li>
 * <li>TOTP works offline (no network required)</li>
 * <li>TOTP is faster and more reliable</li>
 * <li>TOTP secret never transmitted after initial setup</li>
 * </ul>
 * However, SMS is more user-friendly for non-technical users.
 * </p>
 * 
 * <p>
 * <b>Q:</b> How do you handle time synchronization issues with TOTP?
 * </p>
 * <p>
 * <b>A:</b> Solutions:
 * <ul>
 * <li>Accept codes from previous/next time windows (±1 window = ±30
 * seconds)</li>
 * <li>Allow time drift compensation</li>
 * <li>Provide backup codes for recovery</li>
 * <li>Use NTP on servers for accurate time</li>
 * </ul>
 * </p>
 * 
 * @author Shivam Srivastav
 */
@Service
public class MfaService {

    private final GoogleAuthenticator googleAuthenticator;

    public MfaService() {
        this.googleAuthenticator = new GoogleAuthenticator();
    }

    /**
     * Generate MFA secret for a user.
     * 
     * @return Secret key
     */
    public String generateSecret() {
        GoogleAuthenticatorKey key = googleAuthenticator.createCredentials();
        return key.getKey();
    }

    /**
     * Generate QR code URL for Google Authenticator.
     * 
     * @param username User's username
     * @param secret   MFA secret
     * @return QR code URL
     */
    public String generateQrCodeUrl(String username, String secret) {
        return GoogleAuthenticatorQRGenerator.getOtpAuthURL(
                "AuthFlow",
                username,
                new GoogleAuthenticatorKey.Builder(secret).build());
    }

    /**
     * Verify TOTP code.
     * 
     * @param secret User's MFA secret
     * @param code   Code to verify
     * @return true if valid, false otherwise
     */
    public boolean verifyCode(String secret, int code) {
        return googleAuthenticator.authorize(secret, code);
    }

    /**
     * Verify TOTP code with time window tolerance.
     * 
     * @param secret     User's MFA secret
     * @param code       Code to verify
     * @param windowSize Number of time windows to check (±windowSize * 30 seconds)
     * @return true if valid, false otherwise
     */
    public boolean verifyCodeWithWindow(String secret, int code, int windowSize) {
        return googleAuthenticator.authorize(secret, code, windowSize);
    }
}
