package com.authflow.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Email Service for sending notifications.
 * 
 * <h2>Interview Q&A:</h2>
 * 
 * <p>
 * <b>Q:</b> How do you send emails in a Spring Boot application?
 * </p>
 * <p>
 * <b>A:</b> Use JavaMailSender with SMTP configuration:
 * <ul>
 * <li>Add spring-boot-starter-mail dependency</li>
 * <li>Configure SMTP properties (host, port, username, password)</li>
 * <li>Use JavaMailSender to send emails</li>
 * <li>Use Thymeleaf for HTML email templates</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <b>Q:</b> Should email sending be synchronous or asynchronous?
 * </p>
 * <p>
 * <b>A:</b> Asynchronous is better:
 * <ul>
 * <li>Don't block user request waiting for email</li>
 * <li>Use @Async or message queue (RabbitMQ, Kafka)</li>
 * <li>Implement retry logic for failures</li>
 * <li>Log email sending status</li>
 * </ul>
 * </p>
 * 
 * @author Shivam Srivastav
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    // Note: JavaMailSender would be autowired in production
    // For demo, we'll log emails instead of actually sending

    /**
     * Send password reset email.
     * 
     * @param to         Recipient email
     * @param resetToken Password reset token
     * @param username   User's username
     */
    public void sendPasswordResetEmail(String to, String resetToken, String username) {
        String subject = "Password Reset Request - AuthFlow";
        String resetUrl = "http://localhost:8080/reset-password?token=" + resetToken;

        String htmlContent = buildPasswordResetEmail(username, resetUrl);

        // In production, use JavaMailSender
        // For demo, we log the email
        log.info("=== PASSWORD RESET EMAIL ===");
        log.info("To: {}", to);
        log.info("Subject: {}", subject);
        log.info("Reset URL: {}", resetUrl);
        log.info("Content:\n{}", htmlContent);
        log.info("===========================");

        // Production code:
        // sendHtmlEmail(to, subject, htmlContent);
    }

    /**
     * Send welcome email to new users.
     * 
     * @param to       Recipient email
     * @param username User's username
     */
    public void sendWelcomeEmail(String to, String username) {
        String subject = "Welcome to AuthFlow!";
        String htmlContent = buildWelcomeEmail(username);

        log.info("=== WELCOME EMAIL ===");
        log.info("To: {}", to);
        log.info("Subject: {}", subject);
        log.info("Content:\n{}", htmlContent);
        log.info("====================");

        // Production code:
        // sendHtmlEmail(to, subject, htmlContent);
    }

    /**
     * Send password changed notification.
     * 
     * @param to       Recipient email
     * @param username User's username
     */
    public void sendPasswordChangedEmail(String to, String username) {
        String subject = "Password Changed - AuthFlow";
        String htmlContent = buildPasswordChangedEmail(username);

        log.info("=== PASSWORD CHANGED EMAIL ===");
        log.info("To: {}", to);
        log.info("Subject: {}", subject);
        log.info("Content:\n{}", htmlContent);
        log.info("==============================");

        // Production code:
        // sendHtmlEmail(to, subject, htmlContent);
    }

    /**
     * Send MFA setup email.
     * 
     * @param to       Recipient email
     * @param username User's username
     */
    public void sendMfaEnabledEmail(String to, String username) {
        String subject = "Two-Factor Authentication Enabled - AuthFlow";
        String htmlContent = buildMfaEnabledEmail(username);

        log.info("=== MFA ENABLED EMAIL ===");
        log.info("To: {}", to);
        log.info("Subject: {}", subject);
        log.info("Content:\n{}", htmlContent);
        log.info("========================");
    }

    /**
     * Build password reset email HTML.
     */
    private String buildPasswordResetEmail(String username, String resetUrl) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: #4CAF50; color: white; padding: 20px; text-align: center; }
                        .content { padding: 20px; background: #f9f9f9; }
                        .button { display: inline-block; padding: 12px 24px; background: #4CAF50;
                                  color: white; text-decoration: none; border-radius: 4px; margin: 20px 0; }
                        .footer { text-align: center; padding: 20px; color: #777; font-size: 12px; }
                        .warning { background: #fff3cd; border-left: 4px solid #ffc107; padding: 12px; margin: 20px 0; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🔐 Password Reset Request</h1>
                        </div>
                        <div class="content">
                            <p>Hi <strong>%s</strong>,</p>

                            <p>We received a request to reset your password for your AuthFlow account.</p>

                            <p>Click the button below to reset your password:</p>

                            <a href="%s" class="button">Reset Password</a>

                            <p>Or copy and paste this link into your browser:</p>
                            <p style="word-break: break-all; color: #4CAF50;">%s</p>

                            <div class="warning">
                                <strong>⚠️ Security Notice:</strong>
                                <ul>
                                    <li>This link will expire in 1 hour</li>
                                    <li>If you didn't request this, please ignore this email</li>
                                    <li>Never share this link with anyone</li>
                                </ul>
                            </div>

                            <p>If you have any questions, please contact our support team.</p>

                            <p>Best regards,<br>The AuthFlow Team</p>
                        </div>
                        <div class="footer">
                            <p>This is an automated email. Please do not reply.</p>
                            <p>&copy; 2025 AuthFlow. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(username, resetUrl, resetUrl);
    }

    /**
     * Build welcome email HTML.
     */
    private String buildWelcomeEmail(String username) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: #2196F3; color: white; padding: 20px; text-align: center; }
                        .content { padding: 20px; background: #f9f9f9; }
                        .feature { background: white; padding: 15px; margin: 10px 0; border-radius: 4px; }
                        .footer { text-align: center; padding: 20px; color: #777; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🎉 Welcome to AuthFlow!</h1>
                        </div>
                        <div class="content">
                            <p>Hi <strong>%s</strong>,</p>

                            <p>Welcome to AuthFlow! We're excited to have you on board.</p>

                            <p>Your account has been successfully created. Here's what you can do:</p>

                            <div class="feature">
                                <h3>🔐 Secure Authentication</h3>
                                <p>Your account is protected with industry-standard security measures.</p>
                            </div>

                            <div class="feature">
                                <h3>🛡️ Two-Factor Authentication</h3>
                                <p>Enable MFA for an extra layer of security.</p>
                            </div>

                            <div class="feature">
                                <h3>📱 Access Anywhere</h3>
                                <p>Use your account across all devices securely.</p>
                            </div>

                            <p>If you have any questions, feel free to reach out to our support team.</p>

                            <p>Best regards,<br>The AuthFlow Team</p>
                        </div>
                        <div class="footer">
                            <p>&copy; 2025 AuthFlow. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(username);
    }

    /**
     * Build password changed email HTML.
     */
    private String buildPasswordChangedEmail(String username) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: #FF9800; color: white; padding: 20px; text-align: center; }
                        .content { padding: 20px; background: #f9f9f9; }
                        .alert { background: #f44336; color: white; padding: 15px; border-radius: 4px; margin: 20px 0; }
                        .footer { text-align: center; padding: 20px; color: #777; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🔒 Password Changed</h1>
                        </div>
                        <div class="content">
                            <p>Hi <strong>%s</strong>,</p>

                            <p>Your password has been successfully changed.</p>

                            <div class="alert">
                                <strong>⚠️ Important:</strong> If you didn't make this change, please contact our support team immediately.
                            </div>

                            <p><strong>Security Tips:</strong></p>
                            <ul>
                                <li>Never share your password with anyone</li>
                                <li>Use a unique password for each account</li>
                                <li>Enable two-factor authentication for extra security</li>
                                <li>Change your password regularly</li>
                            </ul>

                            <p>Best regards,<br>The AuthFlow Team</p>
                        </div>
                        <div class="footer">
                            <p>&copy; 2025 AuthFlow. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """
                .formatted(username);
    }

    /**
     * Build MFA enabled email HTML.
     */
    private String buildMfaEnabledEmail(String username) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: #9C27B0; color: white; padding: 20px; text-align: center; }
                        .content { padding: 20px; background: #f9f9f9; }
                        .success { background: #4CAF50; color: white; padding: 15px; border-radius: 4px; margin: 20px 0; }
                        .footer { text-align: center; padding: 20px; color: #777; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🛡️ Two-Factor Authentication Enabled</h1>
                        </div>
                        <div class="content">
                            <p>Hi <strong>%s</strong>,</p>

                            <div class="success">
                                <strong>✅ Success!</strong> Two-factor authentication has been enabled on your account.
                            </div>

                            <p>Your account is now more secure. You'll need to enter a verification code from your authenticator app when logging in.</p>

                            <p><strong>What's Next:</strong></p>
                            <ul>
                                <li>Save your backup codes in a secure location</li>
                                <li>You'll need your authenticator app for future logins</li>
                                <li>If you lose access, use your backup codes</li>
                            </ul>

                            <p>If you didn't enable this, please contact support immediately.</p>

                            <p>Best regards,<br>The AuthFlow Team</p>
                        </div>
                        <div class="footer">
                            <p>&copy; 2025 AuthFlow. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """
                .formatted(username);
    }

    /**
     * Production method to send HTML email using JavaMailSender.
     * Uncomment when JavaMailSender is configured.
     */
    /*
     * private void sendHtmlEmail(String to, String subject, String htmlContent) {
     * try {
     * MimeMessage message = javaMailSender.createMimeMessage();
     * MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
     * 
     * helper.setTo(to);
     * helper.setSubject(subject);
     * helper.setText(htmlContent, true);
     * helper.setFrom("noreply@authflow.com");
     * 
     * javaMailSender.send(message);
     * log.info("Email sent successfully to: {}", to);
     * } catch (MessagingException e) {
     * log.error("Failed to send email to: {}", to, e);
     * throw new RuntimeException("Failed to send email", e);
     * }
     * }
     */
}
