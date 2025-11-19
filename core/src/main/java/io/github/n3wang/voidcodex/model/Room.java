package io.github.n3wang.voidcodex.model;

/**
 * Represents a room on the ship grid.
 */
public class Room {
    private int x, y;
    private RoomType type;
    private int powerLevel;
    private int maxPower;
    private int health;
    private int maxHealth;
    private boolean hasFire;
    private boolean hasBreach;
    private Crew assignedCrew;

    public Room(int x, int y, RoomType type) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.maxPower = type.getDefaultMaxPower();
        this.powerLevel = 0;
        this.maxHealth = type.getDefaultHealth();
        this.health = maxHealth;
        this.hasFire = false;
        this.hasBreach = false;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public RoomType getType() { return type; }
    public int getPowerLevel() { return powerLevel; }
    public void setPowerLevel(int level) { this.powerLevel = Math.max(0, Math.min(level, maxPower)); }
    public int getMaxPower() { return maxPower; }
    public int getHealth() { return health; }
    public void setHealth(int health) { this.health = Math.max(0, Math.min(health, maxHealth)); }
    public int getMaxHealth() { return maxHealth; }
    public boolean hasFire() { return hasFire; }
    public void setFire(boolean fire) { this.hasFire = fire; }
    public boolean hasBreach() { return hasBreach; }
    public void setBreach(boolean breach) { this.hasBreach = breach; }
    public Crew getAssignedCrew() { return assignedCrew; }
    public void setAssignedCrew(Crew crew) { this.assignedCrew = crew; }
}

