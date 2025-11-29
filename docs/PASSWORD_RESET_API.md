# Password Reset & Email API Documentation

Complete API reference for password reset and email notification features.

## New Endpoints

### 1. Forgot Password
```http
POST /api/auth/forgot-password
```

**Description:** Request a password reset email. A reset link will be sent to the email if it exists in the system.

**Request Body:**
```json
{
  "email": "user@example.com"
}
```

**Success Response (200 OK):**
```json
{
  "message": "If the email exists, a password reset link has been sent"
}
```

**Security Note:** The response is always the same whether the email exists or not, to prevent email enumeration attacks.

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com"}'
```

**Email Template:**
The user will receive an HTML email with:
- Reset link valid for 1 hour
- Security warnings
- Professional formatting

---

### 2. Validate Reset Token
```http
GET /api/auth/validate-reset-token?token={token}
```

**Description:** Check if a password reset token is valid (not expired, not used).

**Query Parameters:**
- `token`: The reset token from the email link

**Success Response (200 OK):**
```json
{
  "valid": true
}
```

**cURL Example:**
```bash
curl "http://localhost:8080/api/auth/validate-reset-token?token=550e8400-e29b-41d4-a716-446655440000"
```

---

### 3. Reset Password
```http
POST /api/auth/reset-password
```

**Description:** Reset password using the token from email.

**Request Body:**
```json
{
  "token": "550e8400-e29b-41d4-a716-446655440000",
  "newPassword": "newSecurePassword123"
}
```

**Validation Rules:**
- `token`: Required
- `newPassword`: Minimum 8 characters, required

**Success Response (200 OK):**
```json
{
  "message": "Password has been reset successfully"
}
```

**Error Responses:**
- `400 Bad Request`: Validation errors
- `401 Unauthorized`: Invalid or expired token

**Side Effects:**
- Password is updated
- Token is marked as used
- All refresh tokens are invalidated (user logged out from all devices)
- Confirmation email is sent

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{
    "token": "550e8400-e29b-41d4-a716-446655440000",
    "newPassword": "newSecurePassword123"
  }'
```

---

## Complete Password Reset Flow

### Step-by-Step Example

```bash
# Step 1: User requests password reset
curl -X POST http://localhost:8080/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com"}'

# Response:
# {
#   "message": "If the email exists, a password reset link has been sent"
# }

# Step 2: Check application logs for the reset email
# The email will contain a reset URL like:
# http://localhost:8080/reset-password?token=550e8400-e29b-41d4-a716-446655440000

# Step 3: Validate the token (optional)
curl "http://localhost:8080/api/auth/validate-reset-token?token=550e8400-e29b-41d4-a716-446655440000"

# Response:
# {
#   "valid": true
# }

# Step 4: Reset the password
curl -X POST http://localhost:8080/api/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{
    "token": "550e8400-e29b-41d4-a716-446655440000",
    "newPassword": "newSecurePassword123"
  }'

# Response:
# {
#   "message": "Password has been reset successfully"
# }

# Step 5: Login with new password
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user",
    "password": "newSecurePassword123"
  }'

# Response: JWT tokens
```

---

## Email Templates

### 1. Password Reset Email

**Subject:** Password Reset Request - AuthFlow

**Template Features:**
- Professional HTML design
- Clear call-to-action button
- Security warnings
- Expiration notice (1 hour)
- Responsive design

**Sample:**
```html
Hi username,

We received a request to reset your password for your AuthFlow account.

[Reset Password Button]

⚠️ Security Notice:
- This link will expire in 1 hour
- If you didn't request this, please ignore this email
- Never share this link with anyone
```

---

### 2. Welcome Email

**Subject:** Welcome to AuthFlow!

**Sent When:** User registers a new account

**Template Features:**
- Welcome message
- Feature highlights
- Professional branding

**Sample:**
```html
Hi username,

Welcome to AuthFlow! We're excited to have you on board.

Features:
🔐 Secure Authentication
🛡️ Two-Factor Authentication
📱 Access Anywhere
```

---

### 3. Password Changed Email

**Subject:** Password Changed - AuthFlow

**Sent When:** Password is successfully reset

**Template Features:**
- Confirmation of password change
- Security alert if unauthorized
- Security tips

**Sample:**
```html
Hi username,

Your password has been successfully changed.

⚠️ Important: If you didn't make this change, please contact support immediately.

Security Tips:
- Never share your password
- Use unique passwords
- Enable two-factor authentication
```

---

### 4. MFA Enabled Email

**Subject:** Two-Factor Authentication Enabled - AuthFlow

**Sent When:** User enables MFA

**Template Features:**
- Confirmation of MFA setup
- Next steps
- Backup code reminder

---

## Security Features

### Password Reset Security

1. **Cryptographically Random Tokens**
   - Uses UUID for unpredictable tokens
   - 128-bit randomness

2. **Short Expiration**
   - Tokens expire in 1 hour
   - Reduces attack window

3. **One-Time Use**
   - Tokens marked as used after reset
   - Cannot be reused

