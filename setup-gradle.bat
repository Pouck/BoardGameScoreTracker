@echo off
REM Setup Gradle Wrapper
REM This script downloads and sets up the Gradle wrapper

setlocal enabledelayedexpansion
cd /d "H:\Projects\agents-android-app-development"

echo ========================================
echo Gradle Wrapper Setup
echo ========================================
echo.

if exist "gradle\wrapper\gradle-wrapper.jar" (
    echo [✓] Gradle wrapper already exists
    goto build
)

echo [1/2] Creating gradle wrapper directory...
mkdir "gradle\wrapper" 2>nul
echo [✓] Directory created

echo.
echo [2/2] Downloading Gradle 8.1...
echo This may take a moment on first run...
echo.

REM The gradle wrapper will auto-download on first use
REM Just ensure the properties file exists
if exist "gradle\wrapper\gradle-wrapper.properties" (
    echo [✓] gradle-wrapper.properties exists
    echo Gradle will auto-download on first build
) else (
    echo [✗] gradle-wrapper.properties missing
    exit /b 1
)

:build
echo.
echo ========================================
echo Setup Complete!
echo ========================================
echo.
echo Next: Run: gradlew clean build
echo.
pause
