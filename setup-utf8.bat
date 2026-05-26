@echo off
REM Setup script with UTF-8 encoding support
REM Run this instead of setup_complete.py if you encounter encoding errors

setlocal enabledelayedexpansion
cd /d "H:\Projects\agents-android-app-development"

echo ========================================
echo Android Project Setup (UTF-8 Compatible)
echo ========================================
echo.

echo [1/3] Creating directory structure...
mkdir "app\src\main\java\com\example\boardgamescoretracker\ui\screens" 2>nul
mkdir "app\src\main\java\com\example\boardgamescoretracker\ui\theme" 2>nul
mkdir "app\src\main\java\com\example\boardgamescoretracker\data\db" 2>nul
mkdir "app\src\main\java\com\example\boardgamescoretracker\data\repository" 2>nul
mkdir "app\src\main\java\com\example\boardgamescoretracker\viewmodel" 2>nul
mkdir "app\src\main\res\values" 2>nul
mkdir "app\src\main\res\drawable" 2>nul
mkdir "app\src\test\java\com\example\boardgamescoretracker" 2>nul
mkdir "app\src\androidTest\java\com\example\boardgamescoretracker" 2>nul
echo [✓] Directories created

echo.
echo [2/3] Setting up UTF-8 environment...
chcp 65001 >nul 2>&1
echo [✓] UTF-8 encoding enabled

echo.
echo [3/3] Verification...
if exist "app\src\main\java\com\example\boardgamescoretracker" (
    echo [✓] Structure complete
) else (
    echo [✗] Structure creation failed
    pause
    exit /b 1
)

echo.
echo ========================================
echo ✓ SETUP COMPLETE
echo ========================================
echo.
echo Next step: Run: ./gradlew clean build
echo.
pause
