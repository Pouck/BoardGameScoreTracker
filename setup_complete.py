#!/usr/bin/env python3
"""
Android Project Setup Script
Creates complete directory structure and source files for Board Game Score Tracker
"""

import os
import sys
from pathlib import Path

BASE_DIR = Path(r"H:/Projects/agents-android-app-development")

# Directory structure to create
DIRS = [
    "app/src/main/java/com/example/boardgamescoretracker/ui/screens",
    "app/src/main/java/com/example/boardgamescoretracker/ui/theme",
    "app/src/main/java/com/example/boardgamescoretracker/data/db",
    "app/src/main/java/com/example/boardgamescoretracker/data/repository",
    "app/src/main/java/com/example/boardgamescoretracker/viewmodel",
    "app/src/main/res/values",
    "app/src/main/res/drawable",
    "app/src/test/java/com/example/boardgamescoretracker",
    "app/src/androidTest/java/com/example/boardgamescoretracker",
]

FILES = {
    "app/src/main/res/values/strings.xml": '''<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">Board Game Score Tracker</string>
    <string name="new_game">New Game</string>
    <string name="add_player">Add Player</string>
    <string name="remove_player">Remove Player</string>
    <string name="player_name">Player Name</string>
    <string name="score">Score</string>
    <string name="increment">+</string>
    <string name="decrement">−</string>
    <string name="finish_game">Finish Game</string>
    <string name="game_history">Game History</string>
    <string name="no_games">No games yet. Start a new game!</string>
</resources>
''',
    "app/src/main/res/values/colors.xml": '''<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="purple_200">#FFBB86FC</color>
    <color name="purple_500">#FF6200EE</color>
    <color name="purple_700">#FF3700B3</color>
    <color name="teal_200">#FF03DAC5</color>
    <color name="teal_700">#FF018786</color>
    <color name="black">#FF000000</color>
    <color name="white">#FFFFFFFF</color>
</resources>
''',
    "app/src/main/res/values/themes.xml": '''<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.BoardGameScoreTracker" parent="android:Theme.Material.Light.NoActionBar" />
</resources>
''',
}

def create_directories():
    """Create all required directories"""
    print("Creating directories...")
    for dir_path in DIRS:
        full_path = BASE_DIR / dir_path
        full_path.mkdir(parents=True, exist_ok=True)
        print(f"  ✓ {dir_path}")
    print(f"\n✅ {len(DIRS)} directories created!")

def create_files():
    """Create XML resource files"""
    print("\nCreating XML files...")
    for file_path, content in FILES.items():
        full_path = BASE_DIR / file_path
        full_path.parent.mkdir(parents=True, exist_ok=True)
        full_path.write_text(content)
        print(f"  ✓ {file_path}")
    print(f"\n✅ {len(FILES)} files created!")

def verify_structure():
    """Verify the directory structure"""
    print("\nVerifying structure...")
    missing = []
    for dir_path in DIRS:
        full_path = BASE_DIR / dir_path
        if not full_path.exists():
            missing.append(dir_path)
    
    if missing:
        print(f"\n❌ Missing directories:")
        for d in missing:
            print(f"  ✗ {d}")
        return False
    else:
        print("✅ All directories verified!")
        return True

if __name__ == "__main__":
    try:
        create_directories()
        create_files()
        if verify_structure():
            print("\n" + "="*50)
            print("✅ SETUP COMPLETE - Ready to proceed!")
            print("="*50)
            sys.exit(0)
        else:
            print("\n❌ Setup failed - some directories missing")
            sys.exit(1)
    except Exception as e:
        print(f"\n❌ Error: {e}")
        sys.exit(1)
