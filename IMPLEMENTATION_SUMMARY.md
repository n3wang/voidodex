# Void Codex - Implementation Summary

## Overview
This document summarizes the core systems implemented for Void Codex, a 2D space-strategy roguelike game built with LibGDX.

## Core Architecture

### Screen System
- **GameScreen**: Base class for all game screens with common functionality
- **MainMenuScreen**: Entry point with New Game and Exit options
- **ShipScreen**: Main gameplay screen showing ship grid, crew, and systems
- **CodexScreen**: Reading interface for Void Codex pages before hyperspace jumps
- **CombatScreen**: Placeholder for combat encounters (to be expanded)

### Game State Management
- **VoidCodexGame**: Main game class managing screens and global resources
- **GameState**: Persistent state across screens (ship, sector, biome info)

## Data Models

### Ship System
- **Ship**: Represents player's ship with rooms, crew, hull, and power
- **Room**: Individual rooms on ship grid with type, power, health, status
- **RoomType**: Enum of room types (Bridge, Medbay, Shields, Weapons, Engines, Oxygen, Sensors, Doors, Empty)

### Crew System
- **Crew**: Crew members with name, role, location, health, and skills
- **CrewRole**: Roles (Captain, Engineer, Medic, Pilot, Soldier, Scientist) with primary skills
- **Skill**: Skills (Engineering, Medical, Navigation, Combat, Research) with XP progression

### Sector & Biome System
- **Sector**: Represents a sector with biome, exploration status, and codex read status
- **Biome**: Biome with name, description, type, and gameplay modifiers
- **BiomeType**: Enum of biome types (Ion Storm, Nebula Jungle, Crystal Cluster, etc.)
- **BiomeGenerator**: Utility for generating random biomes

### Codex System
- **CodexEntry**: Individual codex pages with title, content, and optional biome hints
- **CodexManager**: Manages codex entries and provides pages for reading

## Visual Representation

All game elements currently use **placeholder visuals**:
- Ship rooms: Colored boxes with text labels showing room type, power, health, and crew count
- Crew: Text-based display with name, role, health, and location
- UI: Using default LibGDX Scene2D UI skin with boxes and text

## Key Features Implemented

1. **Ship Grid System**: 8x4 grid layout with room types and status display
2. **Crew Management**: Crew display with roles, health, and location tracking
3. **Codex Reading**: Multi-page codex system with biome-specific hints
4. **Biome System**: 8 different biome types with modifiers
5. **Hyperspace Jump**: Navigation between sectors (requires codex reading)
6. **Power System**: Power allocation tracking for ship systems
7. **Health System**: Hull and room health tracking

## Asset Requirements

See `assets/required_assets.csv` for a complete list of assets needed:
- Room sprites (9 types)
- Crew portraits (6 roles)
- Biome icons (8 types)
- Combat sprites (enemies, weapons, effects)
- UI elements
- Sound effects and music
- Animations

## Next Steps

1. **Combat System**: Implement paused real-time combat with power distribution
2. **Crew AI**: Pathfinding and task assignment for crew members
3. **Event System**: Branching events influenced by crew skills and codex knowledge
4. **Progression**: Unlockable ships, crew backgrounds, and upgrades
5. **Visual Polish**: Replace placeholder boxes with actual sprites
6. **Data Loading**: JSON/TOML loading for events, biomes, and ship layouts

## Technical Notes

- Framework: LibGDX 1.14, Java 17
- UI: Scene2D with FreeType support
- Viewport: 1280x720 (FitViewport)
- Architecture: Screen-based with GameState for persistence

