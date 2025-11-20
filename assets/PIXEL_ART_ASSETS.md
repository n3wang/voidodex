# Pixel Art Assets - Temporary Placeholders

This document describes the programmatically generated pixel art assets used as temporary placeholders in Void Codex.

## Generated Assets

All assets are generated at runtime using `PixelArtGenerator` class. They are simple pixel art sprites created programmatically.

### Room Sprites (64x64 pixels)
- **Bridge**: Cyan room with control panel pattern
- **Medbay**: Green room with cross pattern
- **Shields**: Blue room with circle pattern
- **Weapons**: Red room with weapon barrel pattern
- **Engines**: Orange room with engine pattern
- **Oxygen**: White room with O2 symbol
- **Sensors**: Purple room with radar pattern
- **Doors**: Gray room
- **Empty**: Dark gray room

### Crew Sprites (32x32 pixels)
- Simple stick-figure style crew members
- Color variations: Tan, Blue, Green, Pink
- Shows head (circle), body (rectangle), arms, and legs
- Different colors for different crew members

### Weapon Icons (32x32 pixels)
- Simple weapon sprite showing:
  - Weapon base (gray rectangle)
  - Barrel (dark gray)
  - Muzzle flash (yellow)

### Power Boxes (18x18 pixels)
- **Powered**: Green box with glow effect
- **Unpowered**: Dark gray box
- Used in systems power management UI

### Fire Effect (32x32 pixels)
- Orange/red base
- Yellow middle
- Yellow-white top
- Represents fires in rooms

### Breach Effect (32x32 pixels)
- Dark hole (black circle)
- Crack lines (gray)
- Represents hull breaches

### Shield Bubble (32x32 pixels)
- Semi-transparent blue circle
- Outer ring for definition
- Represents shield layers

### Ship Hull (64x64 pixels)
- Gray base with plating pattern
- Grid lines for hull plating effect

## Usage

All assets are generated on-demand and cached. They are used in:
- Ship grid rooms
- Crew portraits
- Weapon displays
- Power management UI
- Hazard indicators

## Future Replacement

These are temporary placeholder assets. In the future, they should be replaced with:
- Hand-drawn pixel art sprites
- Animated sprites for crew movement
- More detailed room designs
- Better weapon visualizations
- Animated fire and breach effects

## Code Location

All generation code is in: `io.github.n3wang.voidcodex.util.PixelArtGenerator`

