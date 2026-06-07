# Run backend locally

Use Docker only for Kafka/Zookeeper:

```powershell
cd C:\Users\hp\IdeaProjects\MedConnect
docker compose -f docker-compose.kafka.yml up -d
```

Run these outside Docker on your PC:

- Java 17
- Maven
- MongoDB on `localhost:27017`

No Redis is required for the current backend run.

Start services from IntelliJ in this order:

1. `discovery-service` on `8761`
2. `user-service` on `8081`
3. `messaging-service` on `8082`
4. `dmp-service` on `8083`
5. `prescription-service` on `8084`
6. `appointment-service` on `8085`
7. `teleconsultation-service` on `8086`
8. `audit-service` on `8087`
9. `notification-service` on `8088`
10. `api-gateway` on `8080`

Useful URLs:

- Eureka dashboard: `http://localhost:8761`
- API gateway: `http://localhost:8080`

Local Kafka is available at:

```text
localhost:9092
```

Local MongoDB databases used by the services:

```text
Medconnect_users_db
mediconnect_dmp
mediconnect_prescription
mediconnect_appointments
mediconnect_teleconsultation
messaging_db
notification_db
medconnect
```

To check that the backend still compiles:

```powershell
mvn -DskipTests compile
```
