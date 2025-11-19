package io.github.n3wang.voidcodex.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a sector biome with modifiers and characteristics.
 */
public class Biome {
    private String name;
    private String description;
    private Map<String, Float> modifiers; // Modifier name -> value multiplier
    private BiomeType type;

    public Biome(String name, String description, BiomeType type) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.modifiers = new HashMap<>();
        initializeModifiers();
    }

    private void initializeModifiers() {
        // Default modifiers
        modifiers.put("hazard_damage", 1.0f);
        modifiers.put("event_frequency", 1.0f);
        modifiers.put("enemy_strength", 1.0f);
        modifiers.put("scrap_reward", 1.0f);
        modifiers.put("crew_xp_gain", 1.0f);
        
        // Type-specific modifiers
        switch (type) {
            case ION_STORM:
                modifiers.put("hazard_damage", 1.5f);
                modifiers.put("system_failure_chance", 1.3f);
                break;
            case NEBULA_JUNGLE:
                modifiers.put("sensor_range", 0.7f);
                modifiers.put("scrap_reward", 1.2f);
                break;
            case CRYSTAL_CLUSTER:
                modifiers.put("enemy_strength", 0.8f);
                modifiers.put("scrap_reward", 1.3f);
                break;
            case DERELICT_GRAVEYARD:
                modifiers.put("event_frequency", 1.4f);
                modifiers.put("scrap_reward", 1.5f);
                break;
        }
    }

    public float getModifier(String key) {
        return modifiers.getOrDefault(key, 1.0f);
    }

    // Getters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BiomeType getType() { return type; }
    public Map<String, Float> getModifiers() { return modifiers; }
}

