@echo off
echo ===================================================
echo Starting CTH Enterprise Application (Windows)
echo ===================================================
IF NOT EXIST target\enterprise-app-1.0.0.jar (
    echo Executable JAR not found. Running build.bat first...
    call build.bat
)
start "CTH Application" java -jar target\enterprise-app-1.0.0.jar
echo Application started in background on http://localhost:8080
echo Dynamic Swagger UI available at http://localhost:8080/swagger-ui.html
