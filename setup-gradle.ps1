#!/usr/bin/env powershell

# PowerShell Gradle Wrapper Setup

$basePath = "H:\Projects\agents-android-app-development"
Set-Location $basePath

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Gradle Wrapper Setup" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Create gradle/wrapper directory
$wrapperDir = Join-Path $basePath "gradle\wrapper"
if (-not (Test-Path $wrapperDir)) {
    Write-Host "[1/2] Creating gradle wrapper directory..."
    New-Item -ItemType Directory -Path $wrapperDir -Force | Out-Null
    Write-Host "[✓] Directory created"
} else {
    Write-Host "[✓] gradle/wrapper directory exists"
}

Write-Host ""
Write-Host "[2/2] Gradle wrapper setup..."

$propsFile = Join-Path $wrapperDir "gradle-wrapper.properties"
if (Test-Path $propsFile) {
    Write-Host "[✓] gradle-wrapper.properties exists"
} else {
    Write-Host "[✗] gradle-wrapper.properties missing"
    exit 1
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "Setup Complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Next: Run: gradlew clean build" -ForegroundColor Yellow
Write-Host ""
Write-Host "Note: Gradle will auto-download on first build (~50MB)" -ForegroundColor Gray
Write-Host ""
