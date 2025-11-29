# API Documentation

Complete API reference for the AuthFlow authentication and authorization system.

## Base URL
```
http://localhost:8080
```

## Authentication

All protected endpoints require a valid JWT token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

---

## Endpoints

### Public Endpoints

#### 1. Get Public Information
```http
GET /api/public/info
```

**Description:** Public endpoint that doesn't require authentication.

**Response:**
```json
{
  "message": "This is a public endpoint",
  "authentication": "Not required"
}
```

**cURL Example:**
```bash
curl http://localhost:8080/api/public/info
```

---

### Authentication Endpoints

#### 2. Register New User
```http
POST /api/auth/register
```

**Description:** Register a new user account.

**Request Body:**
```json
{
  "username": "newuser",
  "email": "newuser@example.com",
  "password": "password123"
}
```

**Validation Rules:**
- `username`: 3-50 characters, required
- `email`: Valid email format, required
- `password`: Minimum 8 characters, required

**Success Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "username": "newuser",
  "roles": ["ROLE_USER"],
  "mfaRequired": false
}
```

**Error Responses:**
- `400 Bad Request`: Validation errors
- `409 Conflict`: Username or email already exists

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "email": "newuser@example.com",
    "password": "password123"
  }'
```

---

#### 3. Login
```http
POST /api/auth/login
```

**Description:** Authenticate user and receive JWT tokens.

**Request Body:**
```json
{
  "username": "user",
  "password": "password123"
}
```

**Success Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "username": "user",
  "roles": ["ROLE_USER"],
  "mfaRequired": false
}
```

**MFA Required Response:**
```json
{
  "mfaRequired": true,
  "username": "user"
}
```

**Error Responses:**
- `400 Bad Request`: Validation errors
- `401 Unauthorized`: Invalid credentials
- `403 Forbidden`: Account locked or disabled

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user",
    "password": "password123"
  }'
```

**Save Token for Subsequent Requests:**
```bash
# Save response to variable
TOKEN=$(curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"password123"}' \
  | jq -r '.accessToken')

# Use token in requests
curl http://localhost:8080/api/user/profile \
  -H "Authorization: Bearer $TOKEN"
```

---

#### 4. Refresh Token
```http
POST /api/auth/refresh?refreshToken={token}
```

**Description:** Get a new access token using refresh token.

**Query Parameters:**
- `refreshToken`: The refresh token received during login

**Success Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "username": "user",
  "roles": ["ROLE_USER"],
  "mfaRequired": false
}
```

**Error Responses:**
- `401 Unauthorized`: Invalid or expired refresh token

**cURL Example:**
```bash
curl -X POST "http://localhost:8080/api/auth/refresh?refreshToken=550e8400-e29b-41d4-a716-446655440000"
```

---

### Protected Endpoints

#### 5. Get User Profile
```http
GET /api/user/profile
```

**Description:** Get current user's profile information.

**Authentication:** Required

**Success Response (200 OK):**
```json
{
  "username": "user",
  "authorities": [
    {"authority": "ROLE_USER"},
    {"authority": "READ_DOCUMENT"},
    {"authority": "WRITE_DOCUMENT"}
  ],
  "message": "This is a protected endpoint"
}
```

**Error Responses:**
- `401 Unauthorized`: Missing or invalid token

**cURL Example:**
```bash
curl http://localhost:8080/api/user/profile \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

### Admin Endpoints

#### 6. List All Users (Admin Only)
```http
GET /api/admin/users
```

**Description:** Get list of all users (requires ADMIN role).

**Authentication:** Required (ADMIN role)

**Success Response (200 OK):**
```json
{
  "message": "Admin endpoint - RBAC protected",
  "access": "ROLE_ADMIN required"
}
```

**Error Responses:**
- `401 Unauthorized`: Missing or invalid token
- `403 Forbidden`: Insufficient permissions (not an admin)

