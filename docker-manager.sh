#!/bin/bash

# MedConnect Docker Management Script

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Functions
show_menu() {
    echo -e "\n${BLUE}================================${NC}"
    echo -e "${BLUE}  MedConnect Docker Manager${NC}"
    echo -e "${BLUE}================================${NC}"
    echo "1. Start all services (build)"
    echo "2. Start services (no build)"
    echo "3. Stop all services"
    echo "4. View logs (all)"
    echo "5. View logs (specific service)"
    echo "6. Check service health"
    echo "7. View running containers"
    echo "8. Restart specific service"
    echo "9. Database access (MongoDB)"
    echo "10. Database access (MySQL)"
    echo "11. Database access (Redis)"
    echo "12. Clean up (stop + remove volumes)"
    echo "13. View docker-compose status"
    echo "0. Exit"
    echo -e "${BLUE}================================${NC}"
}

start_services() {
    echo -e "${YELLOW}Starting MedConnect services with build...${NC}"
    docker-compose up -d --build
    echo -e "${GREEN}✓ Services started!${NC}"
    echo -e "\n${BLUE}Service URLs:${NC}"
    echo "  API Gateway: http://localhost:8080"
    echo "  Eureka Dashboard: http://localhost:8761"
    echo "  User Service: http://localhost:8081"
    echo "  DMP Service: http://localhost:8082"
    echo "  Prescription Service: http://localhost:8083"
    echo "  Appointment Service: http://localhost:8084"
}

start_services_no_build() {
    echo -e "${YELLOW}Starting MedConnect services (no build)...${NC}"
    docker-compose up -d
    echo -e "${GREEN}✓ Services started!${NC}"
}

stop_services() {
    echo -e "${YELLOW}Stopping MedConnect services...${NC}"
    docker-compose down
    echo -e "${GREEN}✓ Services stopped!${NC}"
}

view_logs() {
    echo -e "${YELLOW}Viewing logs (press Ctrl+C to exit)...${NC}"
    docker-compose logs -f --tail=50
}

view_specific_logs() {
    echo -e "${YELLOW}Available services:${NC}"
    docker-compose ps --services
    echo -n "Enter service name: "
    read service_name
    echo -e "${YELLOW}Viewing logs for $service_name (press Ctrl+C to exit)...${NC}"
    docker-compose logs -f --tail=50 "$service_name"
}

check_health() {
    echo -e "${YELLOW}Checking service health...${NC}"
    
    services=("api-gateway:8080" "user-service:8081" "dmp-service:8082" "prescription-service:8083" "appointment-service:8084")
    
    for service_port in "${services[@]}"; do
        service="${service_port%:*}"
        port="${service_port#*:}"
        
        if curl -s http://localhost:$port/health > /dev/null 2>&1; then
            echo -e "${GREEN}✓${NC} $service (port $port) is ${GREEN}healthy${NC}"
        else
            echo -e "${RED}✗${NC} $service (port $port) is ${RED}not responding${NC}"
        fi
    done
}

view_containers() {
    echo -e "${YELLOW}Running containers:${NC}"
    docker-compose ps
}

restart_service() {
    echo -e "${YELLOW}Available services:${NC}"
    docker-compose ps --services
    echo -n "Enter service name to restart: "
    read service_name
    echo -e "${YELLOW}Restarting $service_name...${NC}"
    docker-compose restart "$service_name"
    echo -e "${GREEN}✓ $service_name restarted!${NC}"
}

access_mongodb() {
    echo -e "${YELLOW}Accessing MongoDB...${NC}"
    docker exec -it medconnect-mongodb mongosh
}

access_mysql() {
    echo -e "${YELLOW}Accessing MySQL...${NC}"
    docker exec -it medconnect-mysql mysql -u medconnect -pmedconnect123 medconnect
}

access_redis() {
    echo -e "${YELLOW}Accessing Redis...${NC}"
    docker exec -it medconnect-redis redis-cli
}

cleanup() {
    echo -e "${RED}WARNING: This will stop services and remove volumes (databases will be cleared)${NC}"
    read -p "Are you sure? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo -e "${YELLOW}Cleaning up...${NC}"
        docker-compose down -v
        echo -e "${GREEN}✓ Cleanup complete!${NC}"
    else
        echo -e "${YELLOW}Cleanup cancelled${NC}"
    fi
}

view_status() {
    echo -e "${BLUE}Docker Compose Status:${NC}"
    docker-compose ps
    echo -e "\n${BLUE}Resource Usage:${NC}"
    docker stats --no-stream
}

# Main loop
while true; do
    show_menu
    read -p "Select option (0-13): " choice
    
    case $choice in
        1) start_services ;;
        2) start_services_no_build ;;
        3) stop_services ;;
        4) view_logs ;;
        5) view_specific_logs ;;
        6) check_health ;;
        7) view_containers ;;
        8) restart_service ;;
        9) access_mongodb ;;
        10) access_mysql ;;
        11) access_redis ;;
        12) cleanup ;;
        13) view_status ;;
        0) 
            echo -e "${GREEN}Goodbye!${NC}"
            exit 0 
            ;;
        *) 
            echo -e "${RED}Invalid option. Please try again.${NC}" 
            ;;
    esac
done
