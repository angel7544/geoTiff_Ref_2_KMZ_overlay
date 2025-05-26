@echo off
set SCRIPT_DIR=%~dp0
set JAR_PATH=%SCRIPT_DIR%kmz-splitter-1.0-SNAPSHOT.jar

if not exist "%JAR_PATH%" (
    echo Error: Could not find %JAR_PATH%
    echo Please ensure you have built the project using 'mvn clean package'
    exit /b 1
)

java -jar "%JAR_PATH%" 