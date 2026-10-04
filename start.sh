#!/bin/bash
echo "==================================================="
echo "Starting CTH Enterprise Application (Linux)"
echo "==================================================="
if [ ! -f target/enterprise-app-1.0.0.jar ]; then
    echo "Executable JAR not found. Running build.sh first..."
    ./build.sh
fi
nohup java -jar target/enterprise-app-1.0.0.jar > app.log 2>&1 &
echo $! > app.pid
echo "Application started in background (PID: $(cat app.pid)) on http://localhost:8080"
echo "Dynamic Swagger UI available at http://localhost:8080/swagger-ui.html"
