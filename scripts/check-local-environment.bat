@echo off
echo [Env] Java
java -version

echo.
echo [Env] Maven
mvn -version

echo.
echo [Env] Node
node -v
npm -v

echo.
echo [Env] Docker
docker version

echo.
echo [Env] MySQL
mysql --version

echo.
echo [Env] Listening ports
netstat -ano | findstr ":8080 :8101 :8102 :8103 :8104 :8848 :15672 :5672 :9090 :3000 :5173"

echo.
echo [Env] Done.
pause
