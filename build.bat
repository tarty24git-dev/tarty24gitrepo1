@echo off
echo ===================================================
echo Building CTH Enterprise Application Package
echo ===================================================
call mvn clean package -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Build failed! Please check Java/Maven installation and logs.
    exit /b %ERRORLEVEL%
)
echo ===================================================
echo Build completed successfully. Output JAR is in target/
echo ===================================================
