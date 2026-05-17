# MedConnect User Service - API Endpoints

## Base URL
```
http://localhost:8081/api
```

---

## Authentication Endpoints (`/api/auth`)

### User Registration & Verification
- **POST** `/auth/signup` or `/auth/register`
  - Register a new user account
  - Body: `{ email, password, nom, prenom, telephone, role }`

- **POST** `/auth/verify-email`
  - Verify email after registration
  - Body: `{ email, otp }`

- **POST** `/auth/resend-otp`
  - Resend OTP code to email
  - Body: `{ email }`

### Login & Session Management
- **POST** `/auth/signin` or `/auth/login`
  - User login
  - Body: `{ email, password }`

- **POST** `/auth/verify-login`
  - Verify login with MFA/OTP
  - Body: `{ email, otp, sessionToken }`

- **POST** `/auth/refresh`
  - Refresh JWT token
  - Body: `{ refreshToken }`

- **POST** `/auth/logout`
  - Logout user (revoke token)

- **GET** `/auth/sessions`
  - List all active sessions for logged-in user
  - Returns: `List<AuthSession>`

- **DELETE** `/auth/sessions/{sessionId}`
  - Terminate a specific session
  - Path: `sessionId` — Session ID to revoke

### Password Management
- **POST** `/auth/forgot-password`
  - Request password reset
  - Body: `{ email }`

- **POST** `/auth/reset-password`
  - Reset password with token/OTP
  - Body: `{ email, otp, newPassword }`

### Multi-Factor Authentication (MFA)
- **POST** `/auth/mfa/setup`
  - Initialize MFA setup (TOTP, SMS, etc.)
  - Body: `{ mfaMethod, phoneNumber? }`
  - Returns: `{ secret, qrCode?, backupCodes[] }`

- **POST** `/auth/mfa/verify`
  - Verify MFA code during login
  - Body: `{ code, sessionToken }`

- **POST** `/auth/logout-all-devices`
  - Logout from all devices/sessions

### OAuth Integration
- **POST** `/auth/google`
  - Google OAuth login/registration
  - Body: `{ idToken }`
  - Returns: `{ accessToken, refreshToken, user }`

---

## User Management Endpoints (`/api/users`)

### Current User Profile
- **GET** `/users/me`
  - Get current logged-in user profile
  - Returns: `UserResponse`

- **PUT** `/users/me`
  - Update current user profile
  - Body: `{ nom, prenom, telephone }`
  - Returns: `UserResponse`

### User CRUD (Admin)
- **POST** `/users`
  - Create user (admin only)
  - Body: `{ email, password, nom, prenom, telephone, role }`

- **GET** `/users`
  - List all users (paginated)
  - Query: `?page=0&size=20`

- **GET** `/users/{id}`
  - Get user by ID
  - Path: `id` — User ID

- **PUT** `/users/{id}`
  - Update user (admin only)
  - Body: `{ nom, prenom, telephone, email }`

- **DELETE** `/users/{id}`
  - Delete user (admin only)

- **PUT** `/users/{id}/suspend`
  - Suspend/disable user account

### Patient Management
- **POST** `/users/patients`
  - Create patient profile
  - Body: `{ userId, bloodType, medicalHistory, allergies }`

- **GET** `/users/patients/{userId}`
  - Get patient profile
  - Path: `userId` — Patient user ID

- **PUT** `/users/patients/{userId}`
  - Update patient profile
  - Body: `{ bloodType, medicalHistory, allergies }`

### Doctor Management
- **POST** `/users/doctors`
  - Create doctor profile
  - Body: `{ userId, professionalRegistrationNumber, nationalIdNumber, specialty, cardFrontImageUrl?, cardBackImageUrl? }`
  - Note: `nationalIdNumber` is masked for non-admin responses.

- **GET** `/users/doctors/{userId}`
  - Get doctor profile for a user

- **GET** `/users/doctors/search`
  - Search verified doctors only
  - Query: `?specialization=Cardiology&hospital=Hospital Name`

- **PUT** `/users/doctors/{userId}/verification`
  - Verify or reject doctor professional profile (admin only)
  - Body: `{ status: VERIFIED|REJECTED|PENDING_VERIFICATION, note }`

### Pharmacist Management
- **POST** `/users/pharmacists`
  - Create pharmacist profile
  - Body: `{ userId, professionalRegistrationNumber, nationalIdNumber, pharmacyName, cardFrontImageUrl?, cardBackImageUrl? }`

- **GET** `/users/pharmacists/{userId}`
  - Get pharmacist profile for a user

- **PUT** `/users/pharmacists/{userId}/verification`
  - Verify or reject pharmacist professional profile (admin only)
  - Body: `{ status: VERIFIED|REJECTED|PENDING_VERIFICATION, note }`
  - Verification requires uploaded FRONT + BACK proof documents with clean malware scan.

### Professional Documents
- **POST** `/users/professional-documents/upload`
  - Upload proof document file (doctor or pharmacist)
  - Content-Type: `multipart/form-data`
  - Fields:
    - `userId`
    - `profileType` (`DOCTOR` or `PHARMACIST`)
    - `side` (`FRONT` or `BACK`)
    - `file` (jpg/jpeg/png/pdf, max 5MB)
  - Stores encrypted file + metadata with versioning.

- **GET** `/users/professional-documents/user/{userId}`
  - List stored proof documents for user
  - Returns signed `downloadUrl` links (short-lived)

