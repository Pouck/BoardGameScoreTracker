# 🚚 Guide: Moving Android Development to H: Drive

This guide explains how to move your entire Android development environment from `C:` to `H:` to free up space.

## 1. Move the Android SDK (Largest component)
The SDK typically takes 20GB - 100GB.
1. **Copy** `C:\Users\pierr\AppData\Local\Android\Sdk` to `H:\Android\Sdk`.
2. **Update Project**: Open `local.properties` in your project and change the path:
   ```properties
   sdk.dir=H\:\\Android\\Sdk
   ```
3. **Update Android Studio**: 
   - Go to **Settings** > **Appearance & Behavior** > **System Settings** > **Android SDK**.
   - Click **Edit** next to "Android SDK Location" and select the new path on `H:`.

## 2. Move the .gradle Folder (Caches)
The `.gradle` folder stores dependencies and can grow very large.
1. **Copy** `C:\Users\pierr\.gradle` to `H:\.gradle`.
2. **Update Android Studio**:
   - Go to **Settings** > **Build, Execution, Deployment** > **Build Tools** > **Gradle**.
   - Change **Gradle user home** to `H:\.gradle`.
3. **System Variable** (Optional but recommended):
   - Add a Windows Environment Variable `GRADLE_USER_HOME` with value `H:\.gradle`.

## 3. Move AVDs (Emulators)
Virtual devices are stored in the `.android` folder and are very large.
1. **Copy** `C:\Users\pierr\.android` to `H:\.android`.
2. **System Variable**:
   - Add a Windows Environment Variable `ANDROID_USER_HOME` with value `H:\.android`.
   - (For older versions) Add `ANDROID_AVD_HOME` as `H:\.android\avd`.

## 4. Move Android Studio IDE
1. Uninstall Android Studio from `C:`.
2. Reinstall it, choosing `H:\Programs\Android Studio` as the installation target.

## 5. Verify Project Paths
I have already updated your project scripts in `H:\Projects\agents-android-app-development` to point to the new drive.

## 6. Fix: "File names too long" Errors
Windows has a default 260-character limit (MAX_PATH) that often breaks when copying `.gradle` or `Sdk` folders.

### Option A: Enable Long Paths in Windows (Recommended)
1. Press `Win + R`, type `regedit`, and hit Enter.
2. Navigate to: `HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Control\FileSystem`.
3. Find `LongPathsEnabled` and set its value to `1`.
4. Restart your computer.

### Option B: Use Robocopy (Bypasses Explorer limits)
Windows Explorer fails on long paths, but the command-line tool `robocopy` handles them perfectly. Use these commands in a terminal:

**For the SDK:**
```cmd
robocopy "C:\Users\pierr\AppData\Local\Android\Sdk" "H:\Android\Sdk" /E /ZB /R:5 /W:5 /XF *.lock /MT:16
```

**For Gradle:**
```cmd
robocopy "C:\Users\pierr\.gradle" "H:\.gradle" /E /ZB /R:5 /W:5 /XF *.lock /MT:16
```

**Flags used:**
- `/E`: Copy subdirectories, including empty ones.
- `/ZB`: Use restartable mode; if access is denied, use Backup mode.
- `/XF *.lock`: Skip those "in-use" lock files.
- `/MT:16`: Multi-threaded (copies 16 files at once - much faster).

---
**Note:** After moving, you can safely delete the corresponding folders on `C:` to recover space!
