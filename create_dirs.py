#!/usr/bin/env python3
import os
from pathlib import Path

# Define the base directory
base_dir = r"H:\Projects\agents-android-app-development"

# List of directories to create
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

# Create all directories
for directory in directories:
    full_path = os.path.join(base_dir, directory)
    Path(full_path).mkdir(parents=True, exist_ok=True)
    print(f"✓ Created: {directory}")

print("\nAll directories created successfully!")

# Verify by listing the structure
print("\nVerifying directory structure:")
print("=" * 60)

for root, dirs, files in os.walk(os.path.join(base_dir, "app")):
    level = root.replace(os.path.join(base_dir, "app"), "").count(os.sep)
    indent = " " * 2 * level
    print(f"{indent}{os.path.basename(root)}/")
    subindent = " " * 2 * (level + 1)
    for dir_name in sorted(dirs):
        print(f"{subindent}{dir_name}/")
