# MedConnect - Backend Microservices

A Spring Boot microservices architecture for **MedConnect**, a comprehensive healthcare management platform built with Java 17, Spring Cloud, and MongoDB.

## 📋 Table of Contents

- [Project Overview](#project-overview)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Installation & Setup](#installation--setup)
- [Environment Configuration](#environment-configuration)
- [Running Services](#running-services)
- [API Documentation](#api-documentation)
- [Authentication & JWT](#authentication--jwt)
- [Testing with Postman](#testing-with-postman)
- [Project Structure](#project-structure)
- [Common Issues & Troubleshooting](#common-issues--troubleshooting)
- [Contributing](#contributing)
- [Future Services](#future-services)

---

## 🏥 Project Overview

MedConnect is a microservices-based healthcare backend platform providing:
- **User Management** with JWT authentication
- **API Gateway** for service routing
- **Service Discovery** using Eureka
- **OAuth2** integration (Google sign-in)
- **Email Notifications** with OTP verification
- **Swagger/OpenAPI** documentation

### Tech Stack
- **Java 17** - Programming language
- **Spring Boot 3.2.3** - Framework
- **Spring Cloud 2023.0.0** - Microservices tools
- **MongoDB** - NoSQL database
- **Eureka** - Service discovery
- **JWT (JJWT)** - Token-based authentication
- **Swagger/Springdoc OpenAPI** - API documentation

---

## 🏗️ Architecture

```
MedConnect-backend-microservices (Parent POM)
│
├── user-service (Port 8081)
│   ├── Authentication & Authorization (JWT)
│   ├── User Management
│   ├── Google OAuth Integration
│   ├── OTP Email Verification
│   └── Admin Management
│
├── api-gateway (Port 8080)
│   ├── Request Routing
│   ├── Load Balancing
│   ├── Authentication Filter
│   └── Cross-cutting Concerns
│
└── discovery-service (Port 8761)
    └── Eureka Server (Service Registry)


```

---

## ✅ Prerequisites

### Required
- **Java 17** (Eclipse Temurin, Oracle, or OpenJDK)
- **Maven 3.8.1+** (included: `./mvnw`)
- **MongoDB 5.0+** (local or cloud)
- **Git**

### Optional (for testing)
- **Postman** or Insomnia (API testing)
- **IntelliJ IDEA** (IDE - recommended)
- **Docker** (for containerization)

### Check Prerequisites
```bash
java -version          # Should show Java 17.x.x
mvn --version         # Or: ./mvnw --version
mongod --version      # MongoDB version
```

---

## 📦 Installation & Setup

### 1. Clone the Repository
```bash
git clone https://github.com/yourusername/MedConnect-backend-microservices.git
cd MedConnect-backend-microservices
```

### 2. Set Java Version (IntelliJ)
1. **File → Project Structure** (`Ctrl+Alt+Shift+S`)
2. **SDK** → Select Java 17 (Download if needed)
3. Click **Apply → OK**

### 3. Configure Build Tools
1. **Build, Execution, Deployment → Build Tools → Maven**
2. **JDK for importer**: Select Java 17
3. Click **OK**

### 4. Install Dependencies
```bash
./mvnw clean install -DskipTests
```

---

## 🔐 Environment Configuration

### Create `.env` File
Create a `.env` file in the **project root** directory:

**⚠️ IMPORTANT: `.env` is in `.gitignore` and should NEVER be committed to version control!**

```env
# JWT Configuration
JWT_SECRET=medconnect-super-secret-key-32-chars-min-2026!!

# Admin Account
ADMIN_EMAIL=admin@medconnect.com
ADMIN_PASSWORD=Admin12345

# Google OAuth
GOOGLE_CLIENT_IDS=980492524757-g1h19t4a2n30ut7vq6mi4uv6ooijt7sg.apps.googleusercontent.com

# Email SMTP (Gmail with App Password)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# MongoDB
MONGO_URI=mongodb://localhost:27017/medconnect_users_db

# Eureka Server
EUREKA_SERVER_URL=http://localhost:8761/eureka/

# OTP Expiry
OTP_EXPIRY_MINUTES=10
```

**Setup**: Copy the above template into your local `.env` file and update with your actual credentials. Git will ignore this file automatically.

### ⚠️ Important: Set Real Credentials

#### Gmail Setup (for OTP emails):
1. Enable **2-Step Verification** on your Gmail account
2. Create an **App Password**:
   - Go to [Google Account Security](https://myaccount.google.com/security)
   - App passwords → Select "Mail" and "Windows Computer"
   - Copy the 16-character password
   - Paste in `.env` as `MAIL_PASSWORD`

#### Google OAuth Setup:
1. Create a project on [Google Cloud Console](https://console.cloud.google.com/)
2. Create OAuth 2.0 credentials
3. Copy Client ID to `.env` as `GOOGLE_CLIENT_IDS`


---

## 🚀 Running Services

###  Run All Services (IntelliJ)
1. **Build → Rebuild Project**
2. Right-click each service → **Run**
   - discovery-service (waits for port 8761 to be free)
   - user-service (depends on MongoDB & Discovery)
   - api-gateway (routes requests)



---

## 📚 API Documentation

### Swagger UI
Access interactive API documentation:

**User Service**: http://localhost:8081/swagger-ui.html
**API Gateway**: http://localhost:8080/swagger-ui.html

### OpenAPI JSON
```
http://localhost:8081/api-docs
http://localhost:8080/api-docs
```

---

## 🔑 Authentication & JWT

### Authentication Flow

#### 1. **Signup (with Email Verification)**
```
POST /api/auth/signup
Body: { "nom", "prenom", "email", "telephone", "password" }
      ↓
→ OTP sent to email
```

#### 2. **Verify Email**
```
POST /api/auth/verify-email
Body: { "email", "code" } (OTP from email)
      ↓
→ JWT token returned (user enabled)
```

#### 3. **Login (with 2FA)**
```
POST /api/auth/signin
Body: { "email", "password" }
      ↓
→ OTP sent to email
```

#### 4. **Verify Login OTP**
```
POST /api/auth/verify-login
Body: { "email", "code" } (OTP from email)
      ↓
→ JWT token returned
```

#### 5. **Use JWT Token**
```
GET /api/users/me
Header: Authorization: Bearer YOUR_JWT_TOKEN
      ↓
→ User profile returned
```

### Protected Endpoints
All `/api/users/**` endpoints require valid JWT token in `Authorization` header:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

## 🧪 Testing with Postman

### Import Collection
1. Open Postman
2. **File → Import**
3. Create requests for each endpoint (examples below)

### 1. Signup
```
POST http://localhost:8081/api/auth/signup
Content-Type: application/json

{
  "nom": "Doe",
  "prenom": "John",
  "email": "john@example.com",
  "telephone": "0612345678",
  "password": "SecurePass123!"
}
```

**Response**: User created, OTP sent to email

### 2. Verify Email
```
POST http://localhost:8081/api/auth/verify-email
Content-Type: application/json

{
  "email": "john@example.com",
  "code": "123456"  // OTP from email
}
```

**Response**:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "tokenType": "Bearer",
  "userId": "64c1a2b3c4d5e6f7g8h9",
  "email": "john@example.com",
  "roles": ["ROLE_USER"]
}
```

### 3. Login with 2FA
```
POST http://localhost:8081/api/auth/signin
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "SecurePass123!"
}
```

**Response**: OTP sent to email

### 4. Verify Login OTP
```
POST http://localhost:8081/api/auth/verify-login
Content-Type: application/json

{
  "email": "john@example.com",
  "code": "123456"
}
```

**Response**: JWT token returned

### 5. Get User Profile (Protected)
```
GET http://localhost:8081/api/users/me
Authorization: Bearer YOUR_JWT_TOKEN
```

**Response**:
```json
{
  "id": "64c1a2b3c4d5e6f7g8h9",
  "email": "john@example.com",
  "nom": "Doe",
  "prenom": "John",
  "telephone": "0612345678"
}
```

### 6. Update Profile (Protected)
```
PUT http://localhost:8081/api/users/me
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json

{
  "nom": "Smith",
  "prenom": "John",
  "telephone": "0687654321"
}
```

### 7. Google OAuth Login
```
POST http://localhost:8081/api/auth/google
Content-Type: application/json

{
  "idToken": "GOOGLE_ID_TOKEN_FROM_FRONTEND"
}
```

**Response**: JWT token + user data

### 8. Password Reset
```
POST http://localhost:8081/api/auth/forgot-password
{
  "email": "john@example.com"
}
→ OTP sent to email

POST http://localhost:8081/api/auth/reset-password
{
  "email": "john@example.com",
  "code": "123456",
  "newPassword": "NewPassword123!"
}
```

---

## 📁 Project Structure

```
MedConnect-backend-microservices/
├── pom.xml                          # Parent POM (dependency management)
├── .env                             # Environment variables (IGNORE IN GIT)
├── README.md                        # This file
│
├── user-service/
│   ├── pom.xml
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/rihla/userservice/
│   │   │   │   ├── config/           # Configuration classes
│   │   │   │   ├── controller/       # REST endpoints
│   │   │   │   ├── service/          # Business logic
│   │   │   │   ├── entity/           # MongoDB documents
│   │   │   │   ├── repository/       # Data access
│   │   │   │   ├── security/         # JWT, OAuth, filters
│   │   │   │   ├── googleAuth/       # Google OAuth logic
│   │   │   │   ├── otp/              # OTP service & email
│   │   │   │   ├── dto/              # Data transfer objects
│   │   │   │   └── mapper/           # MapStruct mappers
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── test/
│   │       └── java/...             # Unit tests
│   └── target/                      # Build output
│
├── api-gateway/
│   ├── pom.xml
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/rihla/apigateway/
│   │   │   │   ├── config/
│   │   │   │   ├── filter/          # Gateway filters
│   │   │   │   └── security/
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── test/
│   └── target/
│
├── discovery-service/
│   ├── pom.xml
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/rihla/discoveryservice/
│   │   │   │   └── DiscoveryServiceApplication.java
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── test/
│   └── target/
│
├── .mvn/                            # Maven wrapper
├── mvnw                             # Maven wrapper script (Linux/Mac)
└── mvnw.cmd                         # Maven wrapper script (Windows)
```

---

## 🐛 Common Issues & Troubleshooting

### Issue 1: Port Already in Use
```
ERROR: Address already in use: bind
```

**Solution**:
```bash
# Find process using port 8081
netstat -ano | findstr :8081        # Windows
lsof -i :8081                       # Mac/Linux

# Kill process
taskkill /PID <PID> /F             # Windows
kill -9 <PID>                       # Mac/Linux

# Or change port in application.properties:
server.port=8082
```

### Issue 2: MongoDB Connection Failed
```
ERROR: Failed to connect to database
```

**Solution**:
```bash
# Check MongoDB is running
mongosh                             # Or: mongo

# If not installed, download from: https://www.mongodb.com/try/download/community

# Update .env with correct MONGO_URI
MONGO_URI=mongodb://localhost:27017/rihla_users_db
```

### Issue 3: OTP Not Sending (Email)
```
ERROR: Failed to send email
```

**Solution**:
1. Verify Gmail credentials in `.env`
2. Use **Gmail App Password**, not regular password
3. Enable "Less secure apps" if using old Gmail:
   - Go to [Account Security](https://myaccount.google.com/apppasswords)
4. Check firewall blocks port 587

### Issue 4: Java Version Mismatch
```
ERROR: java.lang.ExceptionInInitializerError
```

**Solution**:
1. Check Java version: `java -version` (should be 17)
2. In IntelliJ: **File → Project Structure → SDK** → Select Java 17
3. Restart IDE and rebuild

### Issue 5: Eureka Server Not Accessible
```
ERROR: Cannot register with Eureka
```

**Solution**:
1. Start discovery-service first
2. Check it's running: http://localhost:8761
3. In `.env`: `EUREKA_SERVER_URL=http://localhost:8761/eureka/`
4. If running on different machine: Update hostname

### Issue 6: JWT Token Validation Failed
```
ERROR: Invalid or expired token
```

**Solution**:
1. Ensure `JWT_SECRET` in `.env` matches all services
2. Token might be expired (default 24 hours)
3. Check header format: `Authorization: Bearer <token>` (space required)

---

## 🤝 Contributing

### Commit Message Format
```
diro li ban likomm 
this is just example:
feat: Add email verification OTP
fix: Resolve MongoDB connection timeout
docs: Update README with API examples
refactor: Simplify JWT token generation
test: Add user authentication tests
```

### Pull Request Process
1. Create feature branch from `main`
2. Make changes and commit with clear messages
3. Push to remote: `git push origin feature/xyz`
4. Create Pull Request with description
5. Request review from team
6. Merge after approval

---

## 📝 Environment Variables Reference

| Variable | Description | Example |
|----------|-------------|---------|
| `JWT_SECRET` | Secret key for JWT signing | `your-secret-key-32-chars` |
| `ADMIN_EMAIL` | Default admin email | `admin@medconnect.com` |
| `ADMIN_PASSWORD` | Default admin password | `Admin12345` |
| `GOOGLE_CLIENT_IDS` | Google OAuth client ID | `xxx.apps.googleusercontent.com` |
| `MAIL_HOST` | SMTP server | `smtp.gmail.com` |
| `MAIL_PORT` | SMTP port | `587` |
| `MAIL_USERNAME` | Email address for sending | `your-email@gmail.com` |
| `MAIL_PASSWORD` | Email app password | `16-char-app-password` |
| `MONGO_URI` | MongoDB connection string | `mongodb://localhost:27017/medconnect_db` |
| `EUREKA_SERVER_URL` | Eureka service registry URL | `http://localhost:8761/eureka/` |
| `OTP_EXPIRY_MINUTES` | OTP validity duration | `10` |

---

## 📞 Support

For issues or questions:
1. Check Group wtsp
2. Review API documentation: http://localhost:8081/swagger-ui.html
3. Check logs in IDE console

---

## 📄 License

This project is licensed under the MIT License - see LICENSE file for details.

---

## 👥 Authors

- **MedConnect Team**
- ALL of us

**Last Updated**: April 2026
**Version**: 1.0.0
**Status**: Active Development
