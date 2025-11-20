package io.github.n3wang.voidcodex.model;

/**
 * Represents a room on the ship grid.
 * Each room is a 2x2 grid of tiles, with one crew member per tile.
 */
public class Room {
    private int x, y; // Room position on ship grid
    private RoomType type;
    private int powerLevel;
    private int maxPower;
    private int health;
    private int maxHealth;
    private boolean hasFire;
    private boolean hasBreach;
    private Crew assignedCrew;
    
    // 2x2 grid of tiles - each tile can hold one crew member
    // tiles[tileX][tileY] = Crew at that tile position, or null if empty
    private Crew[][] tiles;
    
    // Oxygen level per tile (0.0 to 1.0, where 1.0 is full oxygen)
    private float[][] tileOxygen;

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
        this.tiles = new Crew[2][2]; // 2x2 grid of tiles
        this.tileOxygen = new float[2][2]; // Initialize oxygen levels
        // Start with random oxygen levels - some tiles without oxygen for testing
        java.util.Random random = new java.util.Random();
        for (int tileX = 0; tileX < 2; tileX++) {
            for (int tileY = 0; tileY < 2; tileY++) {
                // Randomly set some tiles to 0 oxygen, others to partial
                if (random.nextFloat() < 0.3f) {
                    // 30% chance of no oxygen
                    tileOxygen[tileX][tileY] = 0.0f;
                } else {
                    // 70% chance of partial oxygen (0.3 to 0.8)
                    tileOxygen[tileX][tileY] = 0.3f + random.nextFloat() * 0.5f;
                }
            }
        }
    }
    
    public float getTileOxygen(int tileX, int tileY) {
        if (tileX < 0 || tileX >= 2 || tileY < 0 || tileY >= 2) {
            return 0.0f;
        }
        return tileOxygen[tileX][tileY];
    }
    
    public void setTileOxygen(int tileX, int tileY, float oxygen) {
        if (tileX >= 0 && tileX < 2 && tileY >= 0 && tileY < 2) {
            tileOxygen[tileX][tileY] = Math.max(0.0f, Math.min(1.0f, oxygen));
        }
    }
    
    /**
     * Get crew at specific tile position (0-1 for both x and y).
     */
    public Crew getCrewAtTile(int tileX, int tileY) {
        if (tileX < 0 || tileX >= 2 || tileY < 0 || tileY >= 2) {
            return null;
        }
        return tiles[tileX][tileY];
    }
    
    /**
     * Set crew at specific tile position. Returns true if successful.
     */
    public boolean setCrewAtTile(int tileX, int tileY, Crew crew) {
        if (tileX < 0 || tileX >= 2 || tileY < 0 || tileY >= 2) {
            return false;
        }
        if (crew != null && tiles[tileX][tileY] != null) {
            return false; // Tile already occupied
        }
        tiles[tileX][tileY] = crew;
        return true;
    }
    
    /**
     * Remove crew from a tile.
     */
    public void removeCrewFromTile(int tileX, int tileY) {
        if (tileX >= 0 && tileX < 2 && tileY >= 0 && tileY < 2) {
            tiles[tileX][tileY] = null;
        }
    }
    
    /**
     * Check if a tile is empty.
     */
    public boolean isTileEmpty(int tileX, int tileY) {
        return getCrewAtTile(tileX, tileY) == null;
    }
    
    /**
     * Get number of crew in this room.
     */
    public int getCrewCount() {
        int count = 0;
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                if (tiles[x][y] != null) {
                    count++;
                }
            }
        }
        return count;
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

