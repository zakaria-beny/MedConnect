# MedConnect API Reference (Frontend)

Frontend scope: MS-01 Authentication + MS-02 User Management. DMP is excluded.

## Base URL

Use the gateway in React:

```txt
http://localhost:8080/api
```

## Auth header

```txt
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

## Auth / role legend

- **Public**: no token
- **Authenticated**: any logged-in user
- **Owner/Admin**: the resource owner or admin
- **Admin**: `ROLE_ADMIN`

---

## 1) Authentication (`/api/auth`)

| Method | Endpoint | Auth | Request body | Returns | Frontend use |
|---|---|---|---|---|---|
| POST | `/signup` or `/register` | Public | `{ nom, prenom, email, password, telephone? }` | `message` | Create account |
| POST | `/verify-email` | Public | `{ email, code }` | `JwtResponse` | Verify email and get tokens |
| POST | `/resend-otp` | Public | `{ email }` | `message` | Resend email OTP |
| POST | `/signin` or `/login` | Public | `{ email, password }` | `requiresOtp, email, mfaMethod, message` | Start login |
| POST | `/verify-login` | Public | `{ email, code }` | `JwtResponse` | Finish login |
| POST | `/forgot-password` | Public | `{ email }` | `message` | Send reset OTP |
| POST | `/reset-password` | Public | `{ email, code, newPassword }` | `message` | Reset password |
| POST | `/google` | Public | `{ idToken }` | `JwtResponse` + user fields | Google sign-in |
| POST | `/refresh` | Public | `{ refreshToken }` | `JwtResponse` | Rotate access token |
| POST | `/logout` | Authenticated | `Authorization` header only | `message` | Logout current session |
| GET | `/sessions` | Authenticated | — | `sessions[]`, `count` | Show active sessions |
| DELETE | `/sessions/{sessionId}` | Authenticated | — | `message` | Revoke one session |
| POST | `/logout-all-devices` | Authenticated | — | `message`, `sessionsRevoked` | Logout everywhere |
| POST | `/mfa/setup` | Authenticated | `{ method, phoneNumber? }` | MFA setup payload | Enable MFA |
| POST | `/mfa/verify` | Authenticated | `{ method, code }` | MFA verify payload | Confirm MFA setup |

### Auth response shapes

**JwtResponse**
```json
{
  "token": "jwt",
  "type": "Bearer",
  "id": "userId",
  "email": "user@email.com",
  "roles": ["ROLE_USER"],
  "sessionId": "session-id",
  "refreshToken": "refresh-token"
}
```

**Login challenge**
```json
{
  "requiresOtp": true,
  "email": "user@email.com",
  "mfaMethod": "EMAIL",
  "message": "OTP sent to your email."
}
```

**Session list**
```json
{
  "sessions": [
    {
      "sessionId": "id",
      "createdAt": "2026-05-21T10:00:00",
      "lastUsedAt": "2026-05-21T10:05:00",
      "expiresAt": "2026-05-21T10:20:00",
      "ipAddress": "127.0.0.1",
      "userAgent": "Mozilla/5.0"
    }
  ],
  "count": 1
}
```

---

## 2) Current user (`/api/users/me`)

| Method | Endpoint | Auth | Request body | Returns | Frontend use |
|---|---|---|---|---|---|
| GET | `/me` | Authenticated | — | `UserResponse` | Load profile |
| PUT | `/me` | Authenticated | `{ nom, prenom, telephone? }` | `UserResponse` | Update profile |

**UserResponse**
```json
{
  "id": "userId",
  "nom": "Doe",
  "prenom": "John",
  "email": "john@example.com",
  "telephone": "+212600000000",
  "roles": ["ROLE_USER"]
}
```

---

## 3) Admin user CRUD (`/api/users`)

| Method | Endpoint | Auth | Request body | Returns | Frontend use |
|---|---|---|---|---|---|
| POST | `/` | Admin | `{ nom, prenom, email, motDePasse, telephone?, roles[] }` | `UserResponse` | Admin create user |
| GET | `/` | Admin | — | `UserResponse[]` | Admin list users |
| GET | `/{id}` | Admin | — | `UserResponse` | Admin get user |
| PUT | `/{id}` | Admin | `{ nom, prenom, email, motDePasse, telephone?, roles[] }` | `UserResponse` | Admin update user |
| DELETE | `/{id}` | Admin | — | `204 No Content` | Admin delete user |
| PUT | `/{id}/suspend` | Admin | — | `UserResponse` | Suspend user |

---

## 4) Patient profiles (`/api/users/patients`)

| Method | Endpoint | Auth | Request body | Returns | Frontend use |
|---|---|---|---|---|---|
| POST | `/patients` | Owner/Admin | `{ userId, dateOfBirth?, bloodType?, insuranceNumber?, allergies[]? }` | `PatientProfileResponse` | Create patient profile |
| GET | `/patients/{userId}` | Owner/Admin | — | `PatientProfileResponse` | Load patient profile |
| PUT | `/patients/{userId}` | Owner/Admin | Same as create | `PatientProfileResponse` | Update patient profile |

**PatientProfileResponse**
```json
{
  "id": "profileId",
  "userId": "userId",
  "dateOfBirth": "1990-05-15",
  "bloodType": "O+",
  "insuranceNumber": "INS-123",
  "allergies": ["Penicillin"]
}
```

---

## 5) Doctor profiles (`/api/users/doctors`)

| Method | Endpoint | Auth | Request body | Returns | Frontend use |
|---|---|---|---|---|---|
| POST | `/doctors` | Owner/Admin | `{ userId, professionalRegistrationNumber, nationalIdNumber, registrationAuthority?, specialty, languages[]?, city?, clinicName?, cardFrontImageUrl?, cardBackImageUrl? }` | `DoctorProfileResponse` | Create doctor profile |
| GET | `/doctors/search` | Authenticated | Query: `specialty`, `language`, `city` | `DoctorProfileResponse[]` | Search doctors |
| GET | `/doctors/{userId}` | Owner/Admin | — | `DoctorProfileResponse` | Load doctor profile |
| PUT | `/doctors/{userId}/verification` | Admin | `{ status, note? }` | `DoctorProfileResponse` | Admin verify/reject |

**DoctorProfileResponse**
```json
{
  "id": "profileId",
  "userId": "userId",
  "professionalRegistrationNumber": "REG-123",
  "nationalIdNumber": "ID-123456",
  "registrationAuthority": "Order",
  "specialty": "Cardiology",
  "languages": ["French", "Arabic"],
  "city": "Casablanca",
  "clinicName": "Main Clinic",
  "cardFrontImageUrl": "https://...",
  "cardBackImageUrl": "https://...",
  "verificationStatus": "VERIFIED",
  "verificationNote": "OK",
  "verifiedAt": "2026-05-21T10:00:00"
}
```

---

## 6) Pharmacist profiles (`/api/users/pharmacists`)

| Method | Endpoint | Auth | Request body | Returns | Frontend use |
|---|---|---|---|---|---|
| POST | `/pharmacists` | Owner/Admin | `{ userId, professionalRegistrationNumber, nationalIdNumber, registrationAuthority?, pharmacyName, city?, openingHours?, deliveryAvailable?, cardFrontImageUrl?, cardBackImageUrl? }` | `PharmacistProfileResponse` | Create pharmacist profile |
| GET | `/pharmacists/{userId}` | Owner/Admin | — | `PharmacistProfileResponse` | Load pharmacist profile |
| PUT | `/pharmacists/{userId}/verification` | Admin | `{ status, note? }` | `PharmacistProfileResponse` | Admin verify/reject |

**PharmacistProfileResponse**
```json
{
  "id": "profileId",
  "userId": "userId",
  "professionalRegistrationNumber": "REG-123",
  "nationalIdNumber": "ID-123456",
  "registrationAuthority": "Order",
  "pharmacyName": "City Pharmacy",
  "city": "Rabat",
  "openingHours": "09:00-18:00",
  "deliveryAvailable": true,
  "cardFrontImageUrl": "https://...",
  "cardBackImageUrl": "https://...",
  "verificationStatus": "PENDING_VERIFICATION",
  "verificationNote": null,
  "verifiedAt": null
}
```

---

## 7) Professional documents (`/api/users/professional-documents`)

| Method | Endpoint | Auth | Request body | Returns | Frontend use |
|---|---|---|---|---|---|
| POST | `/upload` | Owner/Admin | `multipart/form-data`: `userId`, `profileType`, `side`, `file` | `ProfessionalDocumentResponse` | Upload proof document |
| GET | `/user/{userId}` | Owner/Admin | — | `ProfessionalDocumentResponse[]` | List uploaded docs |
| GET | `/{documentId}/download` | Owner/Admin | Query: `exp`, `sig` | File download | View document |
| GET | `/audit/{userId}` | Admin | — | `ProfessionalVerificationAuditLogResponse[]` | Admin audit trail |

**ProfessionalDocumentResponse**
```json
{
  "id": "docId",
  "userId": "userId",
  "profileType": "DOCTOR",
  "side": "FRONT",
  "originalFilename": "card.png",
  "contentType": "image/png",
  "sizeBytes": 12345,
  "version": 1,
  "active": true,
  "scanStatus": "PENDING",
  "uploadedAt": "2026-05-21T10:00:00",
  "downloadUrl": "http://localhost:8080/api/users/professional-documents/docId/download?exp=...&sig=..."
}
```

---

## 8) Subscriptions (`/api/users/{userId}/subscription`)

| Method | Endpoint | Auth | Request body | Returns | Frontend use |
|---|---|---|---|---|---|
| PUT | `/{userId}/subscription` | Owner/Admin | `{ planType, paymentReference? }` | `SubscriptionResponse` | Upgrade/downgrade |
| DELETE | `/{userId}/subscription` | Owner/Admin | — | `SubscriptionResponse` | Cancel subscription |

**SubscriptionResponse**
```json
{
  "id": "subscriptionId",
  "userId": "userId",
  "planType": "PREMIUM",
  "status": "ACTIVE",
  "maxPatients": 1000,
  "maxAppointmentsPerMonth": 100
}
```

---

## 9) Clinic accounts (`/api/users`)

| Method | Endpoint | Auth | Request body | Returns | Frontend use |
|---|---|---|---|---|---|
| POST | `/{userId}/clinics` | Owner/Admin | `{ name, siretNumber }` | `ClinicAccountResponse` | Create clinic |
| POST | `/clinics/{clinicId}/invite` | Authenticated | `{ userEmail }` | `ClinicAccountResponse` | Invite team member |
| GET | `/{userId}/clinics` | Owner/Admin | — | `ClinicAccountResponse[]` | List clinics |

**ClinicAccountResponse**
```json
{
  "id": "clinicId",
  "name": "Main Clinic",
  "siretNumber": "SIRET-123",
  "ownerUserId": "userId",
  "teamMemberIds": ["user2", "user3"]
}
```

---

## 10) Bulk import (`/api/users/batch-import`)

| Method | Endpoint | Auth | Request body | Returns | Frontend use |
|---|---|---|---|---|---|
| POST | `/batch-import` | Authenticated | `multipart/form-data` with `file` | `BulkImportResponse` | CSV import |
| GET | `/batch-import/{importId}/status` | Authenticated | — | `BulkImportResponse` | Check progress |

**BulkImportResponse**
```json
{
  "id": "importId",
  "userId": "userId",
  "fileName": "users.csv",
  "status": "COMPLETED",
  "totalRows": 100,
  "successCount": 96,
  "failedCount": 4,
  "errors": ["Row 3: invalid email"]
}
```

---

## 11) Search (`/api/users/search`)

| Method | Endpoint | Auth | Request body | Returns | Frontend use |
|---|---|---|---|---|---|
| GET | `/search` | Authenticated | Query: `specialty`, `city` | `DoctorProfileResponse[]` | Search users/doctors |

---

## React usage

```js
import axios from 'axios';

export const api = axios.create({
  baseURL: process.env.REACT_APP_API_URL || 'http://localhost:8080'
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});
```

### Common frontend flow

1. `POST /api/auth/signup`
2. `POST /api/auth/verify-email`
3. `POST /api/auth/login`
4. If `requiresOtp=true`, call `POST /api/auth/verify-login`
5. Save `accessToken`, `refreshToken`, `userId`, `roles`
6. Call `/api/users/me` and profile endpoints as needed