4. **Email Enumeration Prevention**
   - Same response for existing and non-existing emails
   - Prevents attackers from discovering valid emails

5. **Session Invalidation**
   - All refresh tokens deleted after password reset
   - User logged out from all devices

6. **Rate Limiting** (Recommended)
   - Limit password reset requests per email
   - Prevent abuse

### Email Security

1. **Logging Only (Demo)**
   - Emails are logged to console for demo
   - In production, use JavaMailSender

2. **HTML Email Templates**
   - Professional, branded emails
   - Clear security warnings
   - Mobile-responsive design

3. **No Sensitive Data**
   - Emails don't contain passwords
   - Only reset links with tokens

---

## Interview Q&A

### Q: How does password reset work securely?

**A:** Secure password reset flow:
1. User requests reset with email
2. System generates unique, random token (UUID)
3. Token stored in database with 1-hour expiration
4. Email sent with reset link containing token
5. User clicks link, enters new password
6. System validates token (exists, not expired, not used)
7. Password updated, token marked as used
8. All sessions invalidated

**Security measures:**
- Cryptographically random tokens
- Short expiration (1 hour)
- One-time use only
- Email enumeration prevention
- Session invalidation
- Confirmation emails

---

### Q: What are common password reset vulnerabilities?

**A:**
1. **Predictable tokens**: Use UUID or secure random, not sequential IDs
2. **No expiration**: Set short expiration (15-60 minutes)
3. **Reusable tokens**: Mark as used after reset
4. **Email enumeration**: Same response for all emails
5. **No rate limiting**: Implement rate limits per email
6. **Token in logs**: Be careful with logging URLs
7. **No session invalidation**: Logout from all devices after reset

---

### Q: Should email sending be synchronous or asynchronous?

**A:** **Asynchronous is better:**

**Reasons:**
- Don't block user request waiting for email
- Email sending can be slow (network, SMTP)
- Better user experience
- Can retry on failures

**Implementation:**
- Use `@Async` annotation
- Or use message queue (RabbitMQ, Kafka)
- Implement retry logic
- Log email sending status

**Example:**
```java
@Async
public void sendPasswordResetEmail(String to, String token, String username) {
    // Email sending logic
}
```

---

### Q: How do you prevent email enumeration?

**A:** **Email enumeration** is when attackers can determine if an email exists in the system.

**Prevention:**
1. **Same response for all emails**
   ```java
   // Always return success
   return "If the email exists, a reset link has been sent";
   ```

2. **Same response time**
   - Don't return faster for non-existent emails
   - Add artificial delay if needed

3. **Rate limiting**
   - Limit requests per IP
   - Limit requests per email

4. **Logging for monitoring**
   - Log attempts for security monitoring
   - Don't reveal to user

---

## Production Configuration

### Email Configuration (application.properties)

```properties
# SMTP Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Email Settings
app.email.from=noreply@authflow.com
app.email.reset-token-expiration=3600000
```

### Enable JavaMailSender

Uncomment the email sending code in `EmailService.java`:

```java
@Autowired
private JavaMailSender javaMailSender;

private void sendHtmlEmail(String to, String subject, String htmlContent) {
    try {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        helper.setFrom("noreply@authflow.com");
        
        javaMailSender.send(message);
    } catch (MessagingException e) {
        log.error("Failed to send email", e);
    }
}
```

---

## Testing

### Test Password Reset Flow

```bash
#!/bin/bash

echo "=== Testing Password Reset Flow ==="

# 1. Request password reset
echo "1. Requesting password reset..."
curl -X POST http://localhost:8080/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com"}' | jq

# 2. Check logs for reset token
echo "2. Check application logs for reset token"
echo "   Look for: Reset URL: http://localhost:8080/reset-password?token=..."

# 3. Validate token (replace with actual token from logs)
TOKEN="your-token-from-logs"
echo "3. Validating reset token..."
curl "http://localhost:8080/api/auth/validate-reset-token?token=$TOKEN" | jq

# 4. Reset password
echo "4. Resetting password..."
curl -X POST http://localhost:8080/api/auth/reset-password \
  -H "Content-Type: application/json" \
  -d "{\"token\":\"$TOKEN\",\"newPassword\":\"newPassword123\"}" | jq

# 5. Login with new password
echo "5. Logging in with new password..."
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"newPassword123"}' | jq
```

---

## Summary

**New Features Added:**
✅ Forgot password functionality
✅ Password reset with token validation
✅ Email service with HTML templates
✅ 4 email templates (reset, welcome, password changed, MFA)
✅ Security best practices implemented
✅ Complete API documentation

**API Endpoints:**
- `POST /api/auth/forgot-password` - Request reset
- `GET /api/auth/validate-reset-token` - Validate token
- `POST /api/auth/reset-password` - Reset password

**Email Templates:**
- Password Reset Email
- Welcome Email
- Password Changed Email
- MFA Enabled Email

All emails are currently logged to console for demo purposes. In production, configure JavaMailSender for actual email sending.
