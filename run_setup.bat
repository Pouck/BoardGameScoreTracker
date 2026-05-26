@echo off
REM ============================================================
REM Android Gradle Project Directory Setup
REM This script creates all required directories for the project
REM ============================================================

setlocal enabledelayedexpansion

set "BASE_DIR=%cd%"
set "SUCCESS=0"

echo.
echo ========================================
echo Android Project Directory Setup
echo ========================================
echo.
echo Creating directory structure in: %BASE_DIR%
echo.

REM Create all directories
echo Creating directories...
call :CreateDir "app\src\main\java\com\example\boardgamescoretracker\ui\screens"
call :CreateDir "app\src\main\java\com\example\boardgamescoretracker\ui\theme"
call :CreateDir "app\src\main\java\com\example\boardgamescoretracker\data\db"
call :CreateDir "app\src\main\java\com\example\boardgamescoretracker\data\repository"
call :CreateDir "app\src\main\java\com\example\boardgamescoretracker\viewmodel"
call :CreateDir "app\src\main\res\values"
call :CreateDir "app\src\main\res\drawable"
call :CreateDir "app\src\test\java\com\example\boardgamescoretracker"
call :CreateDir "app\src\androidTest\java\com\example\boardgamescoretracker"

echo.
echo ========================================
echo Verifying directory structure...
echo ========================================
echo.

if exist "app" (
    echo [SUCCESS] app directory exists
    echo.
    echo Directory structure:
    echo.
    tree /f app
    echo.
    set "SUCCESS=1"
) else (
    echo [ERROR] app directory was not created!
    set "SUCCESS=0"
)

echo.
if %SUCCESS% equ 1 (
    echo ========================================
    echo COMPLETE: All directories created!
    echo ========================================
) else (
    echo ========================================
    echo ERROR: Setup failed!
    echo ========================================
)
echo.

endlocal
exit /b %SUCCESS%

:CreateDir
set "dir=%~1"
if not exist "%dir%" (
    mkdir "%dir%"
    if exist "%dir%" (
        echo   [OK] %dir%
    ) else (
        echo   [FAIL] %dir%
    )
) else (
    echo   [EXISTS] %dir%
)
exit /b 0
