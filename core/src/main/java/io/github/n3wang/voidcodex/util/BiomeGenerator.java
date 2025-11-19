package io.github.n3wang.voidcodex.util;

import io.github.n3wang.voidcodex.model.Biome;
import io.github.n3wang.voidcodex.model.BiomeType;

import java.util.Random;

/**
 * Generates random biomes for sectors.
 */
public class BiomeGenerator {
    private static final Random random = new Random();

    public static Biome generateRandomBiome() {
        BiomeType[] types = BiomeType.values();
        BiomeType type = types[random.nextInt(types.length)];
        
        return createBiomeForType(type);
    }

    public static Biome createBiomeForType(BiomeType type) {
        switch (type) {
            case ION_STORM:
                return new Biome("Ion Storm Sector", 
                    "Electrical storms disrupt ship systems. Increased system failures and hazard damage.", 
                    type);
            case NEBULA_JUNGLE:
                return new Biome("Nebula Jungle", 
                    "Dense nebula clouds obscure sensors but hide valuable resources.", 
                    type);
            case CRYSTAL_CLUSTER:
                return new Biome("Crystal Cluster", 
                    "Beautiful but dangerous crystal formations. Weaker enemies but rich rewards.", 
                    type);
            case DERELICT_GRAVEYARD:
                return new Biome("Derelict Graveyard", 
                    "The final resting place of countless ships. High event frequency and scrap rewards.", 
                    type);
            case VOID_SPACE:
                return new Biome("Void Space", 
                    "Empty space between sectors. Fewer events but stable conditions.", 
                    type);
            case ASTEROID_FIELD:
                return new Biome("Asteroid Field", 
                    "Navigating through dangerous asteroid clusters requires skill and luck.", 
                    type);
            case PIRATE_TERRITORY:
                return new Biome("Pirate Territory", 
                    "Lawless space controlled by pirates. High combat frequency.", 
                    type);
            case RESEARCH_OUTPOST:
                return new Biome("Research Outpost", 
                    "Abandoned research stations offer knowledge and resources.", 
                    type);
            default:
                return new Biome("Unknown Sector", "An unexplored region of space.", type);
        }
    }
}

