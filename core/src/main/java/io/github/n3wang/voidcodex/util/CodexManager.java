package io.github.n3wang.voidcodex.util;

import io.github.n3wang.voidcodex.model.BiomeType;
import io.github.n3wang.voidcodex.model.CodexEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Manages codex entries and provides pages for reading.
 */
public class CodexManager {
    private static final Random random = new Random();
    private static List<CodexEntry> allEntries = new ArrayList<>();

    static {
        initializeCodexEntries();
    }

    private static void initializeCodexEntries() {
        // General lore entries
        allEntries.add(new CodexEntry("void_01", "The Void", 
            "The Void stretches endlessly between stars. Ancient civilizations spoke of it as a living entity, " +
            "hungry and patient. Those who travel through it must respect its mysteries, for knowledge is the " +
            "only currency that matters here."));
        
        allEntries.add(new CodexEntry("navigation_01", "Hyperspace Navigation", 
            "Hyperspace jumps require precise calculations. A single error can strand a ship in the void forever. " +
            "The Codex contains the accumulated knowledge of countless navigators who came before."));
        
        // Biome-specific entries
        allEntries.add(new CodexEntry("ion_storm_01", "Ion Storms", 
            "Ion storms are the remnants of stellar explosions. Their electrical interference can disable " +
            "ship systems without warning. Experienced captains know to power down non-essential systems " +
            "before entering such regions.",
            BiomeType.ION_STORM, "Ion storms increase system failure rates by 30% and hazard damage by 50%."));
        
        allEntries.add(new CodexEntry("nebula_01", "Nebula Jungles", 
            "Nebula jungles are dense clouds of stellar matter. While they obscure sensors, they also hide " +
            "valuable resources from prying eyes. Many traders have made fortunes in these regions.",
            BiomeType.NEBULA_JUNGLE, "Sensor range reduced by 30%, but scrap rewards increased by 20%."));
        
        allEntries.add(new CodexEntry("crystal_01", "Crystal Clusters", 
            "Crystal clusters form around dying stars. The crystals themselves are valuable, but the formations " +
            "attract weaker predators. Still, the rewards often outweigh the risks.",
            BiomeType.CRYSTAL_CLUSTER, "Enemy strength reduced by 20%, scrap rewards increased by 30%."));
        
        allEntries.add(new CodexEntry("derelict_01", "Derelict Graveyards", 
            "Where ships go to die. Derelict graveyards are filled with the husks of vessels that failed to " +
            "navigate the void. Each wreck tells a story, and each story is a lesson.",
            BiomeType.DERELICT_GRAVEYARD, "Event frequency increased by 40%, scrap rewards increased by 50%."));
    }

    /**
     * Get 1-2 random codex entries, preferably related to the given biome.
     */
    public static List<CodexEntry> getCodexPagesForBiome(BiomeType biomeType) {
        List<CodexEntry> pages = new ArrayList<>();
        
        // Try to get at least one biome-specific entry
        List<CodexEntry> biomeEntries = allEntries.stream()
                .filter(e -> e.revealsBiomeInfo() && e.getRelatedBiome() == biomeType)
                .toList();
        
        if (!biomeEntries.isEmpty()) {
            pages.add(biomeEntries.get(random.nextInt(biomeEntries.size())));
        }
        
        // Add one more random entry (or if no biome entry found, add two random)
        List<CodexEntry> available = new ArrayList<>(allEntries);
        available.removeAll(pages);
        
        int totalPages = pages.isEmpty() ? 2 : 1;
        for (int i = 0; i < totalPages && !available.isEmpty(); i++) {
            CodexEntry entry = available.get(random.nextInt(available.size()));
            pages.add(entry);
            available.remove(entry);
        }
        
        return pages;
    }

    public static List<CodexEntry> getAllEntries() {
        return new ArrayList<>(allEntries);
    }
}

