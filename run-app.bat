@echo off
setlocal enabledelayedexpansion

set SCRIPT_DIR=%~dp0
set JAR_NAME=kmz-splitter-1.0-SNAPSHOT-jar-with-dependencies.jar

:: Check if JavaFX SDK exists
if not exist "%JAVA_HOME%\javafx-sdk-17.0.2\lib" (
    echo Error: JavaFX SDK not found in %JAVA_HOME%\javafx-sdk-17.0.2\lib
    echo Please install JavaFX SDK 17.0.2 and set JAVA_HOME correctly
    exit /b 1
)

:: Add JavaFX modules and JVM options
set JAVAFX_MODULES=javafx.controls,javafx.fxml,javafx.graphics,javafx.base
set JVM_OPTS=--add-opens javafx.graphics/javafx.scene=ALL-UNNAMED --add-opens javafx.controls/javafx.scene.control=ALL-UNNAMED
set JAVAFX_PATH=%JAVA_HOME%\javafx-sdk-17.0.2\lib

:: Run the application
java %JVM_OPTS% --module-path "%JAVAFX_PATH%" --add-modules %JAVAFX_MODULES% -jar "%JAR_NAME%" 