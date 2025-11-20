package io.github.n3wang.voidcodex.model;

/**
 * Types of rooms/systems on the ship.
 */
public enum RoomType {
    EMPTY("Empty", 0, 0),
    BRIDGE("Bridge", 10, 10), // Increased max power
    MEDBAY("Medbay", 10, 15),
    SHIELDS("Shields", 10, 20),
    WEAPONS("Weapons", 10, 15),
    ENGINES("Engines", 10, 15),
    OXYGEN("Oxygen", 10, 10), // Increased max power
    SENSORS("Sensors", 10, 10),
    DOORS("Doors", 10, 5);

    private final String displayName;
    private final int defaultMaxPower;
    private final int defaultHealth;

    RoomType(String displayName, int defaultMaxPower, int defaultHealth) {
        this.displayName = displayName;
        this.defaultMaxPower = defaultMaxPower;
        this.defaultHealth = defaultHealth;
    }

    public String getDisplayName() { return displayName; }
    public int getDefaultMaxPower() { return defaultMaxPower; }
    public int getDefaultHealth() { return defaultHealth; }
}

