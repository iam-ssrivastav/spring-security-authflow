#!/bin/bash

# Base URL
BASE_URL="http://localhost:8081"

# ==========================================
# Authentication Endpoints
# ==========================================

# 1. Register a new user
curl -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "email": "newuser@example.com",
    "password": "password123"
  }'

# 2. Login (Get Tokens)
# Response contains accessToken and refreshToken
curl -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "password123"
  }'

# 3. Refresh Token
curl -X POST "$BASE_URL/api/auth/refresh" \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "YOUR_REFRESH_TOKEN_HERE"
  }'

# 4. Forgot Password
curl -X POST "$BASE_URL/api/auth/forgot-password" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com"
  }'

# ==========================================
# Document Management (Requires Access Token)
# ==========================================
# Replace YOUR_ACCESS_TOKEN with the token from login response

# 5. List Documents (Paginated)
curl -X GET "$BASE_URL/api/documents?page=0&size=10&sortBy=id&sortDir=desc" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"

# 6. Create Document
curl -X POST "$BASE_URL/api/documents" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Project Proposal",
    "content": "This is the proposal content...",
    "visibility": "PRIVATE",
    "classification": "INTERNAL"
  }'

# 7. Get Document by ID
curl -X GET "$BASE_URL/api/documents/1" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"

# 8. Update Document
curl -X PUT "$BASE_URL/api/documents/1" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Updated Proposal",
    "content": "Updated content..."
  }'

# 9. Share Document
curl -X POST "$BASE_URL/api/documents/1/share" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "manager",
    "permission": "READ"
  }'

# 10. Delete Document
curl -X DELETE "$BASE_URL/api/documents/1" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"

# ==========================================
# Admin Operations (Requires ROLE_ADMIN)
# ==========================================

# 11. List Users (Paginated)
curl -X GET "$BASE_URL/api/admin/users?page=0&size=10&sortBy=username&sortDir=asc" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"

# 12. Lock User Account
curl -X POST "$BASE_URL/api/admin/users/1/lock" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"

# 13. Unlock User Account
curl -X POST "$BASE_URL/api/admin/users/1/unlock" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"

# 14. Add Role to User
curl -X POST "$BASE_URL/api/admin/users/1/roles" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "roleName": "ROLE_MANAGER"
  }'

# 15. View Audit Logs
curl -X GET "$BASE_URL/api/admin/audit/logs" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"

# ==========================================
# Public & Demo Endpoints
# ==========================================

# 16. Public Info
curl -X GET "$BASE_URL/api/public/info"

# 17. Manager Reports (Requires ROLE_MANAGER or ROLE_ADMIN)
curl -X GET "$BASE_URL/api/manager/reports" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
