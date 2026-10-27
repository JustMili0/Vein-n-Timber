@echo off

cd /D %~dp0
cd /D ../

powershell -c "./gradlew :1.21.1:publishModrinth"
powershell -c "./gradlew :1.21.11:publishModrinth"
powershell -c "./gradlew :26.1.2:publishModrinth"
powershell -c "./gradlew :26.2:publishModrinth"
powershell -c "./gradlew :26.3:publishModrinth"

pause >nul