#!/bin/bash
echo "==================================================="
echo "Stopping CTH Enterprise Application (Linux)"
echo "==================================================="
if [ -f app.pid ]; then
    PID=$(cat app.pid)
    kill -9 $PID 2>/dev/null || true
    rm -f app.pid
    echo "Application process (PID: $PID) terminated."
else
    fuser -k 8080/tcp 2>/dev/null || true
    echo "Terminated any process listening on port 8080."
fi
