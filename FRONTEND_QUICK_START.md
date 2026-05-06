# 🚀 Frontend Quick Start Guide

**TL;DR - Everything you need to integrate with the backend in 5 minutes**

---

## ✅ What's Ready for Frontend

**MS-01 (Authentication)** - 100% Complete ✅
- Signup with email verification
- Login with optional 2FA (TOTP, SMS, Email)
- Google OAuth integration
- Token refresh (automatic)
- Session management
- Password reset
- Logout all devices

**MS-02 (User Management)** - 100% Complete ✅
- Patient/Doctor/Pharmacist profiles
- Subscription plans (BASIC, PREMIUM, ENTERPRISE)
- Doctor search by specialty/language/location
- Bulk CSV import for users
- Clinic account management with team invites
- Subscription upgrade/downgrade with payment integration

---

## 🔧 Setup Steps

### 1. Backend URL Configuration
```javascript
// .env.local
REACT_APP_API_URL=http://localhost:8080  // Development
// OR
REACT_APP_API_URL=https://api.medconnect.fr  // Production
```

### 2. Install Axios (or Fetch)
```bash
npm install axios
```

### 3. Create API Client
```javascript
// src/api/client.js
import axios from 'axios';

const api = axios.create({
  baseURL: process.env.REACT_APP_API_URL,
  headers: {
    'Content-Type': 'application/json'
  }
});

// Auto-refresh token
api.interceptors.response.use(
  response => response,
  async error => {
    if (error.response?.status === 401) {
      const refreshToken = localStorage.getItem('refreshToken');
      if (refreshToken) {
        try {
          const { data } = await axios.post(`${process.env.REACT_APP_API_URL}/api/auth/refresh`, {
            refreshToken
          });
          localStorage.setItem('accessToken', data.accessToken);
          localStorage.setItem('refreshToken', data.refreshToken);
          // Retry original request
          return api(error.config);
        } catch (err) {
          // Logout user
          localStorage.clear();
          window.location.href = '/login';
        }
      }
    }
    return Promise.reject(error);
  }
);

export default api;
```

### 4. Add Auth Interceptor
```javascript
// Before each request, add token
api.interceptors.request.use(config => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
```

---

## 📝 Key Endpoints at a Glance

### Authentication

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/auth/signup` | Create account |
| POST | `/api/auth/verify-email` | Verify email with OTP |
| POST | `/api/auth/login` | Login (returns tokens) |
| POST | `/api/auth/mfa/setup` | Enable 2FA |
| POST | `/api/auth/mfa/verify` | Verify 2FA code |
| POST | `/api/auth/logout` | Logout current session |
| POST | `/api/auth/logout-all-devices` | Logout all sessions |
| POST | `/api/auth/refresh` | Get new access token |
| GET | `/api/auth/sessions` | List active sessions |
| DELETE | `/api/auth/sessions/{id}` | Revoke session |

### User Profiles

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/users/patients` | Create patient profile |
| GET | `/api/users/patients/{userId}` | Get patient info |
| PUT | `/api/users/patients/{userId}` | Update patient |
| POST | `/api/users/doctors` | Create doctor profile |
| GET | `/api/users/doctors/search` | Search doctors |
| POST | `/api/users/pharmacists` | Create pharmacist |

### Subscriptions

| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/api/users/{userId}/subscription` | Get current plan |
| PUT | `/api/users/{userId}/subscription` | Upgrade/downgrade |
| DELETE | `/api/users/{userId}/subscription` | Cancel |

### Bulk Import

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/users/batch-import` | Upload CSV |
| GET | `/api/users/batch-import/{id}/status` | Check import progress |

