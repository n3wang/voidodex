package io.github.n3wang.voidcodex.util;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
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
     * Generate a simple crew member sprite.
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
}

