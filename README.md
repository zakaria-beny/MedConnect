# 🏥 MedConnect - Healthcare Backend Microservices

**Complete Spring Boot microservices platform for healthcare management**

A production-ready microservices architecture with authentication, user management, subscriptions, and event streaming.

**Status**: ✅ Production Ready | **Version**: 1.0.0 | **Java**: 17+ | **Spring Boot**: 3.2.3

## 📋 Quick Navigation

### For Backend Developers
- [Prerequisites](#prerequisites) - What you need installed
- [Backend Setup](#backend-setup-all-services) - Complete setup guide
- [Run Locally](#run-locally) - Start all 3 services
- [Testing](#testing) - Run tests

### For Frontend Developers
- [Frontend Integration](#frontend-integration) - How to connect frontend
- [API Reference](#api-endpoints---quick-reference) - All 30 endpoints
- [Examples](#examples) - Code snippets

### For DevOps/Deployment
- [Environment Variables](#environment-variables) - Production config
- [Docker Deployment](#docker-deployment) - Containerization
- [Database Setup](#database-setup) - MongoDB initialization

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
- **Docker** (containerization)
- **DBeaver** (MongoDB UI)

---

## 🔧 Backend Setup (All Services)

### Step 1: Clone Repository
```bash
git clone https://github.com/zakaria-beny/MedConnect.git
cd MedConnect-backend-microservices
```

### Step 2: Install MongoDB

**Windows (Chocolatey)**:
```bash
choco install mongodb-community
# Start service
net start MongoDB
```

**macOS (Homebrew)**:
```bash
brew tap mongodb/brew
brew install mongodb-community
brew services start mongodb-community
```

**Linux (Ubuntu)**:
```bash
sudo apt-get install -y mongodb
sudo systemctl start mongodb
```

**Verify**:
```bash
mongosh
# Should open MongoDB shell - type: exit
```

### Step 3: Install Kafka

**Download** from [kafka.apache.org](https://kafka.apache.org/downloads) and extract.

**Windows**:
```bash
cd C:\kafka

# Terminal 1: Start Zookeeper
.\bin\windows\zookeeper-server-start.bat .\config\zookeeper.properties

# Wait 5 seconds, then Terminal 2: Start Kafka
.\bin\windows\kafka-server-start.bat .\config\server.properties

# Terminal 3: Verify (create topics)
.\bin\windows\kafka-topics.bat --create --topic test --bootstrap-server localhost:9092
.\bin\windows\kafka-topics.bat --list --bootstrap-server localhost:9092
```

**macOS/Linux**:
```bash
cd ~/kafka

# Terminal 1
./bin/zookeeper-server-start.sh ./config/zookeeper.properties

# Terminal 2
./bin/kafka-server-start.sh ./config/server.properties

# Terminal 3
./bin/kafka-topics.sh --create --topic test --bootstrap-server localhost:9092
./bin/kafka-topics.sh --list --bootstrap-server localhost:9092
```

### Step 4: Setup Environment Variables

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
```

### Step 6: Database Initialization

```bash
mongosh

use medconnect

# Create collections
db.createCollection("users")
db.createCollection("sessions")
db.createCollection("mfa_settings")
db.createCollection("subscription")
db.createCollection("subscription_plans")
db.createCollection("bulk_imports")
db.createCollection("clinic_accounts")
db.createCollection("patient_profiles")
db.createCollection("doctor_profiles")
db.createCollection("pharmacist_profiles")
db.createCollection("refresh_tokens")
db.createCollection("login_attempts")

# Create indexes
db.users.createIndex({ "email": 1 }, { unique: true })
db.users.createIndex({ "userRole": 1 })
db.sessions.createIndex({ "userId": 1 })
db.doctor_profiles.createIndex({ "specialty": 1 })
db.doctor_profiles.createIndex({ "city": 1 })

# Exit
exit
```

### Step 7: Create Kafka Topics

```bash
cd ~/kafka  # or C:\kafka

# Create topics
./bin/kafka-topics.sh --create --topic user.created --bootstrap-server localhost:9092
./bin/kafka-topics.sh --create --topic user.login --bootstrap-server localhost:9092
./bin/kafka-topics.sh --create --topic subscription.upgraded --bootstrap-server localhost:9092
./bin/kafka-topics.sh --create --topic auth.failed --bootstrap-server localhost:9092
# ... (see list below)
```


---

## 🚀 Run Locally

### Start Services (In This Order)

**Terminal 1: Discovery Service (Eureka)**
```bash
cd discovery-service
mvn spring-boot:run
# Wait for: DiscoveryServiceApplication ... started
# Then visit: http://localhost:8761
```

**Terminal 2: User Service**
```bash
cd user-service
mvn spring-boot:run
# Should see: UserServiceApplication ... started on port 8081
#            Registered with Eureka
```

**Terminal 3: API Gateway**
```bash
cd api-gateway
mvn spring-boot:run
# Should see: GatewayApplication ... started on port 8080
```

### Verify Services Are Running

```bash
# Check Eureka dashboard
curl http://localhost:8761

# Check Gateway health
curl http://localhost:8080/actuator/health

# Check User Service
curl http://localhost:8081/actuator/health

# Or use browser:
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
- Production: `https://api.medconnect.fr` (TBD)

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

**See these files for complete integration guides:**
1. **`FRONTEND_API_GUIDE.md`** - All 30 endpoints with examples
2. **`FRONTEND_QUICK_START.md`** - Setup guide & code snippets

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

### Production (Set as system environment variables)

```bash
# Use strong, random values
export JWT_SECRET=$(openssl rand -base64 32)
export MONGODB_URI=mongodb+srv://user:pass@cluster.mongodb.net/medconnect
export STRIPE_SECRET_KEY=sk_live_xxxxx
export MAIL_PASSWORD=app-specific-password
export KAFKA_BOOTSTRAP_SERVERS=kafka-prod.medconnect.fr:9092
export GATEWAY_CORS_ALLOWED_ORIGINS=https://app.medconnect.fr
```

---

## 📦 Docker Deployment

### Build Docker Image

```dockerfile
# Dockerfile (for user-service)
FROM openjdk:17-slim
WORKDIR /app
COPY target/user-service-1.0.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```bash
# Build
cd user-service
mvn clean package -DskipTests
docker build -t medconnect-user-service:1.0 .

# Run
docker run -p 8081:8081 \
  -e MONGODB_URI=mongodb://mongo:27017/medconnect \
  -e JWT_SECRET=xxxx \
  -e MAIL_USERNAME=xxx \
  -e MAIL_PASSWORD=xxx \
  medconnect-user-service:1.0
```

### Docker Compose (All Services)

```yaml
# docker-compose.yml
version: '3.8'

services:
  mongodb:
    image: mongo:5.0
    ports:
      - "27017:27017"
    environment:
      MONGO_INITDB_DATABASE: medconnect

  kafka:
    image: confluentinc/cp-kafka:7.5.0
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181

  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    ports:
      - "2181:2181"

  discovery-service:
    build:
      context: ./discovery-service
    ports:
      - "8761:8761"

  user-service:
    build:
      context: ./user-service
    ports:
      - "8081:8081"
    environment:
      MONGODB_URI: mongodb://mongodb:27017/medconnect
      KAFKA_BOOTSTRAP_SERVERS: kafka:9092
      JWT_SECRET: ${JWT_SECRET}
      EUREKA_SERVER_URL: http://discovery-service:8761/eureka/
    depends_on:
      - mongodb
      - kafka
      - discovery-service

  api-gateway:
    build:
      context: ./api-gateway
    ports:
      - "8080:8080"
    environment:
      EUREKA_SERVER_URL: http://discovery-service:8761/eureka/
    depends_on:
      - discovery-service
```

```bash
# Run all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all
docker-compose down
```

---

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
├── README.md                        # This file
├── FRONTEND_API_GUIDE.md            # 30 endpoints (frontend)
├── FRONTEND_QUICK_START.md          # Setup guide (frontend)
└── SERVICE-BREAKDOWN.md             # Feature breakdown
```

---

## 📞 Support & Resources

### Documentation Files
| File | Purpose |
|------|---------|
| `README.md` | This file - Setup for all developers |
| `FRONTEND_API_GUIDE.md` | 30 endpoints with examples (frontend) |
| `FRONTEND_QUICK_START.md` | Frontend setup guide (frontend) |
| `SERVICE-BREAKDOWN.md` | Feature breakdown & requirements |

### Links
- **Eureka Dashboard**: http://localhost:8761
- **API Swagger**: http://localhost:8080/swagger-ui.html
- **GitHub**: https://github.com/zakaria-beny/MedConnect
- **Email Support**: api-support@medconnect.fr

### Key Team Members
- **Backend**: Zakaria Beny
- **DevOps**: TBD
- **Frontend**: TBD

---

## ✨ What's Next?

- ✅ MS-01 & MS-02: 100% Complete
- ⏳ MS-09: Audit & Compliance (coming soon)
- 🎨 Frontend integration (see `FRONTEND_API_GUIDE.md`)
- 🚀 Production deployment

---

**Last Updated**: 2026-05-06  
**Version**: 1.0.0  
**Status**: ✅ Production Ready
