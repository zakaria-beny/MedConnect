# MedConnect Docker Setup Guide

## 🐳 Quick Start

### Prerequisites
- Docker (v20.10+)
- Docker Compose (v2.10+)
- 8GB+ RAM available
- 10GB+ disk space

### Start All Services
```bash
# Build all Docker images and start services
docker-compose up --build

# Or run in background
docker-compose up -d --build
```

### Stop All Services
```bash
docker-compose down

# Remove volumes (clean database)
docker-compose down -v
```

---

## 📋 Services Overview

### Infrastructure Services
| Service | Port | Username | Password |
|---------|------|----------|----------|
| **MongoDB** | 27017 | - | - |
| **MySQL** | 3306 | medconnect | medconnect123 |
| **Redis** | 6379 | - | - |
| **Kafka** | 9092 | - | - |
| **Zookeeper** | 2181 | - | - |

### MedConnect Microservices
| Service | Port | Database | Status |
|---------|------|----------|--------|
| **API Gateway** | 8080 | - | ✅ |
| **Discovery Service (Eureka)** | 8761 | - | ✅ |
| **User Service** | 8081 | MongoDB | ✅ |
| **DMP Service** | 8082 | MongoDB | ✅ |
| **Prescription Service** | 8083 | MySQL | ✅ |
| **Appointment Service** | 8084 | MySQL, Redis | ✅ |
| **Notification Service** | 8085 | Kafka | ✅ |
| **Messaging Service** | 8086 | MySQL, Kafka | ✅ |
| **Teleconsultation Service** | 8087 | MySQL, Kafka | ✅ |

---

## 🚀 Usage Examples

### View Running Services
```bash
docker-compose ps
```

### View Service Logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f dmp-service

# Last 100 lines
docker-compose logs --tail=100 api-gateway
```

### Access Services
```bash
# API Gateway
curl http://localhost:8080/

# Discovery Service (Eureka UI)
http://localhost:8761/

# User Service Health
curl http://localhost:8081/health

# DMP Service
curl http://localhost:8082/health

# Prescription Service
curl http://localhost:8083/health
```

---

## 🔧 Configuration

### Environment Variables
Edit `.env` file or pass via docker-compose:

```yaml
environment:
  JWT_SECRET: your-secret-key
  MAIL_USERNAME: your-email@gmail.com
  MAIL_PASSWORD: your-app-password
```

### Database Access

**MongoDB:**
```bash
docker exec -it medconnect-mongodb mongosh
```

**MySQL:**
```bash
docker exec -it medconnect-mysql mysql -u medconnect -pmedconnect123 medconnect
```

**Redis:**
```bash
docker exec -it medconnect-redis redis-cli
```

---

## 🔍 Debugging

### Check Service Health
```bash
docker-compose ps                    # See container status
docker logs medconnect-api-gateway   # View specific service logs
docker stats                         # Monitor resource usage
```

### Restart Specific Service
```bash
docker-compose restart dmp-service
```

### Force Rebuild
```bash
docker-compose up --build --force-recreate
```

---

## 📊 Network Architecture

```
┌─────────────────────────────────────────────────┐
│           Docker Network: medconnect-network    │
├─────────────────────────────────────────────────┤
│                                                 │
│  ┌────────────────────────────────────────┐   │
│  │      API Gateway (Port 8080)           │   │
│  └────────────────────────────────────────┘   │
│           ↓ Routes requests ↓                   │
│  ┌────────────────────────────────────────┐   │
│  │   Discovery Service (Eureka 8761)      │   │
│  │   Service Registry & Load Balancing    │   │
│  └────────────────────────────────────────┘   │
│           ↓ Service Discovery ↓                │
│  ┌──────────────────────────────────────────┐  │
│  │         Microservices (8081-8087)        │  │
│  │ ┌────┐ ┌────┐ ┌────┐ ┌────┐ ┌────┐     │  │
│  │ │User│ │DMP │ │Prx │ │App │ │Notif    │  │
│  │ └────┘ └────┘ └────┘ └────┘ └────┘     │  │
│  └──────────────────────────────────────────┘  │
│           ↓ Share Infrastructure ↓             │
│  ┌──────────────────────────────────────────┐  │
│  │      Infrastructure Services              │  │
│  │ ┌────────────┐ ┌────────────┐            │  │
│  │ │  MongoDB   │ │   MySQL    │            │  │
│  │ └────────────┘ └────────────┘            │  │
│  │ ┌────────────┐ ┌────────────┐            │  │
│  │ │   Redis    │ │   Kafka    │            │  │
│  │ └────────────┘ └────────────┘            │  │
│  └──────────────────────────────────────────┘  │
│                                                 │
└─────────────────────────────────────────────────┘
```

---

## 📚 Service Communication via Kafka

Services communicate through Kafka topics:

| Topic | Producer | Consumer |
|-------|----------|----------|
| **dmp.updated** | DMP Service | Notification Service |
| **dmp.accessed** | DMP Service | Access Logging |
| **allergy.alert** | DMP Service | Notification Service |
| **prescription.created** | Prescription Service | Notification Service |
| **prescription.signed** | Prescription Service | Notification Service |
| **prescription.sent** | Prescription Service | Pharmacy Systems |
| **prescription.dispensed** | Prescription Service | DMP Service |
| **prescription.expired** | Prescription Service | Notification Service |
| **prescription.refilled** | Prescription Service | Notification Service |

---

## 🛠️ Troubleshooting

### Services Not Starting
```bash
# Check Docker daemon
docker info

# Rebuild everything
docker-compose down -v
docker-compose up --build

# Check logs
docker-compose logs --tail=50
```

### Database Connection Issues
```bash
# Test MongoDB
docker exec medconnect-mongodb mongosh --eval "db.runCommand({ping: 1})"

# Test MySQL
docker exec medconnect-mysql mysqladmin ping -u medconnect -pmedconnect123

# Test Redis
docker exec medconnect-redis redis-cli ping

# Test Kafka
docker exec medconnect-kafka kafka-broker-api-versions --bootstrap-server kafka:9092
```

### Port Already in Use
```bash
# Change port in docker-compose.yml
# For example, change "8080:8080" to "8090:8080"

# Or kill the process using the port
# Linux/Mac: lsof -i :8080 | kill -9
# Windows: netstat -ano | findstr :8080
```

---

## 🔐 Security Notes

⚠️ **These are default credentials for local development only!**

For production:
- Use strong JWT secrets
- Update MySQL credentials
- Enable authentication for Redis/Kafka
- Use environment-specific .env files
- Configure SSL/TLS

---

## 📦 Build Images Separately

```bash
# Build all images
docker-compose build

# Build specific service
docker-compose build api-gateway

# Build with no cache
docker-compose build --no-cache
```

---

## 🧹 Cleanup

```bash
# Remove stopped containers
docker container prune

# Remove unused images
docker image prune

# Remove everything (careful!)
docker system prune -a

# Remove volumes too
docker system prune -a --volumes
```

---

## 📞 Support

For issues:
1. Check logs: `docker-compose logs -f service-name`
2. Verify health endpoints: `curl http://localhost:SERVICE_PORT/health`
3. Check network connectivity: `docker network inspect medconnect-network`
4. Review docker-compose.yml for environment variables

---

**Happy Dockering! 🚀**
