package io.github.n3wang.voidcodex.model;

/**
 * Types of rooms/systems on the ship.
 */
public enum RoomType {
    EMPTY("Empty", 0, 0),
    BRIDGE("Bridge", 0, 10),
    MEDBAY("Medbay", 2, 15),
    SHIELDS("Shields", 2, 20),
    WEAPONS("Weapons", 3, 15),
    ENGINES("Engines", 2, 15),
    OXYGEN("Oxygen", 1, 10),
    SENSORS("Sensors", 1, 10),
    DOORS("Doors", 0, 5);

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