### Clinic Management

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/users/{userId}/clinics` | Create clinic |
| POST | `/api/clinics/{id}/invite` | Invite team member |
| GET | `/api/users/{userId}/clinics` | List clinics |

---

## 💡 Common Flows

### Login Flow

```javascript
const login = async (email, password) => {
  const { data } = await api.post('/api/auth/login', { email, password });
  
  // Save tokens
  localStorage.setItem('accessToken', data.accessToken);
  localStorage.setItem('refreshToken', data.refreshToken);
  
  // Check if 2FA required
  if (data.mfaRequired) {
    // Show 2FA input
    // Call /api/auth/verify-login with code
  }
  
  return data.user;
};
```

### Create Patient Profile

```javascript
const createPatient = async (userId, profileData) => {
  const { data } = await api.post('/api/users/patients', {
    userId,
    bloodType: profileData.bloodType,
    dateOfBirth: profileData.dob,
    allergies: profileData.allergies
  });
  return data;
};
```

### Search Doctors

```javascript
const searchDoctors = async (specialty, language, city) => {
  const { data } = await api.get('/api/users/doctors/search', {
    params: { specialty, language, city }
  });
  return data.doctors;
};
```

### Upgrade Subscription

```javascript
const upgradeSubscription = async (userId, newPlan, stripePaymentRef) => {
  const { data } = await api.put(`/api/users/${userId}/subscription`, {
    newPlan,  // "PREMIUM" or "ENTERPRISE"
    paymentReference: stripePaymentRef
  });
  return data;
};
```

### Bulk Import Users

```javascript
const uploadCsv = async (file, userRole) => {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('userRole', userRole);  // PATIENT, DOCTOR, etc.
  
  const { data } = await api.post('/api/users/batch-import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  });
  
  return data.importId;  // Check status later
};
```

---

## 🔒 Token Management

### Store Tokens
```javascript
// RECOMMENDED: Use secure HTTP-only cookies (backend handles this)
// or secure sessionStorage

localStorage.setItem('accessToken', token);  // 15 min expiry
localStorage.setItem('refreshToken', refreshToken);  // 7 day expiry
```

### Auto-Refresh Tokens
```javascript
// Refresh 1 minute before expiry
setInterval(() => {
  const accessToken = localStorage.getItem('accessToken');
  if (accessToken) {
    // Check expiry time and refresh if needed
    const decoded = jwt_decode(accessToken);
    const timeUntilExpiry = (decoded.exp * 1000) - Date.now();
    
    if (timeUntilExpiry < 60000) {  // < 1 minute
      // Refresh token
      api.post('/api/auth/refresh', {
        refreshToken: localStorage.getItem('refreshToken')
      });
    }
  }
}, 30000);  // Check every 30 seconds
```

### Logout
```javascript
const logout = async () => {
  await api.post('/api/auth/logout');
  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
  window.location.href = '/login';
};
```

---

## ⚠️ Rate Limiting & Error Handling

### Handle Rate Limits
```javascript
try {
  await api.post('/api/auth/login', { email, password });
} catch (error) {
  if (error.response?.status === 429) {
    const retryAfter = error.response.headers['retry-after'];
    alert(`Too many attempts. Try again in ${retryAfter} seconds.`);
  }
}
```

### Handle Expired Tokens
```javascript
api.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      localStorage.clear();
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);
```

---

## 🧪 Test Credentials

**For Development Testing:**
```
Email: test@example.com
Password: TestPassword123!
```

**Create via API:**
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "TestPassword123!",
    "firstName": "Test",
    "lastName": "User",
    "userRole": "PATIENT"
  }'
```

Then verify email with OTP sent to console or mail service.

---

## 📚 Full Documentation

**For all endpoints and detailed examples**, see: `FRONTEND_API_GUIDE.md`

---

## 🐛 Troubleshooting

| Issue | Solution |
|-------|----------|
| CORS error | Backend CORS is restricted to `localhost:3000` in dev |
| 401 Unauthorized | Access token expired, refresh or login again |
| 429 Too Many Requests | Rate limit hit (5 attempts/15 min). Wait before retrying |
| Email not received | Check spam/junk folder; resend OTP with `/api/auth/resend-otp` |
| MFA not working | Ensure clock is synchronized on device with authenticator app |

---

**Backend Status**: ✅ Production Ready  
**Last Updated**: 2026-05-06  
**Contact**: api-support@medconnect.fr
