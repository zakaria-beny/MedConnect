# 🚀 MedConnect Frontend API Guide
## Complete Backend API Reference for Frontend Integration

**Backend URL**: `http://localhost:8080` (development) | `https://api.medconnect.fr` (production)  
**API Version**: v1.0  
**Last Updated**: 2026-05-06

---

## 📋 Table of Contents
1. [Authentication (MS-01)](#ms-01-authentication)
2. [User Management (MS-02)](#ms-02-user-management)
3. [Common Response Format](#common-response-format)
4. [Error Handling](#error-handling)
5. [Environment Variables](#environment-variables)
6. [Examples](#examples)

---

## MS-01: Authentication

### 1. **Signup (Email)**
```
POST /api/auth/signup
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecurePassword123!",
  "firstName": "John",
  "lastName": "Doe",
  "userRole": "PATIENT"  // or DOCTOR, PHARMACIST, CLINIC_ADMIN
}

Response (201):
{
  "message": "Signup successful. Check your email to verify.",
  "userId": "67a1b2c3d4e5f6g7h8i9j0k1"
}
```

### 2. **Verify Email**
```
POST /api/auth/verify-email
Content-Type: application/json

{
  "email": "user@example.com",
  "otp": "123456"  // 6-digit code from email
}

Response (200):
{
  "message": "Email verified successfully"
}
```

### 3. **Login**
```
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecurePassword123!"
}

Response (200):
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "67a1b2c3d4e5f6g7h8i9j0k1",
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "userRole": "PATIENT"
  },
  "mfaRequired": false,  // If true, user must verify MFA before proceeding
  "sessionId": "sess_abc123"
}
```

**⚠️ Note**: If `mfaRequired: true`, user must call `/api/auth/verify-login` with MFA code.

### 4. **Verify MFA Code (2FA)**
```
POST /api/auth/verify-login
Content-Type: application/json

{
  "userId": "67a1b2c3d4e5f6g7h8i9j0k1",
  "mfaCode": "123456",  // From authenticator app or SMS
  "sessionId": "sess_abc123"  // From login response
}

Response (200):
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "message": "2FA verified successfully"
}
```

### 5. **Refresh Token**
```
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}

Response (200):
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",  // New token (15 min expiry)
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",  // Rotated
  "expiresIn": 900  // seconds
}
```

### 6. **MFA Setup - TOTP (Authenticator App)**
```
POST /api/auth/mfa/setup
Authorization: Bearer <accessToken>
Content-Type: application/json

{
  "mfaType": "TOTP"  // or SMS, EMAIL
}

Response (200):
{
  "secret": "JBSWY3DPEBLW64TMMQ======",
  "qrCode": "data:image/png;base64,iVBORw0KGgo...",  // Embed in <img> tag
  "backupCodes": ["12345-67890", "11111-22222", "33333-44444"],
  "message": "Scan QR code with Google Authenticator, Microsoft Authenticator, or Authy"
}
```

**Frontend Steps:**
1. Display QR code to user
2. User scans with authenticator app
3. User enters 6-digit code → call `/api/auth/mfa/verify`
4. Save backup codes somewhere safe

### 7. **MFA Enable - Verify Code**
```
POST /api/auth/mfa/verify
Authorization: Bearer <accessToken>
Content-Type: application/json

{
  "code": "123456",  // 6-digit code from authenticator app
  "mfaType": "TOTP"
}

Response (200):
{
  "message": "MFA enabled successfully",
  "backupCodes": ["12345-67890", "11111-22222", "33333-44444"]
}
```

### 8. **MFA Setup - SMS**
```
POST /api/auth/mfa/setup
Authorization: Bearer <accessToken>
Content-Type: application/json

{
  "mfaType": "SMS",
  "phoneNumber": "+33612345678"
}

Response (200):
{
  "message": "SMS code sent to +33612345678",
  "phoneNumber": "+33612345678"
}
```

Then verify with `/api/auth/mfa/verify` (code from SMS).

### 9. **Logout**
```
POST /api/auth/logout
Authorization: Bearer <accessToken>
Content-Type: application/json

{
  "sessionId": "sess_abc123"  // Optional; if omitted, revokes current session
}

Response (200):
{
  "message": "Logged out successfully"
}
```

### 10. **Logout All Devices** ⭐ NEW
```
POST /api/auth/logout-all-devices
Authorization: Bearer <accessToken>

Response (200):
{
  "message": "All sessions revoked",
  "sessionsRevoked": 5
}
```

### 11. **List Active Sessions**
```
GET /api/auth/sessions
Authorization: Bearer <accessToken>

Response (200):
{
  "sessions": [
    {
      "sessionId": "sess_abc123",
      "deviceName": "Chrome on MacOS",
      "ipAddress": "192.168.1.100",
      "createdAt": "2026-05-06T10:30:00Z",
      "lastActivityAt": "2026-05-06T20:45:00Z"
    }
  ]
}
```

### 12. **Revoke Session**
```
DELETE /api/auth/sessions/{sessionId}
Authorization: Bearer <accessToken>

Response (200):
{
  "message": "Session revoked successfully"
}
```

### 13. **Forgot Password - Request Reset**
```
POST /api/auth/password/reset-request
Content-Type: application/json

{
  "email": "user@example.com"
}

Response (200):
{
  "message": "Reset OTP sent to your email"
}
```

### 14. **Reset Password**
```
POST /api/auth/password/reset
Content-Type: application/json

{
  "email": "user@example.com",
  "otp": "123456",
  "newPassword": "NewSecurePassword456!"
}

Response (200):
{
  "message": "Password reset successfully. Please login with your new password."
}
```

### 15. **Google OAuth Login**
```
POST /api/auth/google
Content-Type: application/json

{
  "idToken": "eyJhbGciOiJSUzI1NiIsImtpZCI6Ijk5..."  // Token from Google Sign-In
}

Response (200):
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "67a1b2c3d4e5f6g7h8i9j0k1",
    "email": "user@gmail.com",
    "firstName": "John",
    "lastName": "Doe"
  }
}
```

---

## MS-02: User Management

### User Profiles

#### 16. **Create Patient Profile**
```
POST /api/users/patients
Authorization: Bearer <accessToken>
Content-Type: application/json

{
  "userId": "67a1b2c3d4e5f6g7h8i9j0k1",
  "bloodType": "O+",
  "dateOfBirth": "1990-05-15",
  "allergies": ["Penicillin", "Shellfish"],
  "chronicConditions": ["Diabetes", "Hypertension"],
  "insuranceNumber": "INS123456789",
  "insuranceProvider": "SNCF"
}

Response (201):
{
  "id": "pat_xyz789",
  "userId": "67a1b2c3d4e5f6g7h8i9j0k1",
  "bloodType": "O+",
  "dateOfBirth": "1990-05-15",
  "allergies": ["Penicillin", "Shellfish"],
  "chronicConditions": ["Diabetes", "Hypertension"],
  "createdAt": "2026-05-06T10:30:00Z"
}
```

#### 17. **Get Patient Profile**
```
GET /api/users/patients/{userId}
Authorization: Bearer <accessToken>

Response (200):
{
  "id": "pat_xyz789",
  "userId": "67a1b2c3d4e5f6g7h8i9j0k1",
  "bloodType": "O+",
  "dateOfBirth": "1990-05-15",
  "allergies": ["Penicillin", "Shellfish"],
  "chronicConditions": ["Diabetes", "Hypertension"],
  "insuranceNumber": "INS123456789",
  "insuranceProvider": "SNCF"
}
```

#### 18. **Update Patient Profile**
```
PUT /api/users/patients/{userId}
Authorization: Bearer <accessToken>
Content-Type: application/json

{
  "bloodType": "O+",
  "allergies": ["Penicillin", "Shellfish", "Latex"]
}

Response (200):
{
  "id": "pat_xyz789",
  "allergies": ["Penicillin", "Shellfish", "Latex"],
  "updatedAt": "2026-05-06T15:30:00Z"
}
```

### Doctor Profiles

#### 19. **Create Doctor Profile**
```
POST /api/users/doctors
Authorization: Bearer <accessToken>
Content-Type: application/json

{
  "userId": "67a1b2c3d4e5f6g7h8i9j0k1",
  "licenseNumber": "RPPS12345678901",  // French medical license
  "specialty": "Cardiologist",
  "languages": ["French", "English"],
  "clinicName": "Heart Care Clinic",
  "city": "Paris",
  "zipCode": "75001",
  "address": "123 Rue de la Paix",
  "phoneNumber": "+33123456789"
}

Response (201):
{
  "id": "doc_abc123",
  "userId": "67a1b2c3d4e5f6g7h8i9j0k1",
  "licenseNumber": "RPPS12345678901",
  "specialty": "Cardiologist",
  "languages": ["French", "English"],
  "city": "Paris",
  "createdAt": "2026-05-06T10:30:00Z"
}
```

#### 20. **Search Doctors**
```
GET /api/users/doctors/search?specialty=Cardiologist&language=English&city=Paris
Authorization: Bearer <accessToken>

Response (200):
{
  "doctors": [
    {
      "id": "doc_abc123",
      "name": "Dr. Jean Dupont",
      "specialty": "Cardiologist",
      "languages": ["French", "English"],
      "city": "Paris",
      "rating": 4.8,
      "availableSlots": 5
    }
  ],
  "total": 1
}
```

#### 21. **Update Doctor Profile**
```
PUT /api/users/doctors/{doctorId}
Authorization: Bearer <accessToken>
Content-Type: application/json

{
  "specialty": "Cardiologist",
  "languages": ["French", "English", "Spanish"]
}

Response (200):
{
  "id": "doc_abc123",
  "languages": ["French", "English", "Spanish"],
  "updatedAt": "2026-05-06T15:30:00Z"
}
```

### Pharmacist Profiles

#### 22. **Create Pharmacist Profile**
```
POST /api/users/pharmacists
Authorization: Bearer <accessToken>
Content-Type: application/json

{
  "userId": "67a1b2c3d4e5f6g7h8i9j0k1",
  "licenseNumber": "FINESS12345678901",
  "pharmacyName": "Pharmacy Central",
  "city": "Paris",
  "address": "456 Rue du Commerce",
  "phoneNumber": "+33987654321",
  "operatingHours": "09:00-20:00",
  "deliveryAvailable": true
}

Response (201):
{
  "id": "pharm_xyz789",
  "userId": "67a1b2c3d4e5f6g7h8i9j0k1",
  "pharmacyName": "Pharmacy Central",
  "city": "Paris",
  "createdAt": "2026-05-06T10:30:00Z"
}
```

### Subscription Management

#### 23. **Get Current Subscription**
```
GET /api/users/{userId}/subscription
Authorization: Bearer <accessToken>

Response (200):
{
  "id": "sub_abc123",
  "userId": "67a1b2c3d4e5f6g7h8i9j0k1",
  "plan": "PREMIUM",
  "maxPatients": 1000,
  "maxAppointmentsPerMonth": 3000,
  "currentPatients": 247,
  "currentAppointments": 1250,
  "status": "ACTIVE",
  "startDate": "2026-01-01",
  "renewalDate": "2026-06-01",
  "price": 99.99,
  "currency": "EUR"
}
```

#### 24. **Upgrade/Downgrade Subscription** ⭐ NEW
```
PUT /api/users/{userId}/subscription
Authorization: Bearer <accessToken>
Content-Type: application/json

{
  "newPlan": "ENTERPRISE",  // BASIC, PREMIUM, ENTERPRISE
  "paymentReference": "pi_1234567890"  // From Stripe payment intent (if upgrading)
}

Response (200):
{
  "id": "sub_abc123",
  "plan": "ENTERPRISE",
  "maxPatients": 10000,
  "maxAppointmentsPerMonth": 20000,
  "status": "ACTIVE",
  "renewalDate": "2026-06-01",
  "message": "Subscription upgraded successfully"
}
```

#### 25. **Cancel Subscription**
```
DELETE /api/users/{userId}/subscription
Authorization: Bearer <accessToken>

Response (200):
{
  "message": "Subscription cancelled. Your account will be active until 2026-06-01."
}
```

### Bulk User Import

#### 26. **Batch Import Users (CSV)**
```
POST /api/users/batch-import
Authorization: Bearer <accessToken>
Content-Type: multipart/form-data

Form Data:
- file: <CSV file>
- userRole: PATIENT  // or DOCTOR, PHARMACIST

CSV Format (comma-separated):
firstName,lastName,email,dateOfBirth,bloodType,allergies
John,Doe,john@example.com,1990-05-15,O+,"Penicillin,Shellfish"
Jane,Smith,jane@example.com,1985-03-22,B-,"None"

Response (202 - Accepted):
{
  "importId": "imp_abc123",
  "fileName": "patients.csv",
  "totalRows": 100,
  "status": "PROCESSING",
  "message": "Import started. Check status with importId."
}
```

#### 27. **Get Import Status**
```
GET /api/users/batch-import/{importId}/status
Authorization: Bearer <accessToken>

Response (200):
{
  "importId": "imp_abc123",
  "fileName": "patients.csv",
  "totalRows": 100,
  "successCount": 95,
  "failureCount": 5,
  "status": "COMPLETED",
  "errors": [
    {
      "row": 5,
      "email": "invalid@.com",
      "error": "Invalid email format"
    }
  ],
  "downloadLink": "/api/users/batch-import/imp_abc123/report"
}
```

### Clinic Account Management

#### 28. **Create Clinic Account**
```
POST /api/users/{userId}/clinics
Authorization: Bearer <accessToken>
Content-Type: application/json

{
  "clinicName": "Heart Care Clinic",
  "siretNumber": "12345678901234",  // SIRET number (France)
  "address": "123 Rue de la Paix",
  "city": "Paris",
  "zipCode": "75001",
  "phoneNumber": "+33123456789"
}

Response (201):
{
  "id": "clinic_abc123",
  "userId": "67a1b2c3d4e5f6g7h8i9j0k1",
  "clinicName": "Heart Care Clinic",
  "siretNumber": "12345678901234",
  "status": "ACTIVE",
  "createdAt": "2026-05-06T10:30:00Z"
}
```

#### 29. **Invite Team Member to Clinic**
```
POST /api/users/clinics/{clinicId}/invite
Authorization: Bearer <accessToken>
Content-Type: application/json

{
  "email": "doctor@example.com",
  "role": "DOCTOR",  // or NURSE, RECEPTIONIST, ADMIN
  "invitationMessage": "Please join our team!"
}

Response (200):
{
  "message": "Invitation sent to doctor@example.com",
  "invitationId": "inv_xyz789",
  "expiresAt": "2026-05-13T20:45:00Z"  // 7 days
}
```

#### 30. **List Clinics by User**
```
GET /api/users/{userId}/clinics
Authorization: Bearer <accessToken>

Response (200):
{
  "clinics": [
    {
      "id": "clinic_abc123",
      "clinicName": "Heart Care Clinic",
      "siretNumber": "12345678901234",
      "city": "Paris",
      "teamMembers": 5,
      "createdAt": "2026-05-06T10:30:00Z"
    }
  ]
}
```

---

## Common Response Format

### Success Response
```json
{
  "data": { /* response body */ },
  "message": "Operation successful",
  "timestamp": "2026-05-06T20:45:00Z"
}
```

### Error Response
```json
{
  "error": {
    "code": "INVALID_EMAIL",
    "message": "Email format is invalid",
    "details": "user@.com is not a valid email"
  },
  "timestamp": "2026-05-06T20:45:00Z"
}
```

---

## Error Handling

### HTTP Status Codes

| Code | Meaning | Example |
|------|---------|---------|
| 200 | ✅ Success | Login successful |
| 201 | ✅ Created | User profile created |
| 202 | ⏳ Accepted | Bulk import queued |
| 400 | ❌ Bad Request | Invalid email format |
| 401 | ❌ Unauthorized | Invalid token |
| 403 | ❌ Forbidden | No permission to access |
| 404 | ❌ Not Found | User not found |
| 409 | ❌ Conflict | Email already exists |
| 429 | ❌ Too Many Requests | Rate limit exceeded (5 attempts/15 min) |
| 500 | ❌ Server Error | Database connection failed |

### Common Errors

```json
{
  "error": {
    "code": "EMAIL_ALREADY_EXISTS",
    "message": "An account with this email already exists"
  }
}
```

```json
{
  "error": {
    "code": "RATE_LIMIT_EXCEEDED",
    "message": "Too many login attempts. Please try again in 15 minutes.",
    "retryAfter": 900  // seconds
  }
}
```

```json
{
  "error": {
    "code": "INVALID_TOKEN",
    "message": "Your session has expired. Please login again."
  }
}
```

---

## Environment Variables

**Backend Configuration** (`.env` file or system variables):

```bash
# JWT Configuration
JWT_SECRET=your-secret-key-min-32-chars
JWT_EXPIRATION=900000  # 15 minutes in milliseconds
JWT_REFRESH_EXPIRATION=604800000  # 7 days

# Twilio SMS
TWILIO_ACCOUNT_SID=your_account_sid
TWILIO_AUTH_TOKEN=your_auth_token
TWILIO_PHONE_NUMBER=+1234567890

# Email Service
MAIL_USERNAME=noreply@medconnect.fr
MAIL_PASSWORD=your_app_password

# MongoDB
MONGODB_URI=mongodb://localhost:27017/medconnect

# Stripe (Optional)
STRIPE_SECRET_KEY=sk_test_...
STRIPE_PUBLISHABLE_KEY=pk_test_...

# Google OAuth
GOOGLE_CLIENT_ID=your_google_client_id

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
KAFKA_TOPICS_USER_CREATED=user.created
KAFKA_TOPICS_USER_LOGIN=user.login

# API Gateway
GATEWAY_CORS_ALLOWED_ORIGINS=http://localhost:3000,https://app.medconnect.fr
```

---

## Examples

### Example 1: Complete Authentication Flow

**Step 1: Signup**
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "SecurePass123!",
    "firstName": "John",
    "lastName": "Doe",
    "userRole": "PATIENT"
  }'
```

**Step 2: Verify Email (check email for OTP)**
```bash
curl -X POST http://localhost:8080/api/auth/verify-email \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "otp": "123456"
  }'
```

**Step 3: Login**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "SecurePass123!"
  }'
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "mfaRequired": false,
  "user": {
    "id": "67a1b2c3d4e5f6g7h8i9j0k1",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe"
  }
}
```

### Example 2: API Request with Authentication

```bash
curl -X POST http://localhost:8080/api/users/patients \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "userId": "67a1b2c3d4e5f6g7h8i9j0k1",
    "bloodType": "O+",
    "dateOfBirth": "1990-05-15",
    "allergies": ["Penicillin"]
  }'
```

### Example 3: JavaScript/Fetch Example

```javascript
// Login
const response = await fetch('http://localhost:8080/api/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    email: 'john@example.com',
    password: 'SecurePass123!'
  })
});

const { accessToken, user } = await response.json();
localStorage.setItem('accessToken', accessToken);

// Use access token for subsequent requests
const patientResponse = await fetch(
  `http://localhost:8080/api/users/patients/${user.id}`,
  {
    headers: {
      'Authorization': `Bearer ${accessToken}`
    }
  }
);

const patientProfile = await patientResponse.json();
```

### Example 4: Enable TOTP MFA

```javascript
// Step 1: Request MFA setup
const setupResponse = await fetch('http://localhost:8080/api/auth/mfa/setup', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${accessToken}`
  },
  body: JSON.stringify({ mfaType: 'TOTP' })
});

const { secret, qrCode, backupCodes } = await setupResponse.json();

// Display QR code to user
document.getElementById('qr-code').src = qrCode;
document.getElementById('backup-codes').textContent = backupCodes.join('\n');

// Step 2: User scans QR and enters code
const verifyResponse = await fetch('http://localhost:8080/api/auth/mfa/verify', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${accessToken}`
  },
  body: JSON.stringify({
    code: '123456',  // From authenticator app
    mfaType: 'TOTP'
  })
});

if (verifyResponse.ok) {
  alert('MFA enabled! Save your backup codes.');
}
```

---

## Rate Limiting & Account Security

### Login Rate Limiting
- **Limit**: 5 failed attempts per 15 minutes
- **Response**: 429 Too Many Requests with retry-after header
- **Lock Duration**: 15 minutes (automatic)

### Account Lockout
- **Trigger**: 5 failed login attempts in 15 minutes
- **Status**: Account becomes temporarily locked
- **Recovery**: Wait 15 minutes or contact support

### Token Expiration
- **Access Token**: 15 minutes
- **Refresh Token**: 7 days
- **Session Lifetime**: Until logout or 30 days of inactivity

---

## Security Best Practices

1. **Store tokens securely**:
   - Access token: In-memory or sessionStorage (not localStorage for security)
   - Refresh token: Secure HttpOnly cookie (preferred) or sessionStorage

2. **Token refresh**:
   - Refresh access token 1 minute before expiry
   - Implement automatic token refresh on 401 response

3. **HTTPS in Production**:
   - Always use HTTPS for API calls
   - Set secure flag on cookies

4. **CORS**:
   - Backend only allows requests from whitelisted domains
   - Development: `http://localhost:3000`
   - Production: `https://app.medconnect.fr`

5. **Protect Refresh Tokens**:
   - Never include in URL or logs
   - Rotate on every use (done automatically by backend)

---

## Support

**Issues or Questions?**
- 📧 Email: api-support@medconnect.fr
- 🐛 Bug Reports: [GitHub Issues](https://github.com/zakaria-beny/MedConnect/issues)
- 📖 Documentation: [Full Spec](./SERVICE-BREAKDOWN.md)

---

**Last Updated**: 2026-05-06  
**API Version**: v1.0  
**Status**: Production Ready ✅
