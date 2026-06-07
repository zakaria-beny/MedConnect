@echo off
REM MedConnect Docker Management Script for Windows

:menu
cls
echo.
echo ================================
echo   MedConnect Docker Manager
echo ================================
echo 1. Start all services (build)
echo 2. Start services (no build)
echo 3. Stop all services
echo 4. View logs (all)
echo 5. View logs (specific service)
echo 6. Check service health
echo 7. View running containers
echo 8. Restart specific service
echo 9. Database access (MongoDB)
echo 10. Database access (MySQL)
echo 11. Database access (Redis)
echo 12. Clean up (stop + remove volumes)
echo 13. View docker-compose status
echo 0. Exit
echo ================================
echo.

set /p choice="Select option (0-13): "

if "%choice%"=="1" goto start
if "%choice%"=="2" goto start_no_build
if "%choice%"=="3" goto stop
if "%choice%"=="4" goto logs_all
if "%choice%"=="5" goto logs_specific
if "%choice%"=="6" goto health
if "%choice%"=="7" goto containers
if "%choice%"=="8" goto restart
if "%choice%"=="9" goto mongodb
if "%choice%"=="10" goto mysql
if "%choice%"=="11" goto redis
if "%choice%"=="12" goto cleanup
if "%choice%"=="13" goto status
if "%choice%"=="0" goto exit_script
goto menu

:start
echo Starting MedConnect services with build...
docker-compose up -d --build
echo Services started!
echo.
echo Service URLs:
echo   API Gateway: http://localhost:8080
echo   Eureka Dashboard: http://localhost:8761
echo.
pause
goto menu

:start_no_build
echo Starting MedConnect services (no build)...
docker-compose up -d
echo Services started!
pause
goto menu

:stop
echo Stopping MedConnect services...
docker-compose down
echo Services stopped!
pause
goto menu

:logs_all
echo Viewing logs (press Ctrl+C to exit)...
docker-compose logs -f --tail=50
goto menu

:logs_specific
echo Available services:
docker-compose ps --services
set /p service_name="Enter service name: "
docker-compose logs -f --tail=50 %service_name%
goto menu

:health
echo Checking service health...
for %%S in ("api-gateway:8080" "user-service:8081" "dmp-service:8082" "prescription-service:8083" "appointment-service:8084") do (
    for /f "tokens=1,2 delims=:" %%A in ("%%S") do (
        curl -s http://localhost:%%B/health >nul 2>&1
        if errorlevel 1 (
            echo X %%A ^(port %%B^) is not responding
        ) else (
            echo + %%A ^(port %%B^) is healthy
        )
    )
)
pause
goto menu

:containers
echo Running containers:
docker-compose ps
pause
goto menu

:restart
echo Available services:
docker-compose ps --services
set /p service_name="Enter service name to restart: "
echo Restarting %service_name%...
docker-compose restart %service_name%
echo Service restarted!
pause
goto menu

:mongodb
echo Accessing MongoDB...
docker exec -it medconnect-mongodb mongosh
goto menu

:mysql
echo Accessing MySQL...
docker exec -it medconnect-mysql mysql -u medconnect -pmedconnect123 medconnect
goto menu

:redis
echo Accessing Redis...
docker exec -it medconnect-redis redis-cli
goto menu

:cleanup
echo WARNING: This will stop services and remove volumes
set /p confirm="Are you sure? (y/N): "
if /i "%confirm%"=="y" (
    docker-compose down -v
    echo Cleanup complete!
) else (
    echo Cleanup cancelled
)
pause
goto menu

:status
echo Docker Compose Status:
docker-compose ps
echo.
echo Resource Usage:
docker stats --no-stream
pause
goto menu

:exit_script
echo Goodbye!
exit /b 0