**cURL Example:**
```bash
# Login as admin first
ADMIN_TOKEN=$(curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password123"}' \
  | jq -r '.accessToken')

# Access admin endpoint
curl http://localhost:8080/api/admin/users \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

---

### Manager Endpoints

#### 7. Get Manager Reports
```http
GET /api/manager/reports
```

**Description:** Access manager reports (requires MANAGER or ADMIN role).

**Authentication:** Required (MANAGER or ADMIN role)

**Success Response (200 OK):**
```json
{
  "message": "Manager endpoint - RBAC protected",
  "access": "ROLE_MANAGER or ROLE_ADMIN required"
}
```

**Error Responses:**
- `401 Unauthorized`: Missing or invalid token
- `403 Forbidden`: Insufficient permissions

**cURL Example:**
```bash
# Login as manager
MANAGER_TOKEN=$(curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"manager","password":"password123"}' \
  | jq -r '.accessToken')

# Access manager endpoint
curl http://localhost:8080/api/manager/reports \
  -H "Authorization: Bearer $MANAGER_TOKEN"
```

---

### Document Endpoints

#### 8. Create Document
```http
POST /api/documents
```

**Description:** Create a new document (requires WRITE_DOCUMENT permission).

**Authentication:** Required (WRITE_DOCUMENT permission)

**Success Response (200 OK):**
```json
{
  "message": "Document created - Permission-based access control",
  "permission": "WRITE_DOCUMENT required"
}
```

**Error Responses:**
- `401 Unauthorized`: Missing or invalid token
- `403 Forbidden`: Missing WRITE_DOCUMENT permission

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/documents \
  -H "Authorization: Bearer $TOKEN"
```

---

#### 9. Delete Document
```http
DELETE /api/documents/{id}
```

**Description:** Delete a document (requires DELETE_DOCUMENT permission AND ADMIN role).

**Authentication:** Required (DELETE_DOCUMENT permission + ADMIN role)

**Path Parameters:**
- `id`: Document ID

**Success Response (200 OK):**
```json
{
  "message": "Document deleted - Combined RBAC and Permission-based",
  "requirements": "DELETE_DOCUMENT permission AND ROLE_ADMIN"
}
```

**Error Responses:**
- `401 Unauthorized`: Missing or invalid token
- `403 Forbidden`: Missing required permission or role

**cURL Example:**
```bash
curl -X DELETE http://localhost:8080/api/documents/1 \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

---

## Complete Workflow Examples

### Example 1: New User Registration and Access

```bash
# 1. Register new user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice",
    "email": "alice@example.com",
    "password": "securepass123"
  }' | jq

# Response:
# {
#   "accessToken": "eyJ...",
#   "refreshToken": "550e...",
#   "tokenType": "Bearer",
#   "expiresIn": 86400,
#   "username": "alice",
#   "roles": ["ROLE_USER"]
# }

# 2. Save token
TOKEN="eyJ..."

# 3. Access protected endpoint
curl http://localhost:8080/api/user/profile \
  -H "Authorization: Bearer $TOKEN" | jq

# 4. Try to access admin endpoint (should fail)
curl http://localhost:8080/api/admin/users \
  -H "Authorization: Bearer $TOKEN"
# Response: 403 Forbidden
```

---

### Example 2: Admin Workflow

```bash
# 1. Login as admin
RESPONSE=$(curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "password123"
  }')

# 2. Extract tokens
ACCESS_TOKEN=$(echo $RESPONSE | jq -r '.accessToken')
REFRESH_TOKEN=$(echo $RESPONSE | jq -r '.refreshToken')

# 3. Access user profile
curl http://localhost:8080/api/user/profile \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq

# 4. Access admin endpoint
curl http://localhost:8080/api/admin/users \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq

# 5. Create document
curl -X POST http://localhost:8080/api/documents \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq

# 6. Delete document
curl -X DELETE http://localhost:8080/api/documents/1 \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq

# 7. Refresh token when access token expires
NEW_RESPONSE=$(curl -X POST \
  "http://localhost:8080/api/auth/refresh?refreshToken=$REFRESH_TOKEN")

NEW_ACCESS_TOKEN=$(echo $NEW_RESPONSE | jq -r '.accessToken')
```

---

### Example 3: Testing Different Roles

```bash
#!/bin/bash

# Function to login and get token
login() {
    local username=$1
    local password=$2
    curl -s -X POST http://localhost:8080/api/auth/login \
        -H "Content-Type: application/json" \
        -d "{\"username\":\"$username\",\"password\":\"$password\"}" \
        | jq -r '.accessToken'
}

