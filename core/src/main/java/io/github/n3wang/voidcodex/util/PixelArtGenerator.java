package io.github.n3wang.voidcodex.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import io.github.n3wang.voidcodex.model.CrewRole;
import io.github.n3wang.voidcodex.model.RoomType;

import java.util.HashMap;
import java.util.Map;

/**
 * Generates simple pixel art assets programmatically.
 */
public class PixelArtGenerator {
    private static final int TILE_SIZE = 64;
    private static Map<String, Texture> cachedTextures = new HashMap<>();

    /**
     * Generate a simple room sprite based on room type.
     */
    public static Texture generateRoomSprite(RoomType type) {
        String key = "room_" + type.name();
        if (cachedTextures.containsKey(key)) {
            return cachedTextures.get(key);
        }

        Pixmap pixmap = new Pixmap(TILE_SIZE, TILE_SIZE, Pixmap.Format.RGBA8888);
        
        // Base color based on room type
        com.badlogic.gdx.graphics.Color baseColor = getRoomColor(type);
        pixmap.setColor(baseColor);
        pixmap.fillRectangle(2, 2, TILE_SIZE - 4, TILE_SIZE - 4);
        
        // Border
        pixmap.setColor(0.3f, 0.3f, 0.3f, 1f);
        pixmap.drawRectangle(0, 0, TILE_SIZE, TILE_SIZE);
        
        // Add simple pattern based on type
        addRoomPattern(pixmap, type, baseColor);
        
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        cachedTextures.put(key, texture);
        return texture;
    }

    private static void addRoomPattern(Pixmap pixmap, RoomType type, com.badlogic.gdx.graphics.Color baseColor) {
        int size = TILE_SIZE;
        pixmap.setColor(baseColor.r * 1.3f, baseColor.g * 1.3f, baseColor.b * 1.3f, 1f);
        
        switch (type) {
            case BRIDGE:
                // Add control panel pattern
                pixmap.fillRectangle(10, 10, 20, 15);
                pixmap.fillRectangle(34, 10, 20, 15);
                break;
            case WEAPONS:
                // Add weapon barrel pattern
                pixmap.fillRectangle(20, 5, 24, 8);
                pixmap.fillRectangle(20, 51, 24, 8);
                break;
            case SHIELDS:
                // Add shield pattern (circle)
                pixmap.fillCircle(size / 2, size / 2, 15);
                break;
            case ENGINES:
                // Add engine pattern
                pixmap.fillRectangle(10, 45, 44, 10);
                break;
            case MEDBAY:
                // Add cross pattern
                pixmap.fillRectangle(27, 15, 10, 34);
                pixmap.fillRectangle(15, 27, 34, 10);
                break;
            case OXYGEN:
                // Add O2 symbol
                pixmap.fillCircle(size / 2, size / 2, 12);
                pixmap.setColor(baseColor);
                pixmap.fillCircle(size / 2, size / 2, 8);
                break;
            case SENSORS:
                // Add radar pattern
                pixmap.fillCircle(size / 2, size / 2, 20);
                pixmap.setColor(baseColor);
                pixmap.fillCircle(size / 2, size / 2, 15);
                break;
            case DOORS:
                // Add door pattern
                pixmap.fillRectangle(20, 10, 24, 44);
                break;
            case EMPTY:
                // No pattern for empty rooms
                break;
        }
    }

    private static com.badlogic.gdx.graphics.Color getRoomColor(RoomType type) {
        switch (type) {
            case BRIDGE: return new com.badlogic.gdx.graphics.Color(0.4f, 0.8f, 1f, 1f); // Cyan
            case MEDBAY: return new com.badlogic.gdx.graphics.Color(0.4f, 1f, 0.4f, 1f); // Green
            case SHIELDS: return new com.badlogic.gdx.graphics.Color(0.3f, 0.5f, 1f, 1f); // Blue
            case WEAPONS: return new com.badlogic.gdx.graphics.Color(1f, 0.3f, 0.3f, 1f); // Red
            case ENGINES: return new com.badlogic.gdx.graphics.Color(1f, 0.6f, 0.2f, 1f); // Orange
            case OXYGEN: return new com.badlogic.gdx.graphics.Color(0.9f, 0.9f, 0.9f, 1f); // White
            case SENSORS: return new com.badlogic.gdx.graphics.Color(0.8f, 0.4f, 1f, 1f); // Purple
            case DOORS: return new com.badlogic.gdx.graphics.Color(0.5f, 0.5f, 0.5f, 1f); // Gray
            default: return new com.badlogic.gdx.graphics.Color(0.2f, 0.2f, 0.2f, 1f); // Dark gray
        }
    }

