# PixelLab Assets

This directory contains assets generated using PixelLab MCP tools.

## Character Assets

Character profile pictures are stored in `characters/{role}/rotations/south.png` where `{role}` is the lowercase role name (captain, engineer, medic, pilot, soldier, scientist).

The game will automatically:
1. Try to load PixelLab character assets from this directory
2. Extract profile pictures (headshots) from the full character sprites
3. Fall back to generated sprites if PixelLab assets are not available

## Asset Structure

```
pixellab/
├── characters/
│   ├── captain/
│   │   ├── rotations/
│   │   │   ├── south.png (used for profile)
│   │   │   ├── north.png
│   │   │   ├── east.png
│   │   │   └── west.png
│   │   └── metadata.json
│   ├── engineer/
│   ├── medic/
│   ├── pilot/
│   ├── soldier/
│   └── scientist/
├── rooms/ (future: room sprites)
├── icons/ (future: system icons)
└── weapons/ (future: weapon icons)
```

## Current Status

- ✅ Captain character downloaded and ready
- ⏳ Engineer, Medic, Pilot, Soldier characters processing
- ⏳ Scientist character processing
- ⏳ Room sprites - to be created
- ⏳ System icons - to be created
- ⏳ Weapon icons - to be created

## Usage

The `PixelArtGenerator.generateCrewProfile(CrewRole role)` method automatically:
1. Looks for PixelLab assets in `assets/pixellab/characters/{role}/rotations/south.png`
2. Extracts a 32x32 profile picture from the head/upper body area
3. Falls back to a simple generated profile if assets are not found

## Adding New Assets

1. Use PixelLab MCP tools to generate characters/rooms/icons
2. Download the ZIP files
3. Extract to the appropriate directory structure
4. The game will automatically detect and use them


