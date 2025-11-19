package io.github.n3wang.voidcodex.model;

/**
 * Types of sector biomes.
 */
public enum BiomeType {
    ION_STORM("Ion Storm"),
    NEBULA_JUNGLE("Nebula Jungle"),
    CRYSTAL_CLUSTER("Crystal Cluster"),
    DERELICT_GRAVEYARD("Derelict Graveyard"),
    VOID_SPACE("Void Space"),
    ASTEROID_FIELD("Asteroid Field"),
    PIRATE_TERRITORY("Pirate Territory"),
    RESEARCH_OUTPOST("Research Outpost");

    private final String displayName;

    BiomeType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }
}

