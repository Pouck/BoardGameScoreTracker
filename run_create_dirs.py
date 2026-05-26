import subprocess
import sys
import os

# Try to create the directories directly
base_dir = r"H:\Projects\agents-android-app-development"
os.chdir(base_dir)

directories = [
    r"app\src\main\java\com\example\boardgamescoretracker\ui\screens",
    r"app\src\main\java\com\example\boardgamescoretracker\ui\theme",
    r"app\src\main\java\com\example\boardgamescoretracker\data\db",
    r"app\src\main\java\com\example\boardgamescoretracker\data\repository",
    r"app\src\main\java\com\example\boardgamescoretracker\viewmodel",
    r"app\src\main\res\values",
    r"app\src\main\res\drawable",
    r"app\src\test\java\com\example\boardgamescoretracker",
    r"app\src\androidTest\java\com\example\boardgamescoretracker",
]

print("Creating directories...")
for dir_path in directories:
    try:
        os.makedirs(dir_path, exist_ok=True)
        print(f"✓ {dir_path}")
    except Exception as e:
        print(f"✗ {dir_path}: {e}")

print("\nVerifying...")
if os.path.isdir("app"):
    print("✓ app directory created")
    for root, dirs, files in os.walk("app"):
        level = root.replace("app", "").count(os.sep)
        indent = " " * 2 * level
        print(f"{indent}{os.path.basename(root)}/")
        subindent = " " * 2 * (level + 1)
        for d in sorted(dirs):
            print(f"{subindent}{d}/")
else:
    print("✗ Failed to create app directory")
    sys.exit(1)

print("\nSuccess! All directories created.")
