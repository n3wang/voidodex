package io.github.n3wang.voidcodex;

import io.github.n3wang.voidcodex.model.Biome;
import io.github.n3wang.voidcodex.model.CombatState;
import io.github.n3wang.voidcodex.model.Ship;
import io.github.n3wang.voidcodex.model.Sector;

import java.util.ArrayList;
import java.util.List;

/**
 * Global game state that persists across screens.
 */
public class GameState {
    private Ship currentShip;
    private Sector currentSector;
    private int currentSectorIndex;
    private List<Biome> discoveredBiomes;
    private CombatState combatState;

    public GameState() {
        currentSectorIndex = 0;
        discoveredBiomes = new ArrayList<>();
        combatState = new CombatState();
    }

    public Ship getCurrentShip() {
        return currentShip;
    }

    public void setCurrentShip(Ship ship) {
        this.currentShip = ship;
    }

    public Sector getCurrentSector() {
        return currentSector;
    }

    public void setCurrentSector(Sector sector) {
        this.currentSector = sector;
    }

    public int getCurrentSectorIndex() {
        return currentSectorIndex;
    }

    public void advanceSector() {
        currentSectorIndex++;
    }

    public List<Biome> getDiscoveredBiomes() {
        return discoveredBiomes;
    }

    public CombatState getCombatState() {
        return combatState;
    }
    
    public boolean isInCombat() {
        return combatState.isInCombat();
    }

    public void dispose() {
        // Clean up resources if needed
    }
}

