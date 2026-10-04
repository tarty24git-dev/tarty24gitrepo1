@echo off
echo ===================================================
echo Stopping CTH Enterprise Application (Windows)
echo ===================================================
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8080 ^| findstr LISTENING') do taskkill /f /pid %%a 2>nul
echo CTH Application process on port 8080 terminated.
