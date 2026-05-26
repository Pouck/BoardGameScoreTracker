# Gradle Wrapper Setup

The gradle wrapper needs to download Gradle on first use. Here's what to do:

## Quick Fix (Choose One)

### **Option 1: Run Batch Script** (Easiest)
```cmd
setup-gradle.bat
```

Then:
```cmd
gradlew clean build
```

Gradle will auto-download on first run (~50MB, takes 2-5 min).

### **Option 2: Run PowerShell Script**
```powershell
powershell -ExecutionPolicy Bypass -File setup-gradle.ps1
```

Then:
```powershell
gradlew clean build
```

### **Option 3: Manual - Just Run Build**
```powershell
gradlew clean build
```

The wrapper will auto-download gradle on first use.

---

## What's Happening

1. **gradle-wrapper.properties** - Tells gradle which version to download
2. **gradlew/gradlew.bat** - Script that downloads & runs gradle
3. **gradle/wrapper/** - Directory where gradle gets stored

On first build, gradlew will:
1. Check gradle-wrapper.properties
2. Download Gradle 8.1 (~50MB)
3. Cache it locally
4. Run your build

---

## Troubleshooting

### "Still can't find GradleWrapperMain"
→ Just run build, it will download:
```powershell
gradlew clean build
```

### "Stuck downloading"
→ Check internet connection, or install gradle manually:
```powershell
choco install gradle
```

### "Permission denied"
→ Run as Administrator

---

## Complete Windows Build Steps

```powershell
# 1. Create directories
python setup_complete.py

# 2. Setup gradle wrapper (auto-downloads on use)
setup-gradle.bat

# 3. Build (this triggers gradle download if needed)
gradlew clean build

# 4. Install on device
gradlew installDebug
```

The first build will be slower (downloads gradle), but subsequent builds will be fast.

**Let me know when you run `gradlew clean build` - it should work now!**