    /**
     * Generate a simple crew member sprite (full body for map view).
     */
    public static Texture generateCrewSprite(int index) {
        String key = "crew_" + index;
        if (cachedTextures.containsKey(key)) {
            return cachedTextures.get(key);
        }

        Pixmap pixmap = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        
        // Body color (vary by index) - use simple color variation
        com.badlogic.gdx.graphics.Color bodyColor;
        switch (index % 4) {
            case 0: bodyColor = new com.badlogic.gdx.graphics.Color(0.8f, 0.6f, 0.4f, 1f); break; // Tan
            case 1: bodyColor = new com.badlogic.gdx.graphics.Color(0.4f, 0.6f, 0.8f, 1f); break; // Blue
            case 2: bodyColor = new com.badlogic.gdx.graphics.Color(0.6f, 0.8f, 0.4f, 1f); break; // Green
            default: bodyColor = new com.badlogic.gdx.graphics.Color(0.8f, 0.4f, 0.6f, 1f); break; // Pink
        }
        pixmap.setColor(bodyColor);
        
        // Head (circle)
        pixmap.fillCircle(16, 10, 6);
        
        // Body (rectangle)
        pixmap.fillRectangle(12, 16, 8, 12);
        
        // Arms
        pixmap.fillRectangle(8, 18, 4, 8);
        pixmap.fillRectangle(20, 18, 4, 8);
        
        // Legs
        pixmap.fillRectangle(13, 28, 3, 4);
        pixmap.fillRectangle(16, 28, 3, 4);
        
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        cachedTextures.put(key, texture);
        return texture;
    }
    
    /**
     * Generate a crew profile picture (headshot) based on role.
     * Uses PixelLab assets if available, otherwise falls back to generated sprite.
     */
    public static Texture generateCrewProfile(CrewRole role) {
        String key = "profile_" + role.name();
        if (cachedTextures.containsKey(key)) {
            return cachedTextures.get(key);
        }
        
        // Try to load PixelLab character asset
        String roleName = role.name().toLowerCase();
        FileHandle characterFile = Gdx.files.internal("pixellab/characters/" + roleName + "/rotations/south.png");
        
        if (characterFile.exists()) {
            try {
                // Load the full character sprite
                Pixmap fullSprite = new Pixmap(characterFile);
                
                // Extract profile picture (head and upper body area)
                // Character is ~28px tall in 48x48 canvas, head is roughly top 16-20 pixels
                int profileSize = 32; // Size of profile picture
                int headStartY = 4; // Start of head in the sprite
                int headHeight = 20; // Height of head/shoulders area
                int centerX = fullSprite.getWidth() / 2;
                
                // Create profile picture (headshot)
                Pixmap profilePixmap = new Pixmap(profileSize, profileSize, Pixmap.Format.RGBA8888);
                profilePixmap.setColor(0, 0, 0, 0); // Transparent background
                profilePixmap.fill();
                
                // Copy head and upper body area, centered
                int srcX = Math.max(0, centerX - profileSize / 2);
                int srcY = headStartY;
                int copyWidth = Math.min(profileSize, fullSprite.getWidth() - srcX);
                int copyHeight = Math.min(headHeight, fullSprite.getHeight() - srcY);
                
                // Draw the head/upper body area
                for (int y = 0; y < copyHeight && y < profileSize; y++) {
                    for (int x = 0; x < copyWidth && x < profileSize; x++) {
                        int srcPixelX = srcX + x;
                        int srcPixelY = srcY + y;
                        if (srcPixelX < fullSprite.getWidth() && srcPixelY < fullSprite.getHeight()) {
                            int pixel = fullSprite.getPixel(srcPixelX, srcPixelY);
                            int offsetX = (profileSize - copyWidth) / 2;
                            profilePixmap.drawPixel(offsetX + x, y, pixel);
                        }
                    }
                }
                
                Texture texture = new Texture(profilePixmap);
                profilePixmap.dispose();
                fullSprite.dispose();
                cachedTextures.put(key, texture);
                return texture;
            } catch (Exception e) {
                Gdx.app.error("PixelArtGenerator", "Failed to load PixelLab character for " + roleName + ": " + e.getMessage());
                // Fall through to fallback
            }
        }
        
        // Fallback: Generate simple profile picture
        Pixmap pixmap = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 0); // Transparent background
        pixmap.fill();
        
