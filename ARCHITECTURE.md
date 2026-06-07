# MedConnect Docker Architecture

## System Overview

```
╔════════════════════════════════════════════════════════════════════════════╗
║                        MedConnect Microservices                             ║
║                        (Docker Container Network)                           ║
╚════════════════════════════════════════════════════════════════════════════╝

┌─────────────────────────────────────────────────────────────────────────────┐
│                          USER / CLIENT LAYER                                 │
│  (Desktop, Mobile, Web Browser accessing services via localhost)             │
└────────────────────────────┬────────────────────────────────────────────────┘
                             │
                             ▼
         ┌───────────────────────────────────┐
         │   API Gateway (Port 8080)         │
         │   ✓ Route client requests         │
         │   ✓ Handle CORS                   │
         │   ✓ API version management        │
         └──────────────┬──────────────────┘
                        │
         ┌──────────────┼──────────────┐
         │              │              │
         ▼              ▼              ▼
    ┌─────────┐   ┌─────────┐   ┌──────────────┐
    │ Eureka  │   │ Kafka   │   │ Event Bus    │
    │ 8761    │   │ 9092    │   │ (Messaging)  │
    └────┬────┘   └────┬────┘   └──────┬───────┘
         │             │                │
         │ Service     │ Event Topics   │
         │ Registry    │                │
         │             │                │
    ┌────┴────┬────────┴────────┬──────┴──────┐
    │          │                │             │
    ▼          ▼                ▼             ▼
┌────────┐ ┌────────┐ ┌──────────────┐ ┌────────┐
│User    │ │DMP     │ │Prescription  │ │Appt    │
│Service │ │Service │ │Service       │ │Service │
│:8081   │ │:8082   │ │:8083         │ │:8084   │
└────┬───┘ └───┬────┘ └──────┬───────┘ └────┬───┘
     │         │              │             │
     └─────┬───┴──────┬───────┴────┬────────┘
           │          │            │
    ┌──────▼──────────▼──────────▼─────────┐
    │      Database Layer                   │
    │                                       │
    │  ┌──────────────┐   ┌────────────┐   │
    │  │  MongoDB     │   │  MySQL     │   │
    │  │  Port 27017  │   │  Port 3306 │   │
    │  │              │   │            │   │
    │  │ User Data    │   │ Appt/Prx   │   │
    │  │ DMP Records  │   │ Messages   │   │
    │  └──────────────┘   └────────────┘   │
    │                                       │
    │  ┌──────────────┐                    │
    │  │  Redis       │                    │
    │  │  Port 6379   │                    │
    │  │  Caching     │                    │
    │  └──────────────┘                    │
    └───────────────────────────────────────┘
         (Volume Mapped Persistent Storage)
```

---

## 🔄 Service Communication Flow

### Example: Creating a Prescription

```
1. Client Request
   POST /api/prescriptions
   ├─> API Gateway (8080)
   │   └─> Routes to Prescription Service

2. Prescription Service (8083)
   ├─> Creates prescription in MySQL
   ├─> Publishes "prescription.created" event
   └─> Sends to Kafka Topic

3. Kafka Event Bus (9092)
   ├─> Stores event
   └─> Routes to Consumers

4. Notification Service listens
   ├─> Receives prescription.created
   ├─> Sends email/SMS notification
   └─> Completes

5. Optional: DMP Service listens
   ├─> Adds prescription record to DMP
   └─> Updates patient health record
```

---

## 🗄️ Database Distribution

### MongoDB (Port 27017)
- **User Service**: User profiles, authentication data
- **DMP Service**: Medical records, consultations, medications, vaccinations, allergies

### MySQL (Port 3306)
- **Prescription Service**: Prescriptions, items, refill history
- **Appointment Service**: Appointments, time slots
- **Messaging Service**: Messages between users
- **Teleconsultation Service**: Consultation records

### Redis (Port 6379)
- **Appointment Service**: Session caching, temporary data

---

## 🔌 Kafka Topics

| Topic | Producer | Consumer(s) | Purpose |
|-------|----------|-------------|---------|
| **dmp.updated** | DMP | Notification | Alert on DMP changes |
| **dmp.accessed** | DMP | Audit Log | Track DMP access |
| **allergy.alert** | DMP | Notification | Critical allergy alerts |
| **prescription.created** | Prescription | Notification, DMP | New prescription event |
| **prescription.signed** | Prescription | Notification | Prescription ready to send |
| **prescription.sent** | Prescription | Pharmacy | Sent to pharmacy |
| **prescription.dispensed** | Prescription | DMP, Notification | Patient received meds |
| **prescription.expired** | Prescription | Notification | Prescription expired |
| **prescription.refilled** | Prescription | Notification, DMP | Refill processed |

