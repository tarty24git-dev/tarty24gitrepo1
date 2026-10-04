#!/bin/bash
echo "==================================================="
echo "Building CTH Enterprise Application Package"
echo "==================================================="
mvn clean package -DskipTests
if [ $? -ne 0 ]; then
    echo "[ERROR] Build failed! Please check Java/Maven installation and logs."
    exit 1
fi
echo "==================================================="
echo "Build completed successfully. Output JAR is in target/"
echo "==================================================="
