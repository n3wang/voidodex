package io.github.n3wang.voidcodex.model;

/**
 * Combat modes: boarding or ship weapons.
 */
public enum CombatMode {
    BOARDING("Boarding"),
    WEAPONS("Weapons");
    
    private final String displayName;
    
    CombatMode(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() { return displayName; }
}

