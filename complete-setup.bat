@echo off
REM Complete Setup for Android App Build
REM Creates directories, sets up gradle wrapper, and builds the project

setlocal enabledelayedexpansion
cd /d "H:\Projects\agents-android-app-development"

echo ========================================
echo Complete Android App Setup
echo ========================================
echo.

echo [Phase 1] Creating app directories...
mkdir "app\src\main\java\com\example\boardgamescoretracker\ui\screens" 2>nul
mkdir "app\src\main\java\com\example\boardgamescoretracker\ui\theme" 2>nul
mkdir "app\src\main\java\com\example\boardgamescoretracker\data\db" 2>nul
mkdir "app\src\main\java\com\example\boardgamescoretracker\data\repository" 2>nul
mkdir "app\src\main\java\com\example\boardgamescoretracker\viewmodel" 2>nul
mkdir "app\src\main\res\values" 2>nul
mkdir "app\src\main\res\drawable" 2>nul
mkdir "app\src\test\java\com\example\boardgamescoretracker" 2>nul
mkdir "app\src\androidTest\java\com\example\boardgamescoretracker" 2>nul
echo [✓] App directories created

echo.
echo [Phase 2] Creating gradle wrapper directories...
mkdir "gradle\wrapper" 2>nul
echo [✓] Gradle wrapper directory created

echo.
echo [Phase 3] Creating gradle wrapper properties...
(
echo distributionBase=GRADLE_USER_HOME
echo distributionPath=wrapper/dists
echo distributionUrl=https://services.gradle.org/distributions/gradle-8.1-bin.zip
echo networkTimeout=10000
echo validateDistributionUrl=true
echo zipStoreBase=GRADLE_USER_HOME
echo zipStorePath=wrapper/dists
) > "gradle\wrapper\gradle-wrapper.properties"
echo [✓] gradle-wrapper.properties created

echo.
echo ========================================
echo [✓] SETUP COMPLETE
echo ========================================
echo.
echo Next Steps:
echo 1. Run: gradlew clean build
echo    (This will download Gradle on first run - 50MB)
echo 2. Then: gradlew installDebug
echo.
pause