- **GET** `/users/professional-documents/{documentId}/download?exp=...&sig=...`
  - Download encrypted-at-rest document through signed link
  - Requires authenticated owner or admin

- **GET** `/users/professional-documents/audit/{userId}`
  - Admin-only immutable history of uploads + verification status transitions

### Subscription Management
- **PUT** `/users/{userId}/subscription`
  - Create or update subscription
  - Body: `{ plan, duration }`
  - Returns: `SubscriptionResponse`

- **DELETE** `/users/{userId}/subscription`
  - Cancel subscription

### Clinic Management
- **POST** `/users/{userId}/clinics`
  - Add user to clinic
  - Body: `{ clinicId }`

- **GET** `/users/{userId}/clinics`
  - Get clinics for user
  - Returns: `List<ClinicAccountResponse>`

- **POST** `/users/clinics/{clinicId}/invite`
  - Invite user to clinic
  - Body: `{ email, role }`

### Bulk Import
- **POST** `/users/batch-import`
  - Bulk import users from CSV
  - Content-Type: `multipart/form-data`
  - File: CSV file with user data

- **GET** `/users/batch-import/{importId}/status`
  - Check bulk import status
  - Path: `importId` — Import task ID
  - Returns: `{ status, processed, succeeded, failed, errors[] }`

### User Search
- **GET** `/users/search`
  - Search users
  - Query: `?query=name&role=DOCTOR&enabled=true`

---

## Authentication & Security

### Headers Required
```
Authorization: Bearer {jwt_token}
Content-Type: application/json
```

### Response Format (Success)
```json
{
  "status": 200,
  "data": { ... },
  "message": "Success"
}
```

### Response Format (Error)
```json
{
  "status": 400/401/403/404/500,
  "error": "Error message",
  "timestamp": "2026-05-07T10:00:00Z"
}
```

### Status Codes
- `200` — Success
- `201` — Created
- `400` — Bad Request
- `401` — Unauthorized
- `403` — Forbidden
- `404` — Not Found
- `429` — Rate Limited (5 attempts per 15 minutes)
- `500` — Server Error

---

## Rate Limiting
- Login attempts: **5 failures per 15 minutes** → Account locked
- OTP verification: **3 attempts per token**
- General API: Rate limits apply per user

---

## Data Models (Key Fields)

### User
```json
{
  "id": "user-uuid",
  "email": "user@example.com",
  "nom": "Last Name",
  "prenom": "First Name",
  "telephone": "+1234567890",
  "role": "PATIENT|DOCTOR|PHARMACIST|ADMIN|CLINIC_MANAGER",
  "enabled": true,
  "emailVerified": true,
  "createdAt": "2026-05-07T10:00:00Z"
}
```

### PatientProfile
```json
{
  "userId": "user-uuid",
  "bloodType": "O+",
  "medicalHistory": "...",
  "allergies": ["Penicillin"],
  "createdAt": "2026-05-07T10:00:00Z"
}
```

### DoctorProfile
```json
{
  "userId": "user-uuid",
  "professionalRegistrationNumber": "MED-2026-12345",
  "nationalIdNumber": "AB123456",
  "specialty": "Cardiology",
  "city": "Casablanca",
  "cardFrontImageUrl": "https://cdn.medconnect.ma/proofs/doc-front.jpg",
  "cardBackImageUrl": "https://cdn.medconnect.ma/proofs/doc-back.jpg",
  "verificationStatus": "PENDING_VERIFICATION",
  "verificationNote": null,
  "verifiedAt": null
}
```

### PharmacistProfile
```json
{
  "userId": "user-uuid",
  "professionalRegistrationNumber": "PHARM-2026-9876",
  "nationalIdNumber": "CD456789",
  "pharmacyName": "Pharmacie Centre",
  "city": "Rabat",
  "cardFrontImageUrl": "https://cdn.medconnect.ma/proofs/pharm-front.jpg",
  "cardBackImageUrl": "https://cdn.medconnect.ma/proofs/pharm-back.jpg",
  "verificationStatus": "PENDING_VERIFICATION",
  "verificationNote": null,
  "verifiedAt": null
}
```

### AuthSession
```json
{
  "sessionId": "session-uuid",
  "userId": "user-uuid",
  "createdAt": "2026-05-07T10:00:00Z",
  "expiresAt": "2026-05-14T10:00:00Z",
  "userAgent": "Mozilla/5.0...",
  "ipAddress": "192.168.1.1"
}
```

---

## Common Request/Response Examples

### Login
```bash
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}

# Response
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "eyJhbGc...",
  "user": { ... }
}
```

### Get Current User
```bash
GET /api/users/me
Authorization: Bearer eyJhbGc...

# Response
{
  "id": "user-uuid",
  "email": "user@example.com",
  "nom": "Doe",
  "prenom": "John",
  "role": "PATIENT"
}
```

### Create Patient
```bash
POST /api/users/patients
Authorization: Bearer eyJhbGc...
Content-Type: application/json

{
  "userId": "user-uuid",
  "bloodType": "O+",
  "medicalHistory": "No significant history",
  "allergies": ["Penicillin"]
}
```

---

## Gateway Routing

All requests go through **api-gateway** (port 8080), which routes to **user-service** (port 8081):
```
Client → Gateway (8080) → User Service (8081)
         API Endpoints are exposed at :8080/api
```

---

---

## Endpoint Summary

**Total: 39 Endpoints**
- Auth endpoints: 17 (including aliases)
- User Management endpoints: 14
- User CRUD endpoints: 8

Last Updated: 2026-05-07
