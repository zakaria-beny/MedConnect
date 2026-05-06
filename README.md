# 🏥 MedConnect - Healthcare Backend Microservices

**Complete Spring Boot microservices platform for healthcare management**

A complete Spring Boot microservices architecture with authentication, user management, subscriptions, and event streaming.

**Status**: ✅ Ready | **Version**: 1.0.0 | **Java**: 17+ | **Spring Boot**: 3.2.3

## 📋 Quick Navigation

### For Backend Developers
- [Prerequisites](#prerequisites) - What you need installed
- [Backend Setup](#backend-setup-all-services) - Complete setup guide
- [Run Locally](#run-locally) - Start all 3 services
- [Testing](#testing) - Run tests

### For Frontend Developers (Oumaima 👩‍💻)
- [Frontend Integration](#frontend-integration) - How to connect frontend
- [API Reference](#api-endpoints---quick-reference) - All 30 endpoints
- [Examples](#examples) - Code snippets

### For DevOps/Deployment
- [Environment Variables](#environment-variables) - Development config
- [Troubleshooting](#troubleshooting) - Common issues

---

## 🎯 What's Built (MS-01 & MS-02 - 100% Complete)

### MS-01: Authentication & Identity ✅
- ✅ Email/password signup with email verification
- ✅ Multi-factor authentication (TOTP, SMS, Email OTP)
- ✅ JWT tokens with refresh mechanism
- ✅ Session management per device
- ✅ Rate limiting (5 attempts/15 min)
- ✅ Account lockout (after 5 failures)
- ✅ Password reset with OTP
- ✅ Google OAuth integration
- ✅ Logout all devices endpoint
- ✅ All 5 Kafka events publishing

### MS-02: User Management ✅
- ✅ Patient/Doctor/Pharmacist profile management
- ✅ Subscription plans (BASIC/PREMIUM/ENTERPRISE)
- ✅ Plan-based limits (200/1000/10000 patients)
- ✅ Bulk CSV import with validation
- ✅ Doctor search by specialty/language/location
- ✅ Clinic account management with team invites
- ✅ Subscription upgrade/downgrade with payment
- ✅ Stripe payment integration (configurable)
- ✅ Database-driven subscription plans
- ✅ All 6 Kafka events publishing

### MS-09: Audit & Compliance ⏳
- Coming soon: Immutable audit logs, GDPR data export, anomaly detection

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────┐
│          Frontend (React/Vue/Angular)           │
│         (http://localhost:3000)                 │
└────────────────────┬────────────────────────────┘
                     │ HTTP/HTTPS
                     ▼
┌─────────────────────────────────────────────────┐
│    API Gateway (Port 8080)                      │
│  - Request routing to services                  │
│  - Rate limiting (5 attempts/15min)             │
│  - CORS control                                 │
│  - JWT validation                               │
└────────┬──────────────────────────┬─────────────┘
         │                          │
         ▼                          ▼
┌──────────────────────┐   ┌───────────────────────┐
│ User Service         │   │ Discovery Service     │
│ (Port 8081)          │   │ (Port 8761 - Eureka)  │
│ - Auth & MFA         │   │ - Service Registry    │
│ - User Profiles      │   │ - Health Checks       │
│ - Subscriptions      │   │                       │
│ - Bulk Import        │   │                       │
└──────┬───────────────┘   └───────────────────────┘
       │
       ├─────► MongoDB (localhost:27017)
       │       - Collections: users, sessions, subscriptions, etc.
       │
       └─────► Kafka (localhost:9092)
               - Topics: user.created, user.login, subscription.upgraded, etc.
```


```

---

## ✅ Prerequisites

### Required Tools
- **Java 17** (JDK) - [Download](https://adoptium.net/)
  ```bash
  java -version
  # Output: openjdk version "17.x.x" or higher
  ```

- **Maven 3.8.1+** - [Download](https://maven.apache.org/download.cgi)
  ```bash
  mvn --version
  # Output: Apache Maven 3.8.1 or higher
  ```

- **MongoDB 5.0+** - [Download](https://www.mongodb.com/try/download/community)
  ```bash
  mongosh
  # Opens MongoDB shell
  ```

- **Kafka 3.5+** - [Download](https://kafka.apache.org/downloads)
  ```bash
  # For event streaming between services
  ```

- **Git 2.40+**
  ```bash
  git --version
  ```

### Optional but Recommended
- **IntelliJ IDEA** (IDE)
- **Postman** or **Insomnia** (API testing)
- **DBeaver** (MongoDB UI)

---

## 🔧 Backend Setup (All Services)

### Step 1: Clone Repository
```bash
git clone https://github.com/zakaria-beny/MedConnect.git
cd MedConnect-backend-microservices
```


### Step 2: Setup Environment Variables

Create `.env` file in project root:

```bash
# JWT Configuration
JWT_SECRET=medconnect-super-secret-key-that-is-at-least-32-chars-long
JWT_EXPIRATION=900000
JWT_REFRESH_EXPIRATION=604800000

# Database
MONGODB_URI=mongodb://localhost:27017/medconnect

# Email Service (Gmail App Password)
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-specific-password

# Twilio (Optional - SMS MFA)
TWILIO_ACCOUNT_SID=your_account_sid
TWILIO_AUTH_TOKEN=your_auth_token
TWILIO_PHONE_NUMBER=+1234567890

# Stripe (Optional - Payments)
STRIPE_SECRET_KEY=sk_test_xxxxx
STRIPE_PUBLISHABLE_KEY=pk_test_xxxxx

# Google OAuth
GOOGLE_CLIENT_ID=your_google_client_id

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Subscription Payment Provider
MEDCONNECT_SUBSCRIPTION_PAYMENT_PROVIDER=stripe

# API Gateway CORS
GATEWAY_CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:3001
```

**Get Gmail App Password**:
1. Go to [Google Account Security](https://myaccount.google.com/apppasswords)
2. Select "Mail" and "Windows Computer"
3. Copy the 16-character password → `.env` as `MAIL_PASSWORD`

### Step 5: Build All Services

```bash
# From root directory
mvn clean install

# Expected: BUILD SUCCESS
### Start Services (In This Order)
 1: Discovery Service (Eureka)**


 2: User Service**

# Should see: UserServiceApplication ... started on port 8081
#            Registered with Eureka
```

3: API Gateway**

# Should see: GatewayApplication ... started on port 8080
```

### Verify Services Are Running


# Check Eureka dashboard
 use browser:
# Eureka: http://localhost:8761
# Swagger API Docs: http://localhost:8080/swagger-ui.html
```

### Quick Test - Create Account

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

# Response: { "message": "Signup successful", "userId": "..." }
```

---

## 🧪 Testing

### Run All Tests
```bash
mvn test

# Expected: BUILD SUCCESS
# [INFO] Tests run: XX, Failures: 0, Errors: 0
```

### Run Specific Service Tests
```bash
cd user-service && mvn test
cd ../api-gateway && mvn test
cd ../discovery-service && mvn test
```

### Test Coverage Report
```bash
mvn clean test jacoco:report
# Open: target/site/jacoco/index.html
```

---

## 📚 API Endpoints - Quick Reference

### Authentication (MS-01)
| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/auth/signup` | Create account |
| POST | `/api/auth/verify-email` | Verify email with OTP |
| POST | `/api/auth/login` | Login (returns tokens) |
| POST | `/api/auth/mfa/setup` | Enable 2FA |
| POST | `/api/auth/mfa/verify` | Verify MFA code |
| POST | `/api/auth/logout` | Logout current session |
| POST | `/api/auth/logout-all-devices` | ⭐ NEW - Logout all sessions |
| POST | `/api/auth/refresh` | Refresh access token |
| GET | `/api/auth/sessions` | List active sessions |

### User Profiles (MS-02)
| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/users/patients` | Create patient profile |
| GET | `/api/users/patients/{userId}` | Get patient |
| POST | `/api/users/doctors` | Create doctor profile |
| GET | `/api/users/doctors/search` | Search doctors |
| POST | `/api/users/{userId}/subscription` | Get subscription |
| PUT | `/api/users/{userId}/subscription` | Upgrade/downgrade |
| POST | `/api/users/batch-import` | Bulk import users |
| GET | `/api/users/batch-import/{id}/status` | Check import progress |
| POST | `/api/users/{userId}/clinics` | Create clinic |

**Full API Reference**: See `FRONTEND_API_GUIDE.md` (30 endpoints with examples)

---

## 🎨 Frontend Integration

### What Frontend Developers Need to Know

**Backend URL**: 
- Development: `http://localhost:8080`

**CORS**: Currently allows `http://localhost:3000` and `http://localhost:3001`

### Step 1: Setup Frontend Project

```bash
npm create react-app medconnect-frontend
cd medconnect-frontend

# Install dependencies
npm install axios jwt-decode @stripe/react-stripe-js @stripe/js
```

### Step 2: Create API Client

```javascript
// src/api/client.js
import axios from 'axios';

const api = axios.create({
  baseURL: process.env.REACT_APP_API_URL || 'http://localhost:8080',
});

// Add token to requests
api.interceptors.request.use(config => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Auto-refresh on 401
api.interceptors.response.use(
  response => response,
  async error => {
    if (error.response?.status === 401) {
      const refreshToken = localStorage.getItem('refreshToken');
      if (refreshToken) {
        try {
          const { data } = await axios.post(
            `${process.env.REACT_APP_API_URL}/api/auth/refresh`,
            { refreshToken }
          );
          localStorage.setItem('accessToken', data.accessToken);
          localStorage.setItem('refreshToken', data.refreshToken);
          return api(error.config);
        } catch (err) {
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

### Step 3: Environment Configuration

```bash
# frontend/.env.local
REACT_APP_API_URL=http://localhost:8080
REACT_APP_GOOGLE_CLIENT_ID=your_google_client_id
REACT_APP_STRIPE_PUBLIC_KEY=pk_test_xxxxx
```

### Step 4: Implement Login

```javascript
// src/services/authService.js
import api from '../api/client';

export const authService = {
  signup: (email, password, firstName, lastName, userRole) =>
    api.post('/api/auth/signup', {
      email, password, firstName, lastName, userRole
    }),

  verifyEmail: (email, otp) =>
    api.post('/api/auth/verify-email', { email, otp }),

  login: (email, password) =>
    api.post('/api/auth/login', { email, password }),

  verifyMFA: (userId, code, sessionId) =>
    api.post('/api/auth/verify-login', { userId, code, sessionId }),

  setupMFA: (mfaType) =>
    api.post('/api/auth/mfa/setup', { mfaType }),

  logout: () =>
    api.post('/api/auth/logout'),

  refreshToken: (refreshToken) =>
    api.post('/api/auth/refresh', { refreshToken }),
};
```

### Step 5: Store Tokens Securely

```javascript
// After successful login/signup
localStorage.setItem('accessToken', response.data.accessToken);  // 15 min
localStorage.setItem('refreshToken', response.data.refreshToken);  // 7 days
localStorage.setItem('userId', response.data.user.id);
localStorage.setItem('userRole', response.data.user.userRole);
```

### Common Frontend Flows

**Signup → Email Verification → Login Flow**:
1. User enters email/password → call `/api/auth/signup`
2. Show "Check email for OTP" message
3. User enters OTP → call `/api/auth/verify-email`
4. Redirect to login page
5. User enters email/password → call `/api/auth/login`
6. If `mfaRequired: true`, show MFA input
7. User enters 2FA code → call `/api/auth/verify-login`
8. Save tokens → redirect to dashboard

**Create Patient Profile**:
```javascript
const response = await api.post('/api/users/patients', {
  userId: user.id,
  bloodType: 'O+',
  dateOfBirth: '1990-05-15',
  allergies: ['Penicillin']
});
```

**Search Doctors**:
```javascript
const doctors = await api.get('/api/users/doctors/search', {
  params: {
    specialty: 'Cardiologist',
    language: 'English',
    city: 'Paris'
  }
});
```

### Frontend Documentation

**See sections below for:**
1. **[API Endpoints](#api-endpoints)** - All 30 endpoints with examples
2. **[Common Frontend Flows](#common-frontend-flows)** - Login, profiles, search

---

## 🔐 Environment Variables

### Development (`.env` in root)

```bash
# JWT
JWT_SECRET=medconnect-super-secret-key-at-least-32-chars-long
JWT_EXPIRATION=900000  # 15 minutes
JWT_REFRESH_EXPIRATION=604800000  # 7 days

# Database
MONGODB_URI=mongodb://localhost:27017/medconnect

# Email (Gmail App Password)
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-16-char-app-password

# Twilio (SMS)
TWILIO_ACCOUNT_SID=ACxxxxxxxxxxxxx
TWILIO_AUTH_TOKEN=authxxxxxxxxxxxxx
TWILIO_PHONE_NUMBER=+1234567890

# Stripe (Payments)
STRIPE_SECRET_KEY=sk_test_xxxxx
STRIPE_PUBLISHABLE_KEY=pk_test_xxxxx

# Google
GOOGLE_CLIENT_ID=xxxxx.apps.googleusercontent.com

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# API Gateway
GATEWAY_CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:3001
```



## 🐛 Troubleshooting

| Issue | Solution |
|-------|----------|
| **Port already in use** | `lsof -i :8080` (macOS/Linux) or `netstat -ano \| findstr :8080` (Windows), then `kill <PID>` |
| **MongoDB connection failed** | Start MongoDB: `brew services start mongodb-community` (macOS) or `net start MongoDB` (Windows) |
| **Kafka not responding** | Ensure both Zookeeper and Kafka are running in separate terminals |
| **CORS error** | Add frontend URL to `GATEWAY_CORS_ALLOWED_ORIGINS` (default: `localhost:3000`) |
| **Email not sending** | Use Gmail App Password (not regular password). Enable 2FA on account first |
| **JWT token expired** | Call `/api/auth/refresh` with refresh token or login again |
| **Rate limit hit (429)** | Wait 15 minutes before retrying (5 attempts per 15 min limit) |
| **MFA code invalid** | Ensure device clock is synchronized; try Email OTP instead |
| **Services not discovering** | Ensure Discovery Service is running first and others can reach `localhost:8761` |

---

## 📁 Project Structure

```
MedConnect-backend-microservices/
├── user-service/                    # MS-01 & MS-02: Auth & User Mgmt
│   ├── src/main/java/.../
│   │   ├── controller/              # REST endpoints
│   │   ├── service/                 # Business logic
│   │   ├── entity/                  # MongoDB documents
│   │   ├── repository/              # Data access
│   │   ├── config/                  # Configuration
│   │   ├── security/                # JWT, filters
│   │   └── dto/                     # Data transfer objects
│   ├── src/main/resources/
│   │   └── application.properties
│   └── pom.xml
│
├── api-gateway/                     # MS-02: Request routing
│   ├── src/main/java/.../
│   │   ├── config/                  # Gateway config
│   │   ├── filter/                  # Request filters
│   │   └── security/                # Auth filters
│   ├── src/main/resources/
│   │   └── application.properties
│   └── pom.xml
│
├── discovery-service/               # MS-03: Eureka server
│   ├── src/main/java/.../
│   ├── src/main/resources/
│   │   └── application.properties
│   └── pom.xml
│
├── pom.xml                          # Parent POM
├── README.md                        # This file (everything in one place)
└── SERVICE-BREAKDOWN.md             # Feature breakdown
```

---

## 📞 Support & Resources

### Documentation
| Section | For Whom |
|---------|----------|
| [Prerequisites](#prerequisites) + [Backend Setup](#backend-setup) | Backend developers |
| [Frontend Integration](#frontend-integration) + [API Endpoints](#api-endpoints) | Oumaima (frontend dev) |
| [Environment Variables](#environment-variables) + [Troubleshooting](#troubleshooting) | DevOps engineers |

### Links
- **Eureka Dashboard**: http://localhost:8761
- **API Swagger**: http://localhost:8080/swagger-ui.html
- **GitHub**: https://github.com/zakaria-beny/MedConnect
- **support : hhhhhhhh makaynch

### Team
- **Backend Lead**: Zakaria Beny
- **Frontend Dev**: Oumaima 



---

**Last Updated**: 2026-05-06  
**Version**: 1.0.0  
