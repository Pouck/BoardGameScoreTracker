#!/usr/bin/env python3
import os
import sys
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
print("Creating directories...")
print("=" * 60)
for directory in directories:
    full_path = os.path.join(base_dir, directory)
    Path(full_path).mkdir(parents=True, exist_ok=True)
    print(f"✓ Created: {directory}")

print("\n" + "=" * 60)
print("All directories created successfully!")
print("=" * 60)

# Verify by listing the structure
print("\nDirectory structure verification:")
print("=" * 60)

def print_tree(path, prefix="", is_last=True):
    """Recursively print directory tree"""
    basename = os.path.basename(path)
    connector = "└── " if is_last else "├── "
    
    if os.path.isdir(path):
        print(f"{prefix}{connector}{basename}/")
        contents = []
        try:
            contents = sorted(os.listdir(path))
        except PermissionError:
            pass
        
        for i, item in enumerate(contents):
            item_path = os.path.join(path, item)
            is_last_item = (i == len(contents) - 1)
            extension = "    " if is_last else "│   "
            if os.path.isdir(item_path):
                print_tree(item_path, prefix + extension, is_last_item)

# Start tree printing
app_path = os.path.join(base_dir, "app")
if os.path.exists(app_path):
    print("app/")
    contents = sorted(os.listdir(app_path))
    for i, item in enumerate(contents):
        item_path = os.path.join(app_path, item)
        is_last_item = (i == len(contents) - 1)
        connector = "└── " if is_last_item else "├── "
        extension = "    " if is_last_item else "│   "
        if os.path.isdir(item_path):
            print_tree(item_path, connector, is_last_item)
else:
    print(f"ERROR: app directory not found at {app_path}")
    sys.exit(1)

print("\n" + "=" * 60)
print("✓ All directories verified successfully!")
print("=" * 60)