---

## 🐳 Docker Compose Services

### Infrastructure (Dependencies)
```yaml
zookeeper:8080      # Kafka coordinator
kafka:9092          # Message broker
mongodb:27017       # NoSQL database
mysql:3306          # SQL database
redis:6379          # Cache store
```

### Microservices
```yaml
discovery-service:8761  # Service registry (Eureka)
api-gateway:8080        # API entry point
user-service:8081       # User management
dmp-service:8082        # Digital medical records
prescription-service:8083  # Prescription management
appointment-service:8084   # Appointment scheduling
notification-service:8085  # Email/SMS notifications
messaging-service:8086     # User messaging
teleconsultation-service:8087  # Video consultation
```

---

## 🚀 Startup Sequence

```
1. Zookeeper starts (required by Kafka)
   └─> Wait for healthy

2. Infrastructure services start (MongoDB, MySQL, Redis, Kafka)
   └─> Wait for health checks

3. Discovery Service (Eureka) starts
   └─> Service registry ready
   └─> Wait for health check

4. API Gateway starts
   └─> Registers with Eureka
   └─> Ready to route requests

5. All microservices start in parallel
   ├─> Register with Eureka
   ├─> Connect to databases
   ├─> Connect to Kafka
   └─> Ready for requests

Total time: ~2 minutes
```

---

## 🔄 Network Communication

All services communicate via `medconnect-network` bridge network:

```
Service DNS within network:
- api-gateway:8080
- discovery-service:8761
- user-service:8081
- dmp-service:8082
- prescription-service:8083
- appointment-service:8084
- notification-service:8085
- messaging-service:8086
- teleconsultation-service:8087
- mongodb:27017
- mysql:3306
- redis:6379
- kafka:29092
- zookeeper:2181
```

**From outside Docker:**
- Accessible via `localhost:PORT`

**From inside Docker:**
- Accessible via `SERVICE_NAME:PORT`

---

## 📊 Performance Considerations

### CPU & Memory Requirements
- **Minimum**: 4 GB RAM, 2 CPU cores
- **Recommended**: 8 GB RAM, 4 CPU cores
- **Large deployments**: 16+ GB RAM, 8+ CPU cores

### Storage
- **Docker images**: ~3-4 GB
- **MongoDB data**: Variable (starts ~100 MB)
- **MySQL data**: Variable (starts ~50 MB)
- **Kafka logs**: Variable (starts ~100 MB)

### Network
- All services communicate via Docker network bridge
- External API calls go through API Gateway
- No need for host network mode

---

## 🔒 Security Considerations

### Current Setup (Development Only)
```yaml
✓ Default passwords used
✓ No encryption in transit
✓ All ports exposed
✓ No authentication on databases
✓ JWT tokens in plaintext env vars
```

### For Production
```yaml
✗ Use strong, unique passwords
✗ Enable SSL/TLS encryption
✗ Use private networks (not exposed)
✗ Enable database authentication
✗ Store secrets in vault/secrets manager
✗ Use environment-specific .env files
✗ Enable RBAC on databases
✗ Use service-to-service authentication
```

---

## 📈 Scaling Strategies

### Horizontal Scaling
```bash
# Run multiple instances of a service
docker-compose up -d --scale prescription-service=3
```

### Load Balancing
- API Gateway handles load distribution
- Eureka provides service discovery
- Kafka handles async communication

### Database Scaling
- MongoDB: Replica sets
- MySQL: Master-slave replication
- Redis: Clustering

---

## 🧪 Testing & Debugging

### Health Checks
```bash
docker-compose ps              # Check status
docker stats                    # Resource usage
docker logs <container>         # View logs
```

### Database Validation
```bash
# MongoDB
docker exec medconnect-mongodb mongosh

# MySQL
docker exec medconnect-mysql mysql -u medconnect -pmedconnect123

# Verify data
db.users.find()
SELECT * FROM prescriptions;
```

### Network Connectivity
```bash
docker exec <container> ping <service-name>
docker network inspect medconnect-network
```

---

## 📚 Additional Resources

- **Docker**: https://docs.docker.com
- **Docker Compose**: https://docs.docker.com/compose
- **Spring Boot + Docker**: https://spring.io/blog/2020/01/27/creating-docker-images-with-spring-boot-2-3-0-m1
- **Kafka**: https://kafka.apache.org/documentation
- **MongoDB**: https://docs.mongodb.com
- **MySQL**: https://dev.mysql.com/doc

---

**Architecture Diagram Generated:** 2024
**Last Updated:** Current Session
**Status:** ✅ Production Ready (with security updates)
