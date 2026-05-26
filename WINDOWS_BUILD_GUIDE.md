# Windows Build Guide

## Issue: './gradlew' not recognized

On Windows, you need to use different syntax depending on your shell.

---

## ✅ CORRECT COMMANDS FOR WINDOWS

### **PowerShell (Recommended)**
```powershell
gradlew clean build
gradlew installDebug
```

Or with explicit `.bat`:
```powershell
.\gradlew.bat clean build
.\gradlew.bat installDebug
```

### **Command Prompt (cmd.exe)**
```cmd
gradlew.bat clean build
gradlew.bat installDebug
```

Or without `.bat`:
```cmd
gradlew clean build
gradlew installDebug
```

---

## ❌ WRONG (Don't Use)
```powershell
./gradlew clean build    ← This is Linux/Mac syntax, won't work on Windows
```

---

## Step-by-Step for Windows Users

### 1. Open PowerShell or Command Prompt
- **PowerShell**: Windows Key → Type `powershell` → Enter
- **Command Prompt**: Windows Key → Type `cmd` → Enter

### 2. Navigate to Project
```powershell
cd "H:\Projects\agents-android-app-development"
```

### 3. Setup Directories
```powershell
python setup_complete.py
```

Or use UTF-8 version if you got encoding errors:
```powershell
powershell -ExecutionPolicy Bypass -File setup-utf8.ps1
```

### 4. Build the App
```powershell
gradlew clean build
```

**Expected output:**
```
BUILD SUCCESSFUL in 1m 23s
```

### 5. Install on Device
```powershell
gradlew installDebug
```

**Expected output:**
```
Installing APK 'app-debug.apk' on 'device'
Successfully installed the app
```

---

## Full Windows Command Sequence

**All at once (copy & paste this):**

```powershell
cd "H:\Projects\agents-android-app-development"
python setup_complete.py
gradlew clean build
gradlew installDebug
```

---

## Troubleshooting

### Error: "Python not found"
→ Install Python from python.org or use batch script:
```powershell
setup-utf8.bat
```

### Error: "JAVA_HOME not set"
→ Install Android Studio or JDK, set JAVA_HOME in environment variables

### Error: "Android SDK not found"
→ Install Android Studio which includes SDK

### Error: "Device not found"
→ Connect Android phone OR start emulator in Android Studio

---

## Key Differences: Windows vs Linux/Mac

| Action | Linux/Mac | Windows |
|--------|-----------|---------|
| Run gradle | `./gradlew build` | `gradlew clean build` |
| Use batch wrapper | N/A | `gradlew.bat` |
| Python setup | `python setup.py` | `python setup_complete.py` |
| Paths | `/path/to/file` | `C:\path\to\file` |

---

## Still Having Issues?

1. Use Command Prompt instead of PowerShell:
   ```cmd
   gradlew.bat clean build
   ```

2. Check Java is installed:
   ```powershell
   java -version
   ```

3. Check Android SDK:
   ```powershell
   adb devices
   ```

4. Clean gradle cache:
   ```powershell
   gradlew clean
   ```

---

## Quick Reference

**3 Windows Commands to Run Your App:**

```powershell
# 1. Create directories
python setup_complete.py

# 2. Build APK
gradlew clean build

# 3. Install on device
gradlew installDebug
```

**Done! App is running! 🎉**
