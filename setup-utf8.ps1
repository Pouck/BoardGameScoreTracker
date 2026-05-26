# PowerShell setup script with proper UTF-8 encoding
# Run with: powershell -ExecutionPolicy Bypass -File setup-utf8.ps1

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

$basePath = "H:\Projects\agents-android-app-development"

# Change to project directory
Set-Location $basePath

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Android Project Setup (UTF-8)" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Create directories
Write-Host "[1/2] Creating directory structure..." -ForegroundColor Yellow

$directories = @(
    "app\src\main\java\com\example\boardgamescoretracker\ui\screens",
    "app\src\main\java\com\example\boardgamescoretracker\ui\theme",
    "app\src\main\java\com\example\boardgamescoretracker\data\db",
    "app\src\main\java\com\example\boardgamescoretracker\data\repository",
    "app\src\main\java\com\example\boardgamescoretracker\viewmodel",
    "app\src\main\res\values",
    "app\src\main\res\drawable",
    "app\src\test\java\com\example\boardgamescoretracker",
    "app\src\androidTest\java\com\example\boardgamescoretracker"
)

foreach ($dir in $directories) {
    $fullPath = Join-Path $basePath $dir
    if (-not (Test-Path $fullPath)) {
        New-Item -ItemType Directory -Path $fullPath -Force | Out-Null
        Write-Host "  [✓] $dir" -ForegroundColor Green
    }
}

Write-Host ""
Write-Host "[2/2] Verification..." -ForegroundColor Yellow

$allCreated = $true
foreach ($dir in $directories) {
    $fullPath = Join-Path $basePath $dir
    if (Test-Path $fullPath) {
        Write-Host "  [✓] $(Split-Path $fullPath -Leaf)" -ForegroundColor Green
    } else {
        Write-Host "  [✗] $(Split-Path $fullPath -Leaf)" -ForegroundColor Red
        $allCreated = $false
    }
}

Write-Host ""
if ($allCreated) {
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "✓ SETUP COMPLETE" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "Next steps:" -ForegroundColor Cyan
    Write-Host "1. .\gradlew clean build" -ForegroundColor White
    Write-Host "2. .\gradlew installDebug" -ForegroundColor White
    Write-Host ""
} else {
    Write-Host "✗ SETUP FAILED" -ForegroundColor Red
    exit 1
}