        // Head (circle) - larger for profile
        com.badlogic.gdx.graphics.Color headColor = getRoleColor(role);
        pixmap.setColor(headColor);
        pixmap.fillCircle(16, 20, 10);
        
        // Simple face features
        pixmap.setColor(0.2f, 0.2f, 0.2f, 1f);
        pixmap.fillCircle(12, 18, 2); // Left eye
        pixmap.fillCircle(20, 18, 2); // Right eye
        pixmap.drawLine(14, 22, 18, 22); // Mouth
        
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        cachedTextures.put(key, texture);
        return texture;
    }
    
    private static com.badlogic.gdx.graphics.Color getRoleColor(CrewRole role) {
        switch (role) {
            case CAPTAIN: return new com.badlogic.gdx.graphics.Color(0.4f, 0.2f, 0.6f, 1f); // Purple
            case ENGINEER: return new com.badlogic.gdx.graphics.Color(0.8f, 0.6f, 0.2f, 1f); // Orange
            case MEDIC: return new com.badlogic.gdx.graphics.Color(0.9f, 0.9f, 0.9f, 1f); // White
            case PILOT: return new com.badlogic.gdx.graphics.Color(0.2f, 0.6f, 0.8f, 1f); // Blue
            case SOLDIER: return new com.badlogic.gdx.graphics.Color(0.6f, 0.3f, 0.3f, 1f); // Red-brown
            case SCIENTIST: return new com.badlogic.gdx.graphics.Color(0.6f, 0.4f, 0.8f, 1f); // Purple
            default: return new com.badlogic.gdx.graphics.Color(0.8f, 0.6f, 0.4f, 1f); // Tan
        }
    }

    /**
     * Generate a weapon icon.
     */
    public static Texture generateWeaponIcon() {
        if (cachedTextures.containsKey("weapon_icon")) {
            return cachedTextures.get("weapon_icon");
        }

        Pixmap pixmap = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        
        // Weapon base
        pixmap.setColor(0.6f, 0.6f, 0.6f, 1f);
        pixmap.fillRectangle(8, 12, 16, 8);
        
        // Barrel
        pixmap.setColor(0.4f, 0.4f, 0.4f, 1f);
        pixmap.fillRectangle(24, 14, 6, 4);
        
        // Muzzle flash
        pixmap.setColor(1f, 1f, 0.3f, 1f);
        pixmap.fillRectangle(30, 13, 2, 6);
        
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        cachedTextures.put("weapon_icon", texture);
        return texture;
    }

    /**
     * Generate a fire effect sprite.
     */
    public static Texture generateFireSprite() {
        if (cachedTextures.containsKey("fire")) {
            return cachedTextures.get("fire");
        }

        Pixmap pixmap = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        
        // Fire base (orange/red)
        pixmap.setColor(1f, 0.5f, 0f, 1f);
        pixmap.fillRectangle(10, 20, 12, 8);
        
        // Fire middle (yellow)
        pixmap.setColor(1f, 0.8f, 0f, 1f);
        pixmap.fillRectangle(12, 16, 8, 6);
        
        // Fire top (yellow-white)
        pixmap.setColor(1f, 1f, 0.5f, 1f);
        pixmap.fillRectangle(14, 12, 4, 4);
        
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        cachedTextures.put("fire", texture);
        return texture;
    }

    /**
     * Generate a breach/hull damage sprite.
     */
    public static Texture generateBreachSprite() {
        if (cachedTextures.containsKey("breach")) {
            return cachedTextures.get("breach");
        }

        Pixmap pixmap = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        
        // Breach hole (dark)
        pixmap.setColor(0.1f, 0.1f, 0.1f, 1f);
        pixmap.fillCircle(16, 16, 10);
        
        // Crack lines
        pixmap.setColor(0.3f, 0.3f, 0.3f, 1f);
        pixmap.drawLine(8, 8, 24, 24);
        pixmap.drawLine(24, 8, 8, 24);
        
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        cachedTextures.put("breach", texture);
        return texture;
    }

    /**
     * Generate a shield bubble sprite.
     */
    public static Texture generateShieldSprite() {
        if (cachedTextures.containsKey("shield")) {
            return cachedTextures.get("shield");
        }

        Pixmap pixmap = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        
        // Shield bubble (semi-transparent blue)
        pixmap.setColor(0.3f, 0.6f, 1f, 0.6f);
        pixmap.fillCircle(16, 16, 14);
        
        // Outer ring
        pixmap.setColor(0.5f, 0.8f, 1f, 0.8f);
        pixmap.drawCircle(16, 16, 14);
        
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        cachedTextures.put("shield", texture);
        return texture;
    }

    /**
     * Generate a power box sprite.
     */
    public static Texture generatePowerBoxSprite(boolean powered) {
        String key = "powerbox_" + (powered ? "on" : "off");
        if (cachedTextures.containsKey(key)) {
            return cachedTextures.get(key);
        }

        Pixmap pixmap = new Pixmap(18, 18, Pixmap.Format.RGBA8888);
        
        if (powered) {
            pixmap.setColor(0.2f, 1f, 0.2f, 1f); // Green
        } else {
            pixmap.setColor(0.2f, 0.2f, 0.2f, 1f); // Dark gray
        }
        pixmap.fillRectangle(1, 1, 16, 16);
        
        // Border
        pixmap.setColor(0.5f, 0.5f, 0.5f, 1f);
        pixmap.drawRectangle(0, 0, 18, 18);
        
        // Glow if powered
        if (powered) {
            pixmap.setColor(0.4f, 1f, 0.4f, 0.5f);
            pixmap.fillRectangle(0, 0, 18, 18);
        }
        
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        cachedTextures.put(key, texture);
        return texture;
    }

    /**
     * Generate a simple ship hull sprite.
     */
    public static Texture generateShipHullSprite() {
        if (cachedTextures.containsKey("hull")) {
            return cachedTextures.get("hull");
        }

        Pixmap pixmap = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
        
        // Hull base (gray)
        pixmap.setColor(0.5f, 0.5f, 0.5f, 1f);
        pixmap.fillRectangle(0, 0, 64, 64);
        
        // Hull plating pattern
        pixmap.setColor(0.4f, 0.4f, 0.4f, 1f);
        for (int i = 0; i < 64; i += 8) {
            pixmap.drawLine(i, 0, i, 64);
            pixmap.drawLine(0, i, 64, i);
        }
        
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        cachedTextures.put("hull", texture);
        return texture;
    }

    /**
     * Generate a biome icon.
     */
    public static Texture generateBiomeIcon(io.github.n3wang.voidcodex.model.BiomeType type) {
        String key = "biome_" + type.name();
        if (cachedTextures.containsKey(key)) {
            return cachedTextures.get(key);
        }

        Pixmap pixmap = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        com.badlogic.gdx.graphics.Color color;
        
        switch (type) {
            case ION_STORM:
                color = new com.badlogic.gdx.graphics.Color(0.8f, 0.8f, 0.2f, 1f); // Yellow
                pixmap.setColor(color);
                pixmap.fillCircle(16, 16, 12);
                // Lightning pattern
                pixmap.setColor(1f, 1f, 0.5f, 1f);
                pixmap.drawLine(10, 8, 16, 16);
                pixmap.drawLine(16, 16, 22, 24);
                break;
            case NEBULA_JUNGLE:
                color = new com.badlogic.gdx.graphics.Color(0.6f, 0.3f, 0.8f, 1f); // Purple
                pixmap.setColor(color);
                pixmap.fillCircle(16, 16, 14);
                // Cloud pattern
                pixmap.setColor(0.7f, 0.4f, 0.9f, 1f);
                pixmap.fillCircle(12, 12, 6);
                pixmap.fillCircle(20, 20, 6);
                break;
            case CRYSTAL_CLUSTER:
                color = new com.badlogic.gdx.graphics.Color(0.4f, 0.8f, 1f, 1f); // Light blue
                pixmap.setColor(color);
                // Crystal shapes
                pixmap.fillRectangle(10, 10, 6, 12);
                pixmap.fillRectangle(16, 8, 6, 16);
                pixmap.fillRectangle(22, 12, 6, 10);
                break;
            case DERELICT_GRAVEYARD:
                color = new com.badlogic.gdx.graphics.Color(0.3f, 0.3f, 0.3f, 1f); // Dark gray
                pixmap.setColor(color);
                pixmap.fillRectangle(8, 20, 16, 8);
                // Cross pattern
                pixmap.setColor(0.5f, 0.5f, 0.5f, 1f);
                pixmap.fillRectangle(14, 12, 4, 16);
                pixmap.fillRectangle(10, 20, 12, 4);
                break;
            default:
                color = new com.badlogic.gdx.graphics.Color(0.5f, 0.5f, 0.5f, 1f);
                pixmap.setColor(color);
                pixmap.fillCircle(16, 16, 12);
        }
        
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        cachedTextures.put(key, texture);
        return texture;
    }

    /**
     * Generate an enemy ship sprite.
     */
    public static Texture generateEnemyShipSprite() {
        if (cachedTextures.containsKey("enemy_ship")) {
            return cachedTextures.get("enemy_ship");
        }

        Pixmap pixmap = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
        
        // Enemy ship base (reddish)
        pixmap.setColor(0.7f, 0.3f, 0.3f, 1f);
        pixmap.fillRectangle(10, 20, 44, 24);
        
        // Enemy ship details
        pixmap.setColor(0.5f, 0.2f, 0.2f, 1f);
        pixmap.fillRectangle(15, 25, 34, 14);
        
        // Weapon ports
        pixmap.setColor(0.9f, 0.1f, 0.1f, 1f);
        pixmap.fillCircle(20, 32, 3);
        pixmap.fillCircle(44, 32, 3);
        
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        cachedTextures.put("enemy_ship", texture);
        return texture;
    }

    /**
     * Generate a weapon projectile sprite.
     */
    public static Texture generateProjectileSprite() {
        if (cachedTextures.containsKey("projectile")) {
            return cachedTextures.get("projectile");
        }

        Pixmap pixmap = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
        
        // Projectile (laser bolt)
        pixmap.setColor(1f, 0.8f, 0.2f, 1f); // Yellow
        pixmap.fillCircle(8, 8, 6);
        
        // Core
        pixmap.setColor(1f, 1f, 1f, 1f); // White
        pixmap.fillCircle(8, 8, 3);
        
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        cachedTextures.put("projectile", texture);
        return texture;
    }

    /**
     * Generate a skill icon.
     */
    public static Texture generateSkillIcon(io.github.n3wang.voidcodex.model.Skill skill) {
        String key = "skill_" + skill.name();
        if (cachedTextures.containsKey(key)) {
            return cachedTextures.get(key);
        }

        Pixmap pixmap = new Pixmap(24, 24, Pixmap.Format.RGBA8888);
        com.badlogic.gdx.graphics.Color color;
        
        switch (skill) {
            case ENGINEERING:
                color = new com.badlogic.gdx.graphics.Color(0.8f, 0.6f, 0.2f, 1f); // Orange
                pixmap.setColor(color);
                pixmap.fillRectangle(8, 4, 8, 16);
                pixmap.fillRectangle(4, 8, 16, 8);
                break;
            case MEDICAL:
                color = new com.badlogic.gdx.graphics.Color(1f, 0.3f, 0.3f, 1f); // Red
                pixmap.setColor(color);
                pixmap.fillRectangle(10, 4, 4, 16);
                pixmap.fillRectangle(4, 10, 16, 4);
                break;
            case NAVIGATION:
                color = new com.badlogic.gdx.graphics.Color(0.3f, 0.6f, 1f, 1f); // Blue
                pixmap.setColor(color);
                pixmap.fillCircle(12, 12, 8);
                break;
            case COMBAT:
                color = new com.badlogic.gdx.graphics.Color(0.8f, 0.2f, 0.2f, 1f); // Dark red
                pixmap.setColor(color);
                pixmap.fillRectangle(6, 6, 12, 4);
                pixmap.fillRectangle(10, 4, 4, 16);
                break;
            case RESEARCH:
                color = new com.badlogic.gdx.graphics.Color(0.6f, 0.3f, 0.8f, 1f); // Purple
                pixmap.setColor(color);
                pixmap.fillCircle(12, 12, 10);
                pixmap.setColor(0.8f, 0.5f, 1f, 1f);
                pixmap.fillCircle(12, 12, 6);
                break;
            default:
                color = new com.badlogic.gdx.graphics.Color(0.5f, 0.5f, 0.5f, 1f);
                pixmap.setColor(color);
                pixmap.fillCircle(12, 12, 8);
        }
        
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        cachedTextures.put(key, texture);
        return texture;
    }

    /**
     * Clean up all generated textures.
     */
    public static void dispose() {
        for (Texture texture : cachedTextures.values()) {
            texture.dispose();
        }
        cachedTextures.clear();
    }
    
    /**
     * Generate an icon for a room/system type.
     * Returns a small icon (16x16 or 24x24) representing the system.
     */
    public static Texture generateSystemIcon(RoomType type) {
        String key = "icon_" + type.name();
        if (cachedTextures.containsKey(key)) {
            return cachedTextures.get(key);
        }
        
        int iconSize = 24;
        Pixmap pixmap = new Pixmap(iconSize, iconSize, Pixmap.Format.RGBA8888);
        // Clear with transparent background
        pixmap.setColor(0f, 0f, 0f, 0f);
        pixmap.fill();
        pixmap.setColor(1f, 1f, 1f, 1f); // White icon
        
        // Draw icon based on room type
        switch (type) {
            case BRIDGE:
                // Crescent moon / C-shape pointing right
                pixmap.fillCircle(iconSize / 2, iconSize / 2, iconSize / 2 - 2);
                pixmap.setColor(0f, 0f, 0f, 0f); // Transparent to create crescent
                pixmap.fillCircle(iconSize / 2 - 2, iconSize / 2, iconSize / 2 - 4);
                pixmap.setColor(1f, 1f, 1f, 1f); // Reset to white
                break;
            case SHIELDS:
                // Flashlight beam / spotlight
                pixmap.fillRectangle(iconSize / 2 - 2, 0, 4, iconSize);
                // Triangle for beam
                for (int y = iconSize - 6; y < iconSize; y++) {
                    int width = (iconSize - y) * 2;
                    pixmap.fillRectangle(iconSize / 2 - width / 2, y, width, 1);
                }
                break;
            case WEAPONS:
                // Spaceship / fighter jet pointing up
                // Triangle body
                for (int y = 0; y < iconSize - 4; y++) {
                    int width = (iconSize - 4 - y) / 2;
                    if (width > 0) {
                        pixmap.fillRectangle(iconSize / 2 - width, y, width * 2, 1);
                    }
                }
                // Bottom rectangle
                pixmap.fillRectangle(iconSize / 2 - 2, iconSize - 8, 4, 4);
                break;
            case ENGINES:
                // Arrow pointing right
                // Arrow body
                pixmap.fillRectangle(4, iconSize / 2 - 2, iconSize - 8, 4);
                // Arrow head
                for (int y = 0; y < 8; y++) {
                    int offset = y;
                    pixmap.fillRectangle(iconSize - 8 + offset, iconSize / 2 - 4 + y, 8 - offset, 1);
                }
                break;
            case MEDBAY:
                // Cross / medical symbol
                pixmap.fillRectangle(iconSize / 2 - 1, 2, 2, iconSize - 4);
                pixmap.fillRectangle(2, iconSize / 2 - 1, iconSize - 4, 2);
                break;
            case OXYGEN:
                // Circle with dots (oxygen molecule)
                pixmap.drawCircle(iconSize / 2, iconSize / 2, iconSize / 3);
                pixmap.fillCircle(iconSize / 2 - 4, iconSize / 2, 2);
                pixmap.fillCircle(iconSize / 2 + 4, iconSize / 2, 2);
                break;
            case SENSORS:
                // Radar waves / sonar pulse
                pixmap.drawCircle(iconSize / 2, iconSize / 2, iconSize / 3);
                pixmap.drawCircle(iconSize / 2, iconSize / 2, iconSize / 2 - 2);
                pixmap.fillCircle(iconSize / 2, iconSize / 2, 2);
                break;
            case DOORS:
                // Q shape / circular symbol
                pixmap.drawCircle(iconSize / 2, iconSize / 2, iconSize / 3);
                pixmap.fillRectangle(iconSize / 2 + 2, iconSize / 2 + 2, 4, 4);
                break;
            default:
                // Default: square
                pixmap.fillRectangle(4, 4, iconSize - 8, iconSize - 8);
                break;
        }
        
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        cachedTextures.put(key, texture);
        return texture;
    }
}

