# 🚀 MedConnect Docker Quick Start

## For the Impatient 😅

```bash
# 1. Make sure Docker & Docker Compose are installed
# 2. Navigate to MedConnect project folder
cd MedConnect

# 3. Start everything!
docker-compose up --build

# Done! Services will start one by one...
```

That's it! 🎉

---

## 📍 Access Your Services

While services are starting, open these in your browser:

| Service | URL | Description |
|---------|-----|-------------|
| 🏥 **API Gateway** | http://localhost:8080 | Main entry point |
| 📊 **Eureka Dashboard** | http://localhost:8761 | Service registry |
| 👤 **User Service** | http://localhost:8081/health | Health check |
| 📋 **DMP Service** | http://localhost:8082/health | Health check |
| 💊 **Prescription Service** | http://localhost:8083/health | Health check |
| 📅 **Appointment Service** | http://localhost:8084/health | Health check |

---

## ⏳ How Long Does It Take?

- **First run (with build)**: 10-15 minutes (building images)
- **Subsequent runs**: 30 seconds - 2 minutes
- All services will show **healthy** once ready

---

## 🛑 Stop Everything

```bash
# Stop services (keep data)
docker-compose down

# Stop and delete databases (clean slate)
docker-compose down -v
```

---

## 🔍 Check What's Running

```bash
# See all containers
docker-compose ps

# View logs from all services
docker-compose logs -f

# View logs from specific service
docker-compose logs -f dmp-service
```

---

## 💾 Access Databases

### MongoDB
```bash
docker exec -it medconnect-mongodb mongosh
```

### MySQL
```bash
docker exec -it medconnect-mysql mysql -u medconnect -pmedconnect123 medconnect
```

### Redis
```bash
docker exec -it medconnect-redis redis-cli
```

---

## 🤖 Use the Management Script

### On Linux/Mac
```bash
chmod +x docker-manager.sh
./docker-manager.sh
```

### On Windows
```bash
docker-manager.bat
```

This gives you a menu to:
- Start/Stop services
- View logs
- Check health
- Manage databases
- Clean up

---

## ⚠️ Common Issues

### "Port already in use"
```bash
# Change port in docker-compose.yml
# From: "8080:8080"
# To:   "8090:8080"  (use 8090 instead)
```

### "Services won't start"
```bash
# Check Docker is running
docker ps

# Rebuild everything
docker-compose down -v
docker-compose up --build
```

### "Connection refused to database"
```bash
# Wait a bit more (databases take time to initialize)
# Watch logs: docker-compose logs -f

# Or restart just the service
docker-compose restart dmp-service
```

---

## 📚 Full Documentation

For detailed setup, troubleshooting, and configuration:

📖 See **DOCKER_SETUP.md**

---

## 🎯 What's Next?

1. ✅ Services are running
2. ✅ Check Eureka Dashboard (http://localhost:8761)
3. ✅ Test API endpoints
4. ✅ Access databases if needed
5. ✅ Read full DOCKER_SETUP.md for advanced configuration

---

## 💡 Pro Tips

```bash
# Restart a service without rebuilding
docker-compose restart prescription-service

# View resource usage
docker stats

# Clean up old images/containers
docker system prune

# Run a specific command in a container
docker exec medconnect-dmp java -version
```

---

**Need help?** Check DOCKER_SETUP.md or run `docker-compose logs -f` to see what's happening!

**Happy coding! 🚀**
