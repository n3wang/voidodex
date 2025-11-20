package io.github.n3wang.voidcodex.model;

/**
 * Types of weapons.
 */
public enum WeaponType {
    LASER("Laser"),
    MISSILE("Missile"),
    BEAM("Beam"),
    ION("Ion"),
    BOMB("Bomb");

    private final String displayName;

    WeaponType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }
}

