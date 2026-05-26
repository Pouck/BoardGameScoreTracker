@echo off
setlocal enabledelayedexpansion

set BASE_DIR=H:\Projects\agents-android-app-development

REM Create all necessary directories
mkdir "%BASE_DIR%\app\src\main\java\com\example\boardgamescoretracker\ui\screens" 2>nul
mkdir "%BASE_DIR%\app\src\main\java\com\example\boardgamescoretracker\ui\theme" 2>nul
mkdir "%BASE_DIR%\app\src\main\java\com\example\boardgamescoretracker\data\db" 2>nul
mkdir "%BASE_DIR%\app\src\main\java\com\example\boardgamescoretracker\data\repository" 2>nul
mkdir "%BASE_DIR%\app\src\main\java\com\example\boardgamescoretracker\viewmodel" 2>nul
mkdir "%BASE_DIR%\app\src\main\res\values" 2>nul
mkdir "%BASE_DIR%\app\src\main\res\drawable" 2>nul
mkdir "%BASE_DIR%\app\src\test\java\com\example\boardgamescoretracker" 2>nul
mkdir "%BASE_DIR%\app\src\androidTest\java\com\example\boardgamescoretracker" 2>nul

echo Directory structure created successfully!
