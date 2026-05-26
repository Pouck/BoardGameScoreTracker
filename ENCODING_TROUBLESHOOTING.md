# Troubleshooting: Character Encoding Error

## Error Message
```
'charmap' codec can't encode character '\u2212' in position 435: character maps to <undefined>
```

## Root Cause
Windows Command Prompt uses a limited character set (ASCII/cp1252) by default, but the project contains Unicode characters (the minus sign −, U+2212).

## Solution: Choose One

### Option 1: Use UTF-8 Batch Script (Easiest)
Run the UTF-8 compatible batch file:
```cmd
setup-utf8.bat
```

This automatically enables UTF-8 encoding before creating directories.

### Option 2: Use PowerShell Script
Run the PowerShell setup script with proper encoding:
```powershell
powershell -ExecutionPolicy Bypass -File setup-utf8.ps1
```

Or run manually with UTF-8 enabled:
```powershell
$OutputEncoding = [Console]::OutputEncoding = [System.Text.Encoding]::UTF8
python setup_complete.py
```

### Option 3: Enable UTF-8 in Command Prompt
In Command Prompt, run before setup:
```cmd
chcp 65001
python setup_complete.py
```

### Option 4: Use Python with UTF-8
Run Python with UTF-8 encoding:
```cmd
python -W ignore setup_complete.py
```

Or set environment variable:
```cmd
set PYTHONIOENCODING=utf-8
python setup_complete.py
```

### Option 5: Skip to Build (Directories already exist)
If directories were partially created, just build:
```cmd
./gradlew clean build
```

Gradle handles Unicode correctly by default.

---

## Which Option Should I Use?

| Your Situation | Best Option |
|---|---|
| First time setup | Option 1 (setup-utf8.bat) |
| Using PowerShell | Option 2 (setup-utf8.ps1) |
| Command Prompt user | Option 3 (chcp command) |
| Want to use Python | Option 4 (PYTHONIOENCODING) |
| Directories partially created | Option 5 (skip to build) |

---

## Quick Fix (Copy & Paste)

### For Command Prompt:
```cmd
cd H:\Projects\agents-android-app-development
setup-utf8.bat
```

### For PowerShell:
```powershell
cd H:\Projects\agents-android-app-development
powershell -ExecutionPolicy Bypass -File setup-utf8.ps1
```

---

## After Setup

Directories are created. Now build:
```bash
./gradlew clean build
./gradlew installDebug
```

Gradle handles Unicode properly, so no more encoding issues!

---

## Prevention

For future projects:
- Use ASCII characters in scripts
- Store Unicode strings in resource files (XML)
- Build with Gradle (handles encoding automatically)
- Use Python 3 with UTF-8 (python setup_complete.py)

---

## Need More Help?

1. Check README.md
2. Check QUICKSTART.md
3. Check setup-utf8.bat (new UTF-8 compatible batch)
4. Check setup-utf8.ps1 (new UTF-8 compatible PowerShell)

All setup scripts are now UTF-8 compatible!