# Test as different users
echo "=== Testing as USER ==="
USER_TOKEN=$(login "user" "password123")
curl -s http://localhost:8080/api/user/profile \
    -H "Authorization: Bearer $USER_TOKEN" | jq
curl -s http://localhost:8080/api/admin/users \
    -H "Authorization: Bearer $USER_TOKEN"
echo ""

echo "=== Testing as MANAGER ==="
MANAGER_TOKEN=$(login "manager" "password123")
curl -s http://localhost:8080/api/manager/reports \
    -H "Authorization: Bearer $MANAGER_TOKEN" | jq
curl -s http://localhost:8080/api/admin/users \
    -H "Authorization: Bearer $MANAGER_TOKEN"
echo ""

echo "=== Testing as ADMIN ==="
ADMIN_TOKEN=$(login "admin" "password123")
curl -s http://localhost:8080/api/admin/users \
    -H "Authorization: Bearer $ADMIN_TOKEN" | jq
curl -s -X DELETE http://localhost:8080/api/documents/1 \
    -H "Authorization: Bearer $ADMIN_TOKEN" | jq
```

---

## Error Responses

### Standard Error Format

All error responses follow this format:

```json
{
  "timestamp": "2025-11-30T00:00:00.000+00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid credentials",
  "path": "/api/auth/login"
}
```

### Common HTTP Status Codes

| Code | Meaning | Common Causes |
|------|---------|---------------|
| 200 | OK | Request successful |
| 201 | Created | Resource created successfully |
| 400 | Bad Request | Validation errors, malformed request |
| 401 | Unauthorized | Missing or invalid authentication |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource not found |
| 409 | Conflict | Resource already exists |
| 429 | Too Many Requests | Rate limit exceeded |
| 500 | Internal Server Error | Server error |

---

## Rate Limiting

Some endpoints have rate limiting to prevent abuse:

| Endpoint | Limit | Window |
|----------|-------|--------|
| `/api/auth/login` | 10 requests | 1 minute |
| `/api/auth/register` | 5 requests | 1 hour |
| `/api/auth/refresh` | 20 requests | 1 minute |

**Rate Limit Headers:**
```
X-RateLimit-Limit: 10
X-RateLimit-Remaining: 7
X-RateLimit-Reset: 1638316800
```

---

## Security Best Practices

### 1. Token Storage
- **Browser**: Use httpOnly cookies or memory (not localStorage)
- **Mobile**: Use secure storage (Keychain/KeyStore)
- **Never**: Log tokens or include in URLs

### 2. Token Transmission
- Always use HTTPS in production
- Send tokens in Authorization header
- Don't send tokens in query parameters

### 3. Token Lifecycle
- Access tokens: Short-lived (15 minutes - 1 hour)
- Refresh tokens: Long-lived (7 days - 30 days)
- Implement token rotation
- Revoke tokens on logout

### 4. Error Handling
- Don't expose sensitive information in errors
- Use generic error messages for authentication failures
- Log detailed errors server-side only

---

## Postman Collection

Import this collection into Postman for easy testing:

```json
{
  "info": {
    "name": "AuthFlow API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Register",
      "request": {
        "method": "POST",
        "header": [{"key": "Content-Type", "value": "application/json"}],
        "body": {
          "mode": "raw",
          "raw": "{\"username\":\"testuser\",\"email\":\"test@example.com\",\"password\":\"password123\"}"
        },
        "url": "{{baseUrl}}/api/auth/register"
      }
    },
    {
      "name": "Login",
      "request": {
        "method": "POST",
        "header": [{"key": "Content-Type", "value": "application/json"}],
        "body": {
          "mode": "raw",
          "raw": "{\"username\":\"user\",\"password\":\"password123\"}"
        },
        "url": "{{baseUrl}}/api/auth/login"
      }
    }
  ],
  "variable": [
    {
      "key": "baseUrl",
      "value": "http://localhost:8080"
    }
  ]
}
```

---

## Support

For issues or questions:
- Check the [README](../README.md)
- Review [INTERVIEW_GUIDE.md](INTERVIEW_GUIDE.md) for concepts
- See [AUTHENTICATION_GUIDE.md](AUTHENTICATION_GUIDE.md) for authentication details
